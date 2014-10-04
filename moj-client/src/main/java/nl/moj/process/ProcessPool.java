package nl.moj.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enqueues Runnables to prevent too many threads running at the same time. Has
 * a async notification mechanism.
 */

public class ProcessPool {

	public interface ProcessListener {
		public void queued(Runnable r);

		public void executing(Runnable r);

		public void complete(Runnable r);
	}

	private final class RunnableWrapper implements Runnable {
		private Runnable myRunnable;

		public RunnableWrapper(Runnable r) {
			myRunnable = r;
		}

		public void run() {
			markExecuting(myRunnable);
			try {
				myRunnable.run();
			} catch (Throwable ex) {
				myLog.log(Level.SEVERE,
						ex.getClass().getName() + " : " + ex.getMessage(), ex);
			} finally {
				markFinished(myRunnable);
			}
		}
	}

	private static Logger myLog = Logger.getLogger("ProcessPool");

	private int maxProcesses;
	private List<Runnable> myQueueRunnables;
	private List<ProcessListener> myQueueCallbacks;
	private Map<Runnable, ProcessListener> myActiveProcesses;

	public ProcessPool(int max) {
		maxProcesses = max;
		myQueueCallbacks = new ArrayList<>();
		myQueueRunnables = new ArrayList<>();
		myActiveProcesses = new HashMap<>();
	}

	public synchronized void execute(Runnable process, ProcessListener callback) {
		myQueueRunnables.add(process);
		myQueueCallbacks.add(callback);
		if (callback != null)
			callback.queued(process);
		if (process instanceof ProcessListener)
			((ProcessListener) process).queued(process);
		if (myActiveProcesses.size() < maxProcesses) {
			launchNext();
		}
	}

	synchronized void launchNext() {
		if (myQueueRunnables.size() == 0)
			return;
		ProcessListener lst = myQueueCallbacks.get(0);
		Runnable r = myQueueRunnables.get(0);
		myQueueCallbacks.remove(0);
		myQueueRunnables.remove(0);
		myLog.fine("Starting new Process.");
		Thread t = new Thread(new RunnableWrapper(r));
		t.setPriority(Thread.MIN_PRIORITY);
		t.setDaemon(true);
		myActiveProcesses.put(r, lst);
		t.start();
	}

	synchronized void markExecuting(Runnable r) {
		try {
			ProcessListener pr = myActiveProcesses.get(r);
			if (pr != null)
				pr.executing(r);
			if (r instanceof ProcessListener) {
				((ProcessListener) r).executing(r);
			}
		} catch (Exception ex) {
			myLog.log(Level.WARNING, "Notification failed (Executing).", ex);
		}
	}

	synchronized void markFinished(Runnable r) {
		try {
			ProcessListener pr = myActiveProcesses.get(r);
			if (pr != null)
				pr.complete(r);
			if (r instanceof ProcessListener) {
				((ProcessListener) r).complete(r);
			}
		} catch (Exception ex) {
			myLog.log(Level.WARNING,
					"Notification failed (Finished) : " + ex.getMessage(), ex);
		}
		myActiveProcesses.remove(r);
		myLog.fine("Process finished.");
		if (myActiveProcesses.size() < maxProcesses) {
			launchNext();
		}
	}

}
