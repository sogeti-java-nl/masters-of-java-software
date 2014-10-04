package nl.moj.model;

import nl.ctrlaltdev.io.OutputRedirector;
import nl.moj.client.anim.Anim;

/**
 *
 */
public interface Team {

	public interface Results {
		public Operation getOperation();		
	}
	
	public interface TestResults extends Results {
		public Tester.TestResult getTestResults();
	}

	public interface CompileResults extends Results {
		public boolean wasSuccess();
	}
		
	/** returns the real name of the Team */
	public String getName();
	/** returns the display name of the Team */
	public String getDisplayName();
	/** verifies the password of the team. Returns true if its a match */
	public boolean isValidPassword(String pwd);
	
	//
	// Various getters for TestResults
	//
	
	public int[]  getTestResults();
    public Anim[] getAnimatedTestResults();
	public int[]  getOldTestResults();
	public void   clearOldResults();
	
	//
	// Various getters for Submission Results.
	//
	
	public boolean[] getSubmitTestResults();
	public int getSubmitTime();
	
	//
	// Getters for score.
	//
	
	public int getTheoreticalScore();
	public int getFinalScore();
	
	/** 
	 * returns true if the team is logged in.
	 */ 
	public boolean isOnline();
	/**
	 * returns true if this team is disqualified. 
	 */
	public boolean isDisqualified();
	/**
	 * Disqualifies a team. 
	 * @param caller of this method.
	 */
	public void    disqualify(Object caller);
	
	public boolean isWaiting();
	public boolean isPlaying();
	public boolean isFinished();
	
	/** 
	 * returns true if the submission results are there or the clock has run out. 
	 */
	public boolean isScoreAvailable();
	
	/**
	 * returns true if the team is performing an operation.  
	 */
	public boolean isPerformingOperation();
	public Operation getCurrentOperation();

	/** returns the game rules this team plays by */
	public GameRules  getGameRules();
	/** returns the (read-only) clock of this team.*/
	public Clock      getClock();
	/** returns the assignment of this team. */
	public Assignment getAssignment();
	/** returns the workspace of this team */
	public Workspace  getWorkspace();
	
	/**
	 * Makes the team perform an operation such as save,compile,test or submit.
	 * @param op the operation to submit.
	 * @param ctx the operation context.
	 */
	public void doOperation(Operation op,Operation.Context ctx);
	public void doOperation(Operation op,Operation.Context[] ctx);

	//
	//
	//
	
	public String[][] getLines();	

	public OutputRedirector.Target getTarget();

	//
	//
	//
	
	public void load(Round rnd,State s);
	
	public void addStatistics(int keyStrokes,String operation,int size);
	 
}