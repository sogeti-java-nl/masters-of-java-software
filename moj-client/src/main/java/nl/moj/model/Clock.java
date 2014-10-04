package nl.moj.model;

/**
 * Clock manages the progress of time in a Masters of Java round.
 */

public interface Clock extends Runnable {
	
	/**
	 * Time notification interface. 
	 * Implement this and attach yourself to the clock.
	 */
	public interface Notification {
		/** signals the beginning of time */
		public void clockReset();
		/** signals when the clock is started. */
		public void clockStarted(); 
		/** signals when the clock is stopped. */
		public void clockStopped();
		/** signals when a minute has passed and how many there are still remaining. */
		public void minutePassed(int remaining);	
		/** signals the end of time */
		public void clockFinished();
	} 

	/** resets the clock to its initial position */
	public void reset();
	/** starts the clock */
	public void start();
	/** stops the clock */
	public void stop();
	/** returns true if the clock is started */
	public boolean isStarted();
	/** returns true if the clock is in its initial position */
	public boolean isStartPosition();
	/** returns true if the clock is running */
	public boolean isRunning();
	/** returns true if the duration has passed */
	public boolean isFinished();
	/** returns the number of seconds that have passed */
	public int getSecondsPassed();
	/** returns the duration of this period in seconds */
	public int getDuration();
	/** returns the remaining seconds */
	public int getSecondsRemaining();
	
	public int getDurationInMinutes();
	
	/** attaches a clock notifier to this clock */
	public void addNotifier(Notification nf);

	/** special method for loading the previous state. */
	public void load(Round rnd,Team tm,State s);

}
