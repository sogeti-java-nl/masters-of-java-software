package nl.ctrlaltdev.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * OutputRedirector : wraps the OutputStreams. Redirects all output to the
 * original outputstreams or to a specific Redirector for a specific Thread.
 * 
 * @author E.Hooijmeijer / (C) 2003-2004 E.Hooijmeijer / Licence : LGPL 2.1
 */
public class OutputRedirector {

	/** implement this to capture your own output */
	public interface Target {
		public void append(String context, String s);
	}

	/**
	 * Wraps around a Target to make it synchronized.
	 */
	public static class SynchronizedTarget implements Target {
		private Target t;

		public SynchronizedTarget(Target t) {
			this.t = t;
		}

		public synchronized void append(String context, String s) {
			t.append(context, s);
		}
	}

	/** Do nothing implementation of Outputstream. */
	public static class DummyOutputStream extends OutputStream {
		public void write(byte[] b) throws IOException {
		}

		public void write(int b) throws IOException {
		}

		public void write(byte[] b, int off, int len) throws IOException {
		}
	}

	/**
	 * used to redirect the output of the compiler.
	 */
	public static class RedirectorPrintWriter extends PrintWriter {
		private Target myRDR;
		private String context;

		public RedirectorPrintWriter(Target rdr) {
			super(new DummyOutputStream());
			myRDR = rdr;
		}

		public void setContext(String ctx) {
			this.context = ctx;
		}

		public void print(boolean b) {
			myRDR.append(context, b ? "true" : "false");
		}

		public void print(char b) {
			myRDR.append(context, String.valueOf(b));
		}

		public void print(char[] b) {
			myRDR.append(context, new String(b));
		}

		public void print(double b) {
			myRDR.append(context, String.valueOf(b));
		}

		public void print(float b) {
			myRDR.append(context, String.valueOf(b));
		}

		public void print(int b) {
			myRDR.append(context, String.valueOf(b));
		}

		public void print(long b) {
			myRDR.append(context, String.valueOf(b));
		}

		public void print(Object b) {
			myRDR.append(context, String.valueOf(b));
		}

		public void print(String b) {
			myRDR.append(context, String.valueOf(b));
		}

		public void println() {
			myRDR.append(context, "\n");
		}

		public void println(boolean x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(char x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(char[] x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(double x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(float x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(int x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(long x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(Object x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(String x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void write(char[] buf) {
			myRDR.append(context, new String(buf));
		}

		public void write(String s) {
			myRDR.append(context, s);
		}

		public void write(int c) {
			myRDR.append(context, String.valueOf((char) c));
		}

		public void write(String s, int off, int len) {
			myRDR.append(context, s.substring(off, off + len));
		}

		public void write(char[] s, int off, int len) {
			myRDR.append(context, new String(s).substring(off, off + len));
		}
	}

	/**
	 * this PrintStream redirects all output to the Target. It is used to make
	 * sure the output of stdout and stderr of specific threads get to the
	 * Target.
	 */
	public static class RedirectorPrintStream extends PrintStream {
		private Target myRDR;
		private String context;

		public RedirectorPrintStream(Target rdr) {
			super(new DummyOutputStream());
			myRDR = new SynchronizedTarget(rdr);
		}

		public RedirectorPrintStream copy() {
			RedirectorPrintStream r = new RedirectorPrintStream(null);
			r.myRDR = this.myRDR; // Use the same (synchronized) target.
			r.setContext(this.getContext());
			return r;
		}

		public void setContext(String ctx) {
			this.context = ctx;
		}

		public String getContext() {
			return this.context;
		}

		public void print(boolean b) {
			myRDR.append(context, b ? "true" : "false");
		}

		public void print(char b) {
			myRDR.append(context, String.valueOf(b));
		}

		public void print(char[] b) {
			myRDR.append(context, new String(b));
		}

		public void print(double b) {
			myRDR.append(context, String.valueOf(b));
		}

		public void print(float b) {
			myRDR.append(context, String.valueOf(b));
		}

		public void print(int b) {
			myRDR.append(context, String.valueOf(b));
		}

		public void print(long b) {
			myRDR.append(context, String.valueOf(b));
		}

		public void print(Object b) {
			myRDR.append(context, String.valueOf(b));
		}

		public void print(String b) {
			myRDR.append(context, String.valueOf(b));
		}

		public void println() {
			myRDR.append(context, "\n");
		}

		public void println(boolean x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(char x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(char[] x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(double x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(float x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(int x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(long x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(Object x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}

		public void println(String x) {
			myRDR.append(context, String.valueOf(x));
			myRDR.append(context, "\n");
		}
	}

	/**
	 * This printstream switches between the default target and specific
	 * Redirectors per thread.
	 */
	public static class PrintStreamWrapper extends PrintStream {
		private PrintStream trg;
		private Map<Thread, RedirectorPrintStream> myThreads;

		public PrintStreamWrapper(PrintStream target) {
			super(new DummyOutputStream());
			myThreads = new HashMap<Thread, RedirectorPrintStream>();
			if (target == null)
				throw new NullPointerException("Need Target PrintStream.");
			trg = target;
		}

		public void setContext(String ctx) {
			PrintStream prt = redirect();
			if (prt instanceof RedirectorPrintStream) {
				((RedirectorPrintStream) prt).setContext(ctx);
			} else {
				// Redirection was canceled before this thread could set the
				// context.
			}
		}

		public boolean checkError() {
			return trg.checkError();
		}

		public void close() {
			trg.close();
		}

		public void flush() {
			trg.flush();
		}

		public void print(boolean b) {
			redirect().print(b);
		}

		public void print(char b) {
			redirect().print(b);
		}

		public void print(char[] b) {
			redirect().print(b);
		}

		public void print(double b) {
			redirect().print(b);
		}

		public void print(float b) {
			redirect().print(b);
		}

		public void print(int b) {
			redirect().print(b);
		}

		public void print(long b) {
			redirect().print(b);
		}

		public void print(Object b) {
			redirect().print(b);
		}

		public void print(String b) {
			redirect().print(b);
		}

		public void println() {
			redirect().println();
		}

		public void println(boolean x) {
			redirect().println(x);
		}

		public void println(char x) {
			redirect().println(x);
		}

		public void println(char[] x) {
			redirect().println(x);
		}

		public void println(double x) {
			redirect().println(x);
		}

		public void println(float x) {
			redirect().println(x);
		}

		public void println(int x) {
			redirect().println(x);
		}

		public void println(long x) {
			redirect().println(x);
		}

		public void println(Object x) {
			redirect().println(x);
		}

		public void println(String x) {
			redirect().println(x);
		}

		public void write(byte[] b) throws IOException {
			trg.write(b);
		}

		public void write(byte[] buf, int off, int len) {
			trg.write(buf, off, len);
		}

		public void write(int b) {
			trg.write(b);
		}

		//
		public synchronized void addThread(Thread t, Target redir) {
			myThreads.put(t, new RedirectorPrintStream(redir));
		}

		/**
		 * redirects the output of the newThread to the same target as the
		 * existing thread. If the target thread is not redirected nothing
		 * happens.
		 * 
		 * @param newThread
		 *            the new thread.
		 * @param existing
		 *            an existing redirected thread.
		 */
		public synchronized void addThread(Thread newThread, Thread existing) {
			RedirectorPrintStream rdr = myThreads.get(existing);
			if (rdr == null)
				return;
			myThreads.put(newThread, rdr.copy());
		}

		public synchronized void removeThread(Thread t) {
			myThreads.remove(t);
		}

		protected synchronized PrintStream redirect() {
			RedirectorPrintStream rdr = myThreads.get(Thread.currentThread());
			if (rdr == null)
				return trg;
			return rdr;
		}

		public synchronized void removeTerminated() {
			Thread[] tr = myThreads.keySet().toArray(
					new Thread[myThreads.size()]);
			for (int t = 0; t < tr.length; t++) {
				if (!tr[t].isAlive())
					myThreads.remove(tr[t]);
			}
		}
	}

	private PrintStreamWrapper stdOut, stdErr;

	/**
	 * Wraps around System.out and System.err.
	 * 
	 * @throws RuntimeException
	 *             if the streams are already wrapped.
	 */
	private OutputRedirector() {
		if (isApplied())
			throw new RuntimeException("Output Redirection is already applied.");
		apply();
	}

	private boolean isApplied() {
		return ((System.out instanceof PrintStreamWrapper) && (System.err instanceof PrintStreamWrapper));
	}

	private void apply() {
		stdOut = new PrintStreamWrapper(System.out);
		stdErr = new PrintStreamWrapper(System.err);
		System.setOut(stdOut);
		System.setErr(stdErr);
	}

	public void setContext(String ctx) {
		stdErr.setContext(ctx);
		stdOut.setContext(ctx);
	}

	/**
	 * redirects the output generated by the specified Thread to the specified
	 * Target. Note : In order to remove the Thread from the list you should
	 * also cancel it !
	 */
	public void redirect(Thread t, Target rdr) {
		stdOut.addThread(t, rdr);
		stdErr.addThread(t, rdr);
	}

	/**
	 * redirects to the same target as the other (already redirected) thread.
	 * 
	 * @param newThread
	 *            the new thread that must have its output redirected.
	 * @param alreadyRedirectedThread
	 *            an already redirected thread.
	 */
	public void redirectToSame(Thread newThread, Thread alreadyRedirectedThread) {
		stdOut.addThread(newThread, alreadyRedirectedThread);
		stdErr.addThread(newThread, alreadyRedirectedThread);
	}

	/**
	 * Cancels output redirection for the thread.
	 */
	public void cancel(Thread t) {
		stdOut.removeThread(t);
		stdErr.removeThread(t);
	}

	/** removes all threads from the redirection list who are not alive. */
	public void removeTerminated() {
		stdErr.removeTerminated();
		stdOut.removeTerminated();
	}

	private static OutputRedirector red;

	public static synchronized OutputRedirector getSingleton() {
		if (red == null)
			red = new OutputRedirector();
		// Maven SureFire messes up the redirection...
		if (!red.isApplied())
			red.apply();
		return red;
	}

}
