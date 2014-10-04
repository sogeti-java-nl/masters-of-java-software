package nl.moj.model;

import java.util.Date;

public interface Scheduler {

	public interface ScheduledRound {
		/** the time the round opens i.e. when you can login */
		public Date getOpenTime();
		/** the time the clock starts to run */
		public Date getStartTime();
		/** the round that is going to be played */
		public Round getRound();
		/** the estimated end time of the round */
		public Date getEstimatedEndTime();
		/** returns true if the round is active in the specified range */
		public boolean activeIn(Date rangeStart,Date rangeEnd);
		/** returns true if the round has started */
		public boolean isStarted();
		/** returns true if the round is running */
		public boolean isRunning();
		/** returns true if the round is finished */
		public boolean isFinished();
		
		public boolean isFailed();
		public String getFailure();
		public void setFailure(String s);
	}
	
	/** adds a round to the schedule */
	public ScheduledRound   addToSchedule(Round r,Date startTime);
	/** returns scheduled rounds for the given period */
	public ScheduledRound[] getSchedule(Date startTime,Date endTime);
	/** cancels a round */
	public void 			removeFromSchedule(ScheduledRound r);
	/** returns rounds that are started and not have finished */ 
	public Round[] 			getActiveRounds();
	/** returns the rounds that are finished */
	public Round[] 			getFinishedRounds();
	
}
