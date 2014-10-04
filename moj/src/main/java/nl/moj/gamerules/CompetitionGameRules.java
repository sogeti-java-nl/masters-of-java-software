package nl.moj.gamerules;

import nl.moj.clock.ReadOnlyClock;
import nl.moj.model.Clock;
import nl.moj.model.GameRules;
import nl.moj.model.Round;
import nl.moj.model.State;
import nl.moj.model.Team;

/**
 * This is the default GameRules implementation with one clock for all teams.
 * @author E.Hooijmeijer
 */

public class CompetitionGameRules implements GameRules {

	private Clock readOnlyClock;
	private Clock rwClock;
	
	public CompetitionGameRules(Clock clock) {
		this.readOnlyClock=new ReadOnlyClock(clock);
		rwClock=clock;
	}
	
	public Clock getClock(Team t) {
		return readOnlyClock;
	}

	public int getTheoreticalMaximumScore(Team t) {
		Clock c=getClock(t);
		//
		int result=c.getDuration()-c.getSecondsPassed();
		if (result<0) result=0;
		//
		return result;
	}

	public int getFinalScore(Team tm) {
		if (tm.isDisqualified()) return 0;
		//
		boolean[] results=tm.getSubmitTestResults();
		//
		if (results==null) return 0;	
		//
		for (int t=0;t<results.length;t++) {
			if (!results[t]) return 0;
		}
		//
		return getClock(tm).getDuration()-tm.getSubmitTime();
	}

	public int getState(Team t) {
		Clock c=getClock(t);
		if (t.isDisqualified()) return GameRules.STATE_FINISHED;
		if (c.isFinished()) return GameRules.STATE_FINISHED;		
		if (t.getSubmitTestResults()!=null) return GameRules.STATE_FINISHED;
		if (c.isRunning()||(c.isStarted())) return GameRules.STATE_PLAYING;
		return GameRules.STATE_WAITING;
	}
	
	public void start(Team tm) {
		if (!rwClock.isStarted()) {
			rwClock.start();
		}
	}

	public void load(Round rnd,Team t,State st) {
		t.load(rnd,st);
		rwClock.load(rnd,t,st);
	}
	
}
