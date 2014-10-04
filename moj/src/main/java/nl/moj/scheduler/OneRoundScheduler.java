package nl.moj.scheduler;

import java.util.Date;

import nl.moj.model.Round;
import nl.moj.model.Scheduler;

/**
 * Scheduler with just one round, which is always active. 
 */

public class OneRoundScheduler implements Scheduler {

	private Round rnd;
	
	public OneRoundScheduler(Round rnd) {
		this.rnd=rnd;
	} 
	
	public ScheduledRound addToSchedule(Round r, Date startTime) {
		return null;
	}
	public Round[] getActiveRounds() {
		return new Round[] {rnd};
	}
	public ScheduledRound[] getSchedule(Date startTime, Date endTime) {
		return new ScheduledRound[0];
	}
	public void removeFromSchedule(ScheduledRound r) {
	}	
	
	public Round[] getFinishedRounds() {
		return new Round[0];
	}
	
}
