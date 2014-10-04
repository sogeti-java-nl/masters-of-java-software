package nl.moj.round;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctrlaltdev.io.OutputRedirector;
import nl.moj.client.anim.Anim;
import nl.moj.model.Assignment;
import nl.moj.model.Clock;
import nl.moj.model.GameRules;
import nl.moj.model.Operation;
import nl.moj.model.Round;
import nl.moj.model.State;
import nl.moj.model.Team;
import nl.moj.model.Tester;
import nl.moj.model.Workspace;
import nl.moj.operation.ContextImpl;
import nl.moj.process.ProcessPool;
import nl.moj.sfx.SoundEffects;
import nl.moj.workspace.factory.WorkspaceFactory;

/**
 *
 */
public class TeamImpl implements Team, ProcessPool.ProcessListener, OutputRedirector.Target, Clock.Notification {

	private static long OPERATION_TIMEOUT = 45000L;

	private String myName;
	private String myPWD;
	private String myDisplayName;

	private boolean[] mySubmitTestResults;
	private int[] myLatestTestResults;
	private Anim[] myLatestAnimationTestResults;
	private int[] myOldTestResults;
	private int myUploadTime;

	private Logger myLog;
	private List<String[]> myLines;

	private StringBuffer sb = new StringBuffer();
	private State.Writer myStateWriter;

	private Workspace theWorkspace;
	private Assignment myAssignment;
	private SoundEffects sfx;
	private Clock myClock;
	private GameRules gameRules;

	private boolean online;
	private boolean disqualified;
	private long myCurrentOperationStartTime;
	private Operation myCurrentOperation;

	public TeamImpl(String name, String displayName, String pwd, Assignment a, GameRules rules, State.Writer wr, SoundEffects sfx) {
		if (name == null)
			throw new NullPointerException("Team name is null.");
		myName = name;
		myDisplayName = displayName;
		myPWD = pwd;
		myUploadTime = 0;
		myLog = Logger.getLogger(myName);
		myLines = new ArrayList<String[]>();
		myStateWriter = wr;
		myAssignment = a;
		this.gameRules = rules;
		myClock = rules.getClock(this);
		myClock.addNotifier(this);
		this.sfx = sfx;
	}

	public void setWorkspace(Workspace sp) {
		if (theWorkspace == null)
			theWorkspace = sp;
	}

	public void initWorkspace(WorkspaceFactory wf) throws IOException {
		setWorkspace(wf.createWorkspace(myName, this, this));
	}

	public String getName() {
		return myName;
	}

	public String getDisplayName() {
		return myDisplayName;
	}

	public boolean isScoreAvailable() {
		return ((isFinished()) && (mySubmitTestResults != null));
	}

	public boolean[] getSubmitTestResults() {
		if (mySubmitTestResults == null)
			return null;
		boolean[] copy = new boolean[mySubmitTestResults.length];
		for (int t = 0; t < mySubmitTestResults.length; t++) {
			copy[t] = mySubmitTestResults[t];
		}
		return copy;
	}

	public int getSubmitTime() {
		return myUploadTime;
	}

	public Workspace getWorkspace() {
		if (theWorkspace == null)
			throw new NullPointerException("Workspace not set.");
		return theWorkspace;
	}

	public Assignment getAssignment() {
		return myAssignment;
	}

	public Clock getClock() {
		return myClock;
	}

	public GameRules getGameRules() {
		return gameRules;
	}

	public synchronized int[] getTestResults() {
		if (myLatestTestResults == null)
			return null;
		int[] tmp = myLatestTestResults;
		myLatestTestResults = null;
		return tmp;
	}

	public synchronized Anim[] getAnimatedTestResults() {
		if (myLatestAnimationTestResults == null)
			return null;
		Anim[] tmp = myLatestAnimationTestResults;
		myLatestAnimationTestResults = null;
		return tmp;
	}

	public int[] getOldTestResults() {
		return myOldTestResults;
	}

	public void clearOldResults() {
		myOldTestResults = null;
	}

	public boolean canDisqualify() {
		return (!isDisqualified()) && (isPlaying());
	}

	public boolean isDisqualified() {
		return disqualified;
	}

	public boolean isWaiting() {
		return gameRules.getState(this) == GameRules.STATE_WAITING;
	}

	public boolean isFinished() {
		return gameRules.getState(this) == GameRules.STATE_FINISHED;
	}

	public boolean isPlaying() {
		return gameRules.getState(this) == GameRules.STATE_PLAYING;
	}

	public boolean isValidPassword(String pwd) {
		boolean ok = myPWD.equals(pwd);
		if (ok)
			online = true;
		if (pwd == null)
			online = false;
		return ok;
	}

	public boolean isOnline() {
		return online;
	}

	public int getFinalScore() {
		return gameRules.getFinalScore(this);
	}

	public int getTheoreticalScore() {
		return gameRules.getTheoreticalMaximumScore(this);
	}

	public int getSecondsInitial() {
		return myClock.getDuration();
	}

	public int getSecondsRemaining() {
		return myClock.getSecondsRemaining();
	}

	public synchronized boolean isPerformingOperation() {
		return (myCurrentOperation != null);
	}

	public synchronized Operation getCurrentOperation() {
		return myCurrentOperation;
	}

	public synchronized void doOperation(Operation op, Operation.Context ctx) {
		if (isPlaying() && (!isPerformingOperation())) {
			myLog.info("Performing : " + op.getName());
			if (op.isSubmit()) {
				markSubmitting();
			}
			getWorkspace().perform(op, ctx);
			myCurrentOperationStartTime = System.currentTimeMillis();
			myCurrentOperation = op;
		} else if (isPlaying()) {
			addLine(null, "Still busy.");
			if (System.currentTimeMillis() - myCurrentOperationStartTime > OPERATION_TIMEOUT) {
				String msg = "Operation took longer than " + OPERATION_TIMEOUT / 1000L + " seconds. Aborting.";
				addLine(null, msg);
				myLog.warning(msg);
				complete(new Runnable() {
					public void run() {
					}
				});
			}
		}
	}
	
	public synchronized void doOperation(Operation op, Operation.Context[] ctx) {
		if (isPlaying() && (!isPerformingOperation())) {
			myLog.info("Performing : " + op.getName());
			if (op.isSubmit()) {
				markSubmitting();
			}
			for(int i = 0; i < ctx.length; i++) {
				getWorkspace().perform(op, ctx[i]);
			}
			myCurrentOperationStartTime = System.currentTimeMillis();
			myCurrentOperation = op;
		} else if (isPlaying()) {
			addLine(null, "Still busy.");
			if (System.currentTimeMillis() - myCurrentOperationStartTime > OPERATION_TIMEOUT) {
				String msg = "Operation took longer than " + OPERATION_TIMEOUT / 1000L + " seconds. Aborting.";
				addLine(null, msg);
				myLog.warning(msg);
				complete(new Runnable() {
					public void run() {
					}
				});
			}
		}
	}

	//
	// Console Redirection
	//
	public OutputRedirector.Target getTarget() {
		return this;
	}

	/** OutputRedirector.Target implementation */
	public void append(String ctx, String s) {
		sb.append(s);
		if (sb.length() > 1024)
			sb.append("\n");
		int idx = -1;
		while ((idx = sb.indexOf("\n")) >= 0) {
			String sub = sb.substring(0, idx);
			addLine(ctx, sub);
			sb.delete(0, idx + 1);
		}
	}

	//
	// Callbacks from operations
	//
	public void complete(Runnable r) {
		//
		// Store Test Results
		//
		if (r instanceof Results) {
			Operation op = ((Results) r).getOperation();
			boolean submit = op.isSubmit();
			//
			if (r instanceof TestResults) {
				TestResults tr = (TestResults) r;
				if (tr.getTestResults() == null) {
					//
					// NULL tests results indicate a test that timed out
					// or raised an exception.
					//
					if (submit) {
						// Failed Submission.
						markFailed(new boolean[] { false });
					} else {
						// Failed Test.
						logEndTest(new int[] { Tester.TestResult.FAIL }, null);
					}
				} else {
					//
					// Valid test results indicate the unit tests ran as
					// expected.
					// We now need to check if the results are all good.
					//
					if (submit) {
						// Successful Submit
						if (tr.getTestResults().isOk()) {
							markSuccess(tr.getTestResults().getScore());
						} else {
							markFailed(tr.getTestResults().getScore());
						}
					} else {
						// Successful Test
						logEndTest(tr.getTestResults().getResults(), tr.getTestResults().getAnimationOutput());
					}
				}
			} else if (r instanceof CompileResults) {
				//
				// Store Compilation Results.
				//
				addCompileSuccess(((CompileResults) r).wasSuccess());
				//
				if (submit) {
					// Submission with compile error.
					markFailed(new boolean[] { false });
				}
			} else {
				//
				// Dunno what it was, but if it is a submit its wrong.
				//
				if (submit) {
					markFailed(new boolean[] { false });
				}
			}
		}
		//
		// Clear current Operation.
		//
		myCurrentOperation = null;
		//
		addLine(null, "Complete.");
	}

	public void executing(Runnable r) {
		addLine(null, "Running.");
	}

	public void queued(Runnable r) {
		addLine(null, "Queued.");
		// Moved to doOperation because of timing inconsistency when running on
		// workspace server.
		//
		// if (r instanceof Team.TestResults) {
		// Team.TestResults tr=(Team.TestResults)r;
		// if (tr.getOperation().isSubmit()) {
		// markSubmitting();
		// }
		// } else if (r instanceof RemoteWorkspaceClient.ProcessResults) {
		// Operation
		// op=((RemoteWorkspaceClient.ProcessResults)r).getOperation();
		// if (op.isSubmit()) {
		// markSubmitting();
		// }
		// }
	}

	//
	// package interface.
	//

	public static final int MAX_LINES = 1500;

	/** adds a line to send to the client console */
	protected synchronized void addLine(String ctx, String line) {
		if (myLines.size() > MAX_LINES)
			return;
		if (myLines.size() == MAX_LINES) {
			Logger.getLogger(getName()).warning("Team '" + getName() + "' is generating lots of output.");
		}
		myLines.add(new String[] { ctx, line });
	}

	/** adds a number of lines to send to the client console */
	protected synchronized void addLines(String ctx, String[] lines) {
		if (myLines.size() + lines.length > MAX_LINES)
			return;
		for (int t = 0; t < lines.length; t++) {
			myLines.add(new String[] { ctx, lines[t] });
		}
	}

	/** gets and clears the lines */
	public synchronized String[][] getLines() {
		String[][] result = myLines.toArray(new String[myLines.size()][]);
		myLines.clear();
		return result;
	}

	protected void markSubmitting() {
		if (isPlaying()) {
			if (myUploadTime == 0) {
				myUploadTime = myClock.getSecondsPassed();
				myStateWriter.submit(this, myUploadTime);
			}
		}
	}

	protected boolean hasSubmitted() {
		return myUploadTime != 0;
	}

	protected void markSuccess(boolean[] r) {
		if (mySubmitTestResults == null) {
			mySubmitTestResults = r;
			myStateWriter.score(this);
			if (sfx != null)
				sfx.submitSuccess();
		}
	}

	protected void markFailed(boolean[] r) {
		if (mySubmitTestResults == null) {
			mySubmitTestResults = r;
			myStateWriter.score(this);
			if (sfx != null)
				sfx.submitFailed();
		}
	}

	protected synchronized void logEndTest(int[] testResults, Anim[] animResults) {
		myLatestTestResults = testResults;
		myLatestAnimationTestResults = animResults;
		myOldTestResults = testResults;
		for (int t = 0; t < testResults.length; t++) {
			if (testResults[t] < 0)
				myStateWriter.testFailure(this);
			if (testResults[t] > 0)
				myStateWriter.testSuccess(this);
		}
	}

	public void disqualify(Object caller) {
		if (canDisqualify()) {
			if (!EventQueue.isDispatchThread()) {
				throw new RuntimeException("Cannot Disqualify. Reason : not on Dispatch Thread.");
			}
			disqualified = true;
			myStateWriter.submit(this, myClock.getSecondsPassed());
			myStateWriter.score(this);
		}
	}

	public void load(Round r, State s) {
		if (s.isComplete(r, this)) {
			int score = s.getScore(r, this);
			if (score < 0) {
				//
				// The code was submitted, but the engine terminated
				// before any results could be written. Resumbit.
				//
				myUploadTime = s.getSubmitTime(r, this);
				Operation[] o = myAssignment.getOperations();
				for (int t = 0; t < o.length; t++) {
					if (o[t].isSubmit()) {
						getWorkspace().perform(o[t], new ContextImpl());
						return;
					}
				}
			} else if (score == 0) {
				mySubmitTestResults = new boolean[] { false };
			} else {
				myUploadTime = s.getSubmitTime(r, this);
				mySubmitTestResults = new boolean[] { true };
			}
		}
	}

	//
	// Stats
	//

	public void addStatistics(int keyStrokes, String operation, int size) {
		myStateWriter.keyStrokes(this, keyStrokes);
		myStateWriter.operation(this, operation);
		myStateWriter.solutionSize(this, size);
	}

	private void addCompileSuccess(boolean ok) {
		if (ok) {
			myStateWriter.compileSuccess(this);
		} else {
			myStateWriter.compileFailure(this);
		}
	}

	//
	// Clock Notification
	//

	public void clockReset() {
	}

	public void clockFinished() {
		if (!isScoreAvailable() && (!hasSubmitted())) {
			myLog.log(Level.INFO, "TIMEUP : score=" + getFinalScore());
			myStateWriter.timeRemaining(this, 0);
			myStateWriter.finish(this);
			myStateWriter.submit(this, myClock.getSecondsPassed());
			myStateWriter.score(this);
		} else {
			myStateWriter.finish(this);
		}
	}

	public void clockStarted() {
		if (myClock.isStartPosition()) {
			myStateWriter.start(this);
		} else {
			myStateWriter.resume(this);
		}
	}

	public void clockStopped() {
		myStateWriter.pause(this);
	}

	public void minutePassed(int remaining) {
		myStateWriter.timeRemaining(this, remaining * 60);
	}

}