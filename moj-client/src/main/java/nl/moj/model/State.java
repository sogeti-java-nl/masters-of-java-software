package nl.moj.model;

import java.io.IOException;

/**
 * Interface for managing state between rounds and also a score board between rounds. 
 */
public interface State {
	
	public interface Writer {	
			
		public void open(Team team);
		public void start(Team team);
		public void pause(Team team);
		public void resume(Team team);
		public void finish(Team team);
		
		public void close(Team team);
		public void timeRemaining(Team team,int time);
		
		public void submit(Team team,int time);
		public void score(Team team);
		
		public void keyStrokes(Team team,int strokes);
		public void operation(Team team,String name); 
		public void solutionSize(Team team,int size);
		
		public void compileSuccess(Team team);
		public void compileFailure(Team team);

		public void testSuccess(Team team);
		public void testFailure(Team team);
		
		public void workspaceHost(String workspaceName,String host,int port);
		
	}
	
	//
	// Interface for use with real objects.
	// 
	
	/** @return true if the specified round was started previously */
	public boolean isStarted(Round rnd,Team team);
	/** @return the number of seconds remaining for the round. If the round was finished this method returns 0 */
	public int     getTimeRemaining(Round rnd,Team team); 
	/** @return true if the round was finished (i.e. time ran out or the team submitted) */
	public boolean isFinished(Round rnd,Team team);
	/** @return true if the team has completed this round. */
	public boolean isComplete(Round rnd,Team team);
	/** @return the score for the team in the specified round if the round was finished. Otherwise returns 0. */
	public int     getScore(Round rnd,Team team);
	/** @return the time of submission of the solution of the specified round/team */ 
	public int 	   getSubmitTime(Round rnd,Team team);	
	
	//
	// Interface for use with names of objects.
	//
	
	/** @return the round names stored in the State implementation */
	public String[] getKnownRoundNames();
	/** @return the team names stored for which State information is available */
	public String[] getKnownTeams();
	
	public String getRoundWinner(String round);
	/** @return the score for the team in the specified round */
	public int getScore(String round,String team);
	/** @return the number of keystrokes of the team in that round */
    public int getNrOfKeystrokes(String round, String team);
	/**
	 * calculates the ratio between the number of keystrokes and file size.
	 * @return the ratio between the number of keystrokes and file size.
	 */
	public float getKeyStrokeFileSizeRatio(String round,String team);
	/** return the names of all the performed operations by this team */
	public String[] getOperationNames(String round,String team);
	/** return the invocation count of the specified operation */
	public int   getOperationCount(String round,String team,String op);
	
	/** returns the number of compile failures for the specified round and team */
	public int   getCompileFailures(String round,String team);
	/** returns the number of compile successes for the specified round and team */
	public int   getCompileSuccess(String round,String team);
	/** returns the number of test failures for the specified round and team */
	public int   getTestFailures(String round,String team);
	/** returns the number of test successes for the specified round and team */
	public int   getTestSuccess(String round,String team);
	
	/** returns the file size for the specified round, team and minute. */
	public int   getFileSizeForMinute(String round,String team,int minute);
	/** returns the last file size for the specified round, team */
	public int   getFinalFileSize(String round,String team);
	
	/** saves the added lines */
	public void flush() throws IOException;
	/** saves the added lines and closes the file. */
	public void closeFile() throws IOException;
	
	public String getLastHost(String workspaceName);
	public int    getLastPort(String workspaceName);
	
}
