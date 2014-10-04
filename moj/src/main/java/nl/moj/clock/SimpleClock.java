package nl.moj.clock;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.moj.model.Clock;
import nl.moj.model.Round;
import nl.moj.model.State;
import nl.moj.model.Team;

/**
 * Simple Clock implementation. Uses System.currentTimeMillis() to calculate the
 * time difference between the start time and current time and updates minutes
 * and seconds accordingly. To update the current time the run() method must be
 * called periodically. This is done using theTimer
 * 
 * @author E.Hooijmeijer
 */

public class SimpleClock implements Clock, Runnable {

	private static final Timer theTimer = new Timer();

	public static final Logger log = Logger.getLogger("Clock");
	/**
	 * divisor, number of milliseconds for one second. Decrease to make time go
	 * faster :-)
	 */
	public static final long DELTA_DIV = 1000;
	// public static final long DELTA_DIV=100;
	// public static final long DELTA_DIV=10;

	/**
	 * the start-time of the clock. The reference to which the progress of time
	 * is measured
	 */
	private long referenceTime;
	/** true if the clock is running */
	private boolean isRunning;
	/**
	 * contains the number of minutes at the moment the clock was
	 * started/stopped
	 */
	private int mins;
	/**
	 * contains the number of seconds at the moment the clock was
	 * started/stopped
	 */
	private int secs;
	/** value used to reset the clock to */
	private int resetMins;
	/** value used to reset the clock to */
	private int resetSecs;
	/** value used to detect a minute change */
	private int lastMins;
	/** list of notifiers */
	private List<Notification> myNoti = new ArrayList<>();
	/** holds the actual number of seconds passed (updated each run()) */
	private int secondsPassed;
	/** the duration of this clock in minutes */
	private int durationInMinutes;
	/** indicates if the clock was started now or previously */
	private boolean wasStarted;

	public SimpleClock(int durationInMinutes) {
		this.durationInMinutes = durationInMinutes;
		resetMins = 0;
		resetSecs = 0;
		reset();
		stop();
		//
		// Make sure the clock gets updated.
		//
		theTimer.schedule(new TimerTask() {
			public void run() {
				SimpleClock.this.run();
			}
		}, 250l, 250l);
		//
		//
		//
	}

	public void stop() {
		if (isRunning) {
			long delta = System.currentTimeMillis() - referenceTime;
			//
			delta = delta / DELTA_DIV;
			//
			secs += (int) delta % 60;
			mins += (int) delta / 60;
			//
			while (secs > 60) {
				mins += 1;
				secs -= 60;
			}
			//
			isRunning = false;
			//
			fireClockStopped();
		}
	}

	public void run() {
		int rmins = mins;
		int rsecs = secs;
		//
		if (isRunning) {
			//
			long delta = System.currentTimeMillis() - referenceTime;
			//
			delta = delta / DELTA_DIV;
			//
			int dsecs = (int) delta % 60;
			int dmins = (int) delta / 60;
			//
			rmins += dmins;
			rsecs += dsecs;
			//
			while (rsecs >= 60) {
				rmins += 1;
				rsecs -= 60;
			}
			//
			if (rmins >= durationInMinutes) {
				rsecs = 0;
				rmins = durationInMinutes;
			}
			//
			secondsPassed = rmins * 60 + rsecs;
			//
			if (lastMins != rmins) {
				lastMins = rmins;
				fireMinutePassed(durationInMinutes - lastMins);
				if (durationInMinutes == lastMins) {
					stop();
				}
			}
			//
		}
		//
		if (isFinished()) {
			secs = 0;
			mins = durationInMinutes;
		}
		//
	}

	public void addNotifier(Notification nf) {
		myNoti.add(nf);
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void start() {
		if (!isFinished()) {
			isRunning = true;
			referenceTime = System.currentTimeMillis();
			fireClockStarted();
			wasStarted = true;
		}
	}

	public boolean isStarted() {
		return wasStarted;
	}

	public boolean isFinished() {
		return (secondsPassed >= getDuration());
	}

	public boolean isStartPosition() {
		return ((mins == 0) && (secs == 0) && (!wasStarted));
	}

	public int getDuration() {
		return durationInMinutes * 60;
	}

	public int getDurationInMinutes() {
		return durationInMinutes;
	}

	public int getSecondsRemaining() {
		return durationInMinutes * 60 - secondsPassed;
	}

	public int getSecondsPassed() {
		return secondsPassed;
	}

	public void reset() {
		if (isRunning)
			stop();
		mins = resetMins;
		secs = resetSecs;
		lastMins = resetMins;
		wasStarted = false;
		fireClockReset();
	}

	private void fireClockReset() {
		for (int t = 0; t < myNoti.size(); t++)
			try {
				(myNoti.get(t)).clockReset();
			} catch (Exception ex) {
				log.log(Level.SEVERE, "Reset Notification failure", ex);
				ex.printStackTrace();
			}
	}

	private void fireClockStarted() {
		for (int t = 0; t < myNoti.size(); t++)
			try {
				(myNoti.get(t)).clockStarted();
			} catch (Exception ex) {
				log.log(Level.SEVERE, "Start Notification failure", ex);
				ex.printStackTrace();
			}
	}

	private void fireMinutePassed(int min) {
		for (int t = 0; t < myNoti.size(); t++)
			try {
				if (min == durationInMinutes) {
					(myNoti.get(t)).clockFinished();
				} else {
					(myNoti.get(t)).minutePassed(min);
				}
			} catch (Exception ex) {
				log.log(Level.SEVERE, "Tick Notification failure", ex);
				ex.printStackTrace();
			}
	}

	private void fireClockStopped() {
		for (int t = 0; t < myNoti.size(); t++)
			try {
				(myNoti.get(t)).clockStopped();
			} catch (Exception ex) {
				log.log(Level.SEVERE, "Stop Notification failure", ex);
				ex.printStackTrace();
			}
	}

	public void load(Round rnd, Team tm, State s) {
		if (s.isFinished(rnd, tm)) {
			secondsPassed = durationInMinutes * 60;
			mins = durationInMinutes;
			secs = 0;
			resetMins = mins;
			resetSecs = secs;
			lastMins = mins;
		} else {
			this.wasStarted = s.isStarted(rnd, tm);
			int remain = s.getTimeRemaining(rnd, tm);
			if (remain >= 0) {
				secondsPassed = getDuration() - remain;
				mins = secondsPassed / 60;
				secs = secondsPassed % 60;
				resetMins = mins;
				resetSecs = secs;
				lastMins = mins;
			}
		}
	}

}
