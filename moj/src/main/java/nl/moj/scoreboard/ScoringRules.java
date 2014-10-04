package nl.moj.scoreboard;

/**
 * ScoringRules desribes the conversion from timeRemaining to actual scores.
 * This allows for some flexability in the score graphing.
 *
 * @author E.Hooijmeijer
 */

public interface ScoringRules {

	public double getMaxScorePerRound(); 
	/** maps times to scores. The score must be between 0..MaxScorePerRound */
	public double[] assignScores(int[] times);
	
}
