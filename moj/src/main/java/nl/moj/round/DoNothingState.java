package nl.moj.round;

import java.io.IOException;

import nl.moj.model.Round;
import nl.moj.model.State;
import nl.moj.model.Team;

/**
 * 
 */
public class DoNothingState implements State,State.Writer {

	private int timeRemaining;
	
	public void setTimeRemaining(int t) {
		timeRemaining=t;
	}
	
	public void close(Team team) {

	}
	public void closeFile() throws IOException {

	}
	public void compileFailure(Team team) {

	}
	public void compileSuccess(Team team) {

	}
	public void finish(Team team) {

	}
	public void finish(Team team, int time) {

	}
	public void flush() throws IOException {

	}
	public int getCompileFailures(String round, String team) {
		return 0;
	}

	public int getCompileSuccess(String round, String team) {
		return 0;
	}

	public int getFileSizeForMinute(String round, String team, int minute) {
		return 0;
	}

	public int getFinalFileSize(String round, String team) {
		return 0;
	}
	public float getKeyStrokeFileSizeRatio(String round, String team) {
		return 0;
	}
	public String[] getKnownRoundNames() {
		return null;
	}
	public String[] getKnownTeams() {
		return null;
	}
	public int getOperationCount(String round, String team, String op) {
		return 0;
	}
	public String[] getOperationNames(String round, String team) {
		return null;
	}
	public String getRoundWinner(String round) {
		return null;
	}
	public int getScore(Round rnd, Team team) {
		return 0;
	}
	public int getScore(String round, String team) {
		return 0;
	}
	public int getSubmitTime(Round rnd, Team team) {
		return 0;
	}
	public int getTestFailures(String round, String team) {
		return 0;
	}
	public int getTestSuccess(String round, String team) {
		return 0;
	}
	public int getTimeRemaining(Round rnd, Team team) {
		return timeRemaining;
	}
	public boolean isComplete(Round rnd, Team team) {
		return false;
	}
	public boolean isFinished(Round rnd, Team team) {
		return false;
	}
	public boolean isStarted(Round rnd, Team team) {
		return false;
	}
	
	//
	// State.Writer
	//
	
	public void keyStrokes(Team team, int strokes) {

	}
	public void open(Team team) {

	}
	public void operation(Team team, String name) {

	}
	public void pause(Team team) {

	}
	public void resume(Team team) {

	}
	public void score(Team team) {

	}
	public void solutionSize(Team team, int size) {

	}
	public void start(Team tm) {

	}
	public void testFailure(Team team) {

	}
	public void testSuccess(Team team) {

	}
	public void timeRemaining(Team team,int time) {

	}
	public void submit(Team team, int time) {
	}
	public String getLastHost(String workspaceName) {
		return null;
	}
	public int getLastPort(String workspaceName) {
		return 0;
	}
	public void workspaceHost(String workspaceName, String host, int port) {
	}
	public int getNrOfKeystrokes(String round, String team) {
		return 0;
	}	

}
