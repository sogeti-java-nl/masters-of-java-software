package nl.moj.scoreboard.scoringrules;

import nl.moj.scoreboard.ScoringRules;

/**
 * Calculates the score based on the time remaining. 
 * @author E.Hooijmeijer
 */

public class MoJ2005ScoringRules implements ScoringRules {

	private static final double[] SCORES=new double[] { 20,18,16,14,12,10,1 };
	
	public double getMaxScorePerRound() {
		return SCORES[0];
	}	
	
	public double[] assignScores(int[] times) {
		double[] result=new double[times.length];
		int[] teams=new int[times.length];
		for (int t=0;t<teams.length;t++) teams[t]=t;
		//
		// BubbleSort : Yech !
		//
		for (int t=0;t<times.length;t++) {
			for (int y=t;y<times.length;y++) {
				if (times[t]<times[y]) {
					int tmp=times[t];times[t]=times[y];times[y]=tmp;
					tmp=teams[t];teams[t]=teams[y];teams[y]=tmp;
				}
			}
		}
		//
		double[] tmp=new double[result.length];
		//
		int teamIdx=0;
		int scoreIndex=0;
		while (teamIdx<teams.length) {
			//
			// No submit -> No points.
			//
			if (times[teamIdx]<=0) break;
			//
			// Average the points for teams that have identical scores.			
			//
			double sum=0;
			int tmpIdx=teamIdx;
			//
			do  {
				sum+=SCORES[scoreIndex>=SCORES.length?SCORES.length-1:scoreIndex];
				scoreIndex++;
				tmpIdx++;
			} while ((!(tmpIdx>=times.length))&&(times[tmpIdx-1]==times[tmpIdx]));
			//
			double avg=sum/(tmpIdx-teamIdx);
			for (int t=teamIdx;t<tmpIdx;t++) {
				tmp[t]=avg;
			}			
			//
			teamIdx=tmpIdx;
		}
		//
		// Copy the scores to the correct (de-sorted) index.
		//
		for (int t=0;t<tmp.length;t++) {
			result[teams[t]]=tmp[t];
		}
		//
		return result;
	}

	public static final int[] dumpInt(int[] a) {
		for (int t=0;t<a.length;t++) {
			if (t>0) System.out.print(",");
			System.out.print(a[t]);
		}
		System.out.println();
		return a;
	}

	public static final double[] dumpDouble(double[] a) {
		for (int t=0;t<a.length;t++) {
			if (t>0) System.out.print(",");
			System.out.print(a[t]);
		}
		System.out.println();
		return a;
	}
	
	public static void main(String[] args) {
		dumpDouble(new MoJ2005ScoringRules().assignScores(dumpInt(new int[] { 1412,1211,800,600,500,400,300,200,100,87,24,12,9 })));
		dumpDouble(new MoJ2005ScoringRules().assignScores(dumpInt(new int[] { 10,10,10,9,8,7 })));
		dumpDouble(new MoJ2005ScoringRules().assignScores(dumpInt(new int[] { 10,0 })));
		dumpDouble(new MoJ2005ScoringRules().assignScores(dumpInt(new int[] { 0,0,0,0,0 })));
	}
	
}
