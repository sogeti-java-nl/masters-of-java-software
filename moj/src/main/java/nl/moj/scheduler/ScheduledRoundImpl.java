package nl.moj.scheduler;

import java.util.Calendar;
import java.util.Date;

import nl.moj.model.Round;
import nl.moj.model.Scheduler.ScheduledRound;

class ScheduledRoundImpl implements ScheduledRound {

	private Date open;
	private Date start;
	private Round round;
	private String failure;
	
	public ScheduledRoundImpl(Date open,Date start,Round round) {
		if (open==null) throw new NullPointerException("NULL open time.");
		if (start==null) throw new NullPointerException("NULL start time.");
		if (start.before(new Date())) throw new IllegalArgumentException("Start time must be in the future.");
		if (start.before(open)) throw new IllegalArgumentException("Start time must be after Open time.");
		this.start=start;
		this.open=open;
		this.round=round;
	}
	
	public Round getRound() {
		return round;
	}
	public Date getOpenTime() {
		return open;
	}
	public Date getStartTime() {
		return start;
	}
	
	public Date getEstimatedEndTime() {
		return addRoundDuration(start);
	}
	
	protected Date addRoundDuration(Date d) {
		int mins=round.getAssignment().getDuration();
		Calendar c=Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.MINUTE,mins);
		return c.getTime();		
	}
	
	public boolean activeIn(Date rangeStart,Date rangeEnd) {
		if (rangeEnd==null) rangeEnd=addRoundDuration(rangeStart);
		Date estEnd=getEstimatedEndTime();
		if (rangeStart.after(estEnd)) return false;
		if (rangeEnd.before(open)) return false;
		return true;
	}
	
	public boolean isStarted() {
		return round.isStarted();
	}
	
	public boolean isRunning() {
		return (round.isStarted())&&(!round.isFinished());
	}

	public boolean isFinished() {
		return round.isFinished();
	}

	public String getFailure() {
		return failure;
	}

	public void setFailure(String failure) {
		this.failure = failure;
	}
	
	public boolean isFailed() {
		return failure!=null;
	}
	
}
