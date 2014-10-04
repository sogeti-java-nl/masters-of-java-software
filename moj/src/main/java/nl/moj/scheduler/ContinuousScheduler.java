package nl.moj.scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.moj.model.Round;
import nl.moj.model.Scheduler;
import nl.moj.workspace.factory.WorkspaceFactory;

public class ContinuousScheduler implements Scheduler {

	private static final Logger log = Logger.getLogger("Scheduler");
	private static final Timer timer = new Timer("Continuous-Scheduler");

	private List<ScheduledRound> scheduledRounds = new ArrayList<>();
	private WorkspaceFactory factory;

	public ContinuousScheduler(WorkspaceFactory wf) {
		factory = wf;
		timer.schedule(new TimerTask() {
			public void run() {
				update();
			}
		}, 250L, 250L);
	}

	protected synchronized ScheduledRound[] getAllScheduledRounds() {
		return scheduledRounds.toArray(new ScheduledRound[scheduledRounds.size()]);
	}

	public synchronized ScheduledRound addToSchedule(Round r, Date startTime) {
		Calendar c = Calendar.getInstance();
		c.setTime(startTime);
		c.add(Calendar.MINUTE, -5);
		ScheduledRound sr = new ScheduledRoundImpl(c.getTime(), startTime, r);
		scheduledRounds.add(sr);
		log.info("Round for " + r.getAssignment().getName() + " and " + r.getTeamCount() + " teams added to schedule. Starts at " + startTime);
		return sr;
	}

	public ScheduledRound[] getSchedule(Date startTime, Date endTime) {
		List<ScheduledRound> results = new ArrayList<>();
		ScheduledRound[] rnd = getAllScheduledRounds();
		for (int t = 0; t < rnd.length; t++) {
			if (rnd[t].activeIn(startTime, endTime)) {
				results.add(rnd[t]);
			}
		}
		return results.toArray(new ScheduledRound[results.size()]);
	}

	public synchronized void removeFromSchedule(ScheduledRound r) {
		scheduledRounds.remove(r);
	}

	public Round[] getActiveRounds() {
		Date now = new Date();
		List<Round> results = new ArrayList<Round>();
		ScheduledRound[] rnd = getAllScheduledRounds();
		for (int t = 0; t < rnd.length; t++) {
			if (rnd[t].activeIn(now, null))
				results.add(rnd[t].getRound());
		}
		return results.toArray(new Round[results.size()]);
	}

	public Round[] getFinishedRounds() {
		List<Round> results = new ArrayList<>();
		ScheduledRound[] rnd = getAllScheduledRounds();
		for (int t = 0; t < rnd.length; t++) {
			if (rnd[t].isFinished())
				results.add(rnd[t].getRound());
		}
		return results.toArray(new Round[results.size()]);
	}

	/**
	 * Takes care of the starting of rounds and clean up.
	 */
	protected void update() {
		//
		// 1) Start any rounds that have exceeded their start time and
		// have not been started yet.
		//
		Date now = new Date();
		ScheduledRound[] rnd = getAllScheduledRounds();
		for (int t = 0; t < rnd.length; t++) {
			if (rnd[t].activeIn(now, null)) {
				if (rnd[t].getStartTime().before(now)) {
					if (!rnd[t].isStarted() && (!rnd[t].isFailed()))
						try {
							log.info("Started " + rnd[t].getRound().getAssignment().getName());
							rnd[t].getRound().start(factory, false);
						} catch (IOException ex) {
							rnd[t].setFailure("Failed to Start : " + ex.getMessage());
							log.log(Level.SEVERE, "Failed starting round ", ex);
						}
				}
			}
		}
		//
		// 2) remove all rounds that are (approx) older than a day.
		//
		Date yesterday = new Date(now.getTime() - (24L * 60L * 60L * 1000L));
		for (int t = 0; t < rnd.length; t++) {
			Date s = rnd[t].getStartTime();
			if (s.before(yesterday)) {
				removeFromSchedule(rnd[t]);
				log.info("Removed " + rnd[t].getRound().getAssignment().getName());
			}
		}
	}

}
