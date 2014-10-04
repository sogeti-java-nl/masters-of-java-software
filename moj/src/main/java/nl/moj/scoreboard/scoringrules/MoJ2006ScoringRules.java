package nl.moj.scoreboard.scoringrules;

import nl.moj.gamerules.MoJ2006CompetitionGameRules;
import nl.moj.scoreboard.ScoringRules;

public class MoJ2006ScoringRules implements ScoringRules {

	public double[] assignScores(int[] times) {
		double[] result=new double[times.length];
		for (int t=0;t<times.length;t++) {
			result[t]=times[t];
			if (result[t]<0) result[t]=0;
			if (result[t]>getMaxScorePerRound()) result[t]=getMaxScorePerRound();
		}
		return result;
	}
	public double getMaxScorePerRound() {
		return MoJ2006CompetitionGameRules.SUCCESS_BASE_SCORE+1800;
	}	
	
}
