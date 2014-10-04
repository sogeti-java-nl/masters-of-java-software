package nl.moj.clock;

import nl.moj.model.Clock;
import nl.moj.model.Round;
import nl.moj.model.State;
import nl.moj.model.Team;


/**
 * Readonly wrapper for the clock interface to allow Teams to read the clock
 * but not start/stop/reset it. 
 * @author xanathar
 */
public class ReadOnlyClock implements Clock {

	private Clock clock;
	
	public ReadOnlyClock(Clock c) {
		clock=c;
	}
	
	public void addNotifier(Notification nf) {
		clock.addNotifier(nf);
	}
	public int getDuration() {		
		return clock.getDuration();
	}
	public int getDurationInMinutes() {
		return clock.getDurationInMinutes();
	}
	public int getSecondsPassed() {
		return clock.getSecondsPassed();
	}
	public int getSecondsRemaining() {
		return clock.getSecondsRemaining();
	}
	public boolean isFinished() {
		return clock.isFinished();
	}
	public boolean isRunning() {
		return clock.isRunning();
	}
	public boolean isStartPosition() {
		return clock.isStartPosition();
	}
	public boolean isStarted() {
		return clock.isStarted();
	}	
	
	//
	// These methods are blocked to prevent Teams from cheating.
	//
	
	public void load(Round rnd,Team tm, State s) {
		throw new RuntimeException("This is a readonly clock.");		
	}
	public void reset() {
		throw new RuntimeException("This is a readonly clock.");		
	}
	public void run() {
		throw new RuntimeException("This is a readonly clock.");		
	}
	public void start() {
		throw new RuntimeException("This is a readonly clock.");		
	}
	public void stop() {
		throw new RuntimeException("This is a readonly clock.");		
	}
	
	//
	//
	//
	
}
