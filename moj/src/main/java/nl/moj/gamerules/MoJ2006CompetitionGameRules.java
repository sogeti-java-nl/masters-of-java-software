package nl.moj.gamerules;

import nl.moj.model.Clock;
import nl.moj.model.GameRules;
import nl.moj.model.Team;

/**
 * Adds 400 points bonus if submitted succesfully so that the
 * difference between people who submitted successfully at the last
 * moment and those who submitted nothing gets bigger.
 */

public class MoJ2006CompetitionGameRules extends CompetitionGameRules implements GameRules {

	public static final int SUCCESS_BASE_SCORE=400;
	
	public MoJ2006CompetitionGameRules(Clock clock) {
		super(clock);
	}
	
	public int getTheoreticalMaximumScore(Team tm) {
		int r=super.getTheoreticalMaximumScore(tm);
		if (r==0) return 0;
		return SUCCESS_BASE_SCORE+r;
	}
	
	public int getFinalScore(Team tm) {
		int score=super.getFinalScore(tm);
		if (score==0) return 0;
		return SUCCESS_BASE_SCORE+score;
	}
	
}
