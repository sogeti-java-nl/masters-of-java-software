package nl.moj.gamerules;

import java.util.HashMap;
import java.util.Map;

import nl.moj.clock.ReadOnlyClock;
import nl.moj.clock.SimpleClock;
import nl.moj.model.Clock;
import nl.moj.model.GameRules;
import nl.moj.model.Round;
import nl.moj.model.State;
import nl.moj.model.Team;

/**
 * GameRules implementation without a shared clock which dicates when to start
 * and when to stop. So each team can start the assignment when they like and
 * without waiting for the other Teams.
 * 
 * @author E.Hooijmeijer
 */

public class IndividualGameRules implements GameRules {

	private Map<Team, ReadOnlyClock> clock;
	private Map<Team, Clock> rwClock;
	private int duration;

	public IndividualGameRules(int durationInMinutes) {
		//
		clock = new HashMap<>();
		rwClock = new HashMap<>();
		duration = durationInMinutes;
		//
	}

	public synchronized Clock getClock(Team t) {
		Clock c = (Clock) clock.get(t);
		if (c == null) {
			c = new SimpleClock(duration);
			rwClock.put(t, c);
			clock.put(t, new ReadOnlyClock(c));
		}
		return c;
	}

	private synchronized Clock getRWClock(Team t) {
		return rwClock.get(t);
	}

	public int getTheoreticalMaximumScore(Team t) {
		Clock c = getClock(t);
		//
		int result = c.getDuration() - c.getSecondsPassed();
		if (result < 0)
			result = 0;
		//
		return result;
	}

	public int getFinalScore(Team tm) {
		if (tm.isDisqualified())
			return 0;
		boolean[] results = tm.getSubmitTestResults();
		//
		if (results == null)
			return 0;
		//
		for (int t = 0; t < results.length; t++) {
			if (!results[t])
				return 0;
		}
		//
		return getClock(tm).getDuration() - tm.getSubmitTime();
	}

	public int getState(Team t) {
		Clock c = getClock(t);
		if (t.isDisqualified())
			return GameRules.STATE_FINISHED;
		if (c.isFinished())
			return GameRules.STATE_FINISHED;
		if (t.getSubmitTestResults() != null)
			return GameRules.STATE_FINISHED;
		if (c.isRunning()) {
			return GameRules.STATE_PLAYING;
		} else {
			// if the workspace is not yet ready, keep on waiting.
			if (t.getWorkspace() == null)
				return GameRules.STATE_WAITING;
			// We're waiting. So Start the clock ! as we are in Individual mode.
			getRWClock(t).start();
			return GameRules.STATE_PLAYING;
		}
	}

	public void start(Team tm) {
		// We're in individual mode, so the clock starts when logging in.
	}

	public void load(Round rnd, Team t, State st) {
		t.load(rnd, st);
		getRWClock(t).load(rnd, t, st);
	}

}
