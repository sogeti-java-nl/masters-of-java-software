package nl.moj.model;

/**
 * The GameRule interface describes the rules of a Round of MoJ. It allows
 * custom scoring systems and alternative timing of a round. Each game must
 * follow a simple state diagram :
 * <code>
 *   WAITING  -->  PLAYING  -->  FINISHED
 *            ---------------->
 * </code>
 * The moments of transition from one state to another can be determined by
 * the implementation of the GameRules interface.
 * @see nl.moj.gamerules.CompetitionGameRules
 * @author E.Hooijmeijer
 */

public interface GameRules {

	/** 
	 * STATE_WAITING is the state in which players are waiting for each 
	 * other for a synchronous game. Also the initial state of a Team.
	 */
	public static final int STATE_WAITING=0;
	/**
	 * STATE_PLAYING follows STATE_WAITING and indicates the Team is playing the game.
	 */
	public static final int STATE_PLAYING=1;
	/**
	 * STATE_FINISHED follows STATE_PLAYING or STATE_WAITING and indicates either
	 * the Team has sumbitted a solution OR the time has ran out. 
	 */
	public static final int STATE_FINISHED=2;
	
	/**
	 * returns the Clock for the specified Team. This may be a shared clock or an
	 * individual one.
	 * @param t the team to get the clock for.
	 * @return the clock for the specified Team.
	 */
	public Clock getClock(Team t);
	/**
	 * calculates the theoretical maximum score which could be scored if the Team
	 * submitted now and got all answers right.
	 * @param t the Team to calculate the score for.
	 * @return the number of points.
	 */
	public int getTheoreticalMaximumScore(Team t);
	
	/**
	 * calculates the score which based on the concrete anwers of the specified Team.
	 * submitted now and got all answers right.
	 * @param t the Team to calculate the score for.
	 * @return the number of points.
	 */
	public int getFinalScore(Team t);
	/**
	 * returns the current state of the specified Team.
	 * @param t the team to get the state for.
	 * @return one of the state constants (STATE_WAITING,STATE_PLAYING,STATE_FINISHED)
	 */
	public int getState(Team t);	

	/**
	 * loads the state for the specified Team. 
	 */
	public void load(Round rnd,Team t,State st);
	
	/**
	 * starts the clock for the specified (or all) team.
	 * @param tm the team.
	 */
	public void start(Team tm);
}
