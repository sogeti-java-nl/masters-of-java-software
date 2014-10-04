package nl.moj.round;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.ctrlaltdev.util.Tool;
import nl.moj.model.Round;
import nl.moj.model.State;
import nl.moj.model.Team;

/**
 * 
 */
public class StateImpl implements State, State.Writer {

	private static final String OPENED = "OPEN";
	private static final String STARTED = "START";
	private static final String PAUSED = "PAUSE";
	private static final String RESUMED = "RESUME";
	private static final String FINISHED = "FINISH";
	private static final String SUBMITTED = "SUBMITTED";
	private static final String CLOSED = "CLOSE";
	private static final String TIME = "TIME";
	private static final String SCORE = "SCORE";
	private static final String KEYSTROKE = "KEYSTROKE";
	private static final String OPERATION = "OPERATION";
	private static final String SOLUTIONSIZE = "SOLUTIONSIZE";
	private static final String COMPILESUCCESS = "COMPSUCCESS";
	private static final String COMPILEFAILURE = "COMPFAILURE";
	private static final String TESTSUCCESS = "TESTSUCCESS";
	private static final String TESTFAILURE = "TESTFAILURE";
	private static final String LASTHOST = "LASTHOST";

	private class RoundData {
		Map<String, TeamData> teamData = new HashMap<>();

		public TeamData getTeam(String name) {
			TeamData tm = teamData.get(name);
			if (tm == null) {
				tm = new TeamData();
				teamData.put(name, tm);
				if (!knownTeams.contains(name)) {
					knownTeams.add(name);
				}
			}
			return tm;
		}
	}

	private static class TeamData {
		//
		// Clock / Round data
		//
		int timeRemaining = -1;
		boolean started;
		boolean finished;
		//
		// Stats of Team
		//
		boolean submitted;
		int nrOfKeyStrokes;
		int submitTime;
		int score = -1;
		int compileFailure;
		int compileSuccess;
		int testFailure;
		int testSuccess;
		Map<String, Integer> operationCount = new HashMap<>();
		Map<Integer, Integer> solutionSize = new HashMap<>();
		int lastSize;
	}

	private FileWriter myWriter;
	private Map<String, RoundData> roundData;
	private List<String> knownRounds;
	private List<String> knownTeams;
	private List<String> myBuffer;
	private Map<String, String> workspaceHosts;

	public StateImpl(File stateFile) throws IOException {
		super();
		if (stateFile == null)
			throw new IOException("NULL State File.");
		//
		roundData = new HashMap<>();
		knownTeams = new ArrayList<>();
		knownRounds = new ArrayList<>();
		myBuffer = new ArrayList<>();
		workspaceHosts = new HashMap<>();
		//
		if (stateFile.exists()) {
			readState(stateFile);
		} else {
			stateFile.createNewFile();
		}
		//
		myWriter = new FileWriter(stateFile, true);
		//
	}

	protected RoundData getRoundData(String round) {
		RoundData rd = roundData.get(round);
		if (rd == null) {
			rd = new RoundData();
			roundData.put(round, rd);
			knownRounds.add(round);
		}
		return rd;
	}

	protected void readState(File in) throws IOException {
		BufferedReader fin = new BufferedReader(new FileReader(in));
		int cnt = 1;
		try {
			String s = fin.readLine();
			while (s != null) {
				parseLine(s);
				s = fin.readLine();
				cnt++;
			}
		} catch (IOException ex) {
			System.out.println("Error in line : " + cnt);
			throw ex;
		} catch (NumberFormatException ex) {
			System.out.println("Error in line : " + cnt);
			throw ex;
		} finally {
			fin.close();
		}
	}

	protected void parseLine(String s) throws IOException {
		String[] elem = Tool.cutForEach(s, ";");
		if (elem.length != 4)
			throw new IOException("Invalid number of cells.");
		String round = elem[0];
		String team = elem[1];
		String what = elem[2];
		String value = elem[3];
		//
		if (LASTHOST.equals(what)) {
			workspaceHosts.put(round, team + ":" + value);
			return;
		}
		//
		RoundData rd = getRoundData(round);
		if (team.length() > 0) {
			TeamData tm = rd.getTeam(team);
			if (SUBMITTED.equals(what)) {
				if (tm.submitted)
					throw new IOException("Cannot submit twice in a round ?!");
				tm.submitted = true;
				tm.submitTime = Integer.parseInt(value);
			} else if (SCORE.equals(what)) {
				if (!tm.submitted)
					throw new IOException("Cannot have a SCORE before a SUBMITTED.");
				tm.score = Integer.parseInt(value);
			} else if (KEYSTROKE.equals(what)) {
				tm.nrOfKeyStrokes += Integer.parseInt(value);
			} else if (OPERATION.equals(what)) {
				Integer i = tm.operationCount.get(what);
				if (i == null) {
					tm.operationCount.put(what, new Integer(1));
				} else {
					tm.operationCount.put(what, new Integer(i.intValue()));
				}
			} else if (SOLUTIONSIZE.equals(what)) {
				tm.lastSize = Integer.parseInt(value);
			} else if (COMPILEFAILURE.equals(what)) {
				tm.compileFailure++;
			} else if (COMPILESUCCESS.equals(what)) {
				tm.compileSuccess++;
			} else if (TESTFAILURE.equals(what)) {
				tm.testFailure++;
			} else if (TESTSUCCESS.equals(what)) {
				tm.testSuccess++;
			} else if (TIME.equals(what)) {
				tm.timeRemaining = Integer.parseInt(value);
				if (tm.timeRemaining % 60 != 0)
					throw new IOException("Not a complete minute : " + tm.timeRemaining);
				tm.solutionSize.put(new Integer(tm.timeRemaining), new Integer(tm.lastSize));
			} else if (OPENED.equals(what)) {
			} else if (STARTED.equals(what)) {
				tm.started = true;
			} else if (PAUSED.equals(what)) {
			} else if (RESUMED.equals(what)) {
			} else if (FINISHED.equals(what)) {
				tm.finished = true;
			} else if (CLOSED.equals(what)) {
			} else
				throw new IOException("State : Invalid value : '" + what + "'");
		} else
			throw new IOException("State : Missing Team value (1.0 state file ??)");
	}

	protected synchronized void writeLine(String round, String team, String what, String score) {
		myBuffer.add(round + ";" + team + ";" + what + ";" + score + "\n");
	}

	//
	// public interface
	//
	public String[] getKnownRoundNames() {
		return knownRounds.toArray(new String[knownRounds.size()]);
	}

	public String[] getKnownTeams() {
		return knownTeams.toArray(new String[knownTeams.size()]);
	}

	public int getScore(Round rnd, Team team) {
		return getScore(rnd.getAssignment().getName(), team.getName());
	}

	public int getScore(String round, String team) {
		RoundData rd = getRoundData(round);
		TeamData tm = rd.getTeam(team);
		if (tm.submitted)
			return tm.score;
		return -1;
	}

	public int getSubmitTime(Round round, Team team) {
		RoundData rd = getRoundData(round.getAssignment().getName());
		TeamData tm = rd.getTeam(team.getName());
		if (tm.submitted)
			return tm.submitTime;
		return -1;
	}

	public int getTimeRemaining(Round rnd, Team tm) {
		RoundData rd = getRoundData(rnd.getAssignment().getName());
		TeamData td = rd.getTeam(tm.getName());
		return td.timeRemaining;
	}

	public boolean isComplete(Round rnd, Team team) {
		RoundData rd = getRoundData(rnd.getAssignment().getName());
		TeamData tm = rd.getTeam(team.getName());
		return tm.submitted;
	}

	public boolean isFinished(Round rnd, Team tm) {
		RoundData rd = getRoundData(rnd.getAssignment().getName());
		TeamData td = rd.getTeam(tm.getName());
		return td.finished;
	}

	public boolean isStarted(Round rnd, Team tm) {
		RoundData rd = getRoundData(rnd.getAssignment().getName());
		TeamData td = rd.getTeam(tm.getName());
		return td.started;
	}

	public String getRoundWinner(String rnd) {
		RoundData rd = getRoundData(rnd);
		int hi = 0;
		String nameHi = null;
		Iterator<String> i = rd.teamData.keySet().iterator();
		while (i.hasNext()) {
			String name = i.next();
			TeamData td = rd.getTeam(name);
			if (td.score >= 0) {
				if (hi < td.score) {
					nameHi = name;
					hi = td.score;
				}
			}
		}
		//
		return nameHi;
	}

	public int getNrOfKeystrokes(String round, String team) {
		RoundData rd = getRoundData(round);
		TeamData tm = rd.getTeam(team);
		return tm.nrOfKeyStrokes;
	}

	public float getKeyStrokeFileSizeRatio(String round, String team) {
		RoundData rd = getRoundData(round);
		TeamData tm = rd.getTeam(team);
		if (tm.lastSize == 0)
			return 999;
		return ((float) tm.nrOfKeyStrokes) / ((float) tm.lastSize);
	}

	public String[] getOperationNames(String round, String team) {
		RoundData rd = getRoundData(round);
		TeamData tm = rd.getTeam(team);
		return tm.operationCount.keySet().toArray(new String[tm.operationCount.size()]);
	}

	public int getOperationCount(String round, String team, String op) {
		RoundData rd = getRoundData(round);
		TeamData tm = rd.getTeam(team);
		Integer i = tm.operationCount.get(op);
		if (i == null)
			return -1;
		return i.intValue();
	}

	public int getCompileFailures(String round, String team) {
		return getRoundData(round).getTeam(team).compileFailure;
	}

	public int getTestFailures(String round, String team) {
		return getRoundData(round).getTeam(team).testFailure;
	}

	public int getCompileSuccess(String round, String team) {
		return getRoundData(round).getTeam(team).compileSuccess;
	}

	public int getTestSuccess(String round, String team) {
		return getRoundData(round).getTeam(team).testSuccess;
	}

	public int getFileSizeForMinute(String round, String team, int minute) {
		Integer m = new Integer(1800 - minute * 60);
		Integer i = (getRoundData(round).getTeam(team).solutionSize.get(m));
		if (i == null) {
			if (minute == 0)
				return 0;
			return getFileSizeForMinute(round, team, minute - 1);
		}
		return i.intValue();
	}

	public int getFinalFileSize(String round, String team) {
		return getRoundData(round).getTeam(team).lastSize;
	}

	//
	// Writer interface.
	//

	public void open(Team tm) {
		writeLine(tm.getAssignment().getName(), tm.getName(), OPENED, "");
	}

	public void start(Team tm) {
		writeLine(tm.getAssignment().getName(), tm.getName(), STARTED, "");
	}

	public void pause(Team tm) {
		writeLine(tm.getAssignment().getName(), tm.getName(), PAUSED, "");
	}

	public void resume(Team tm) {
		writeLine(tm.getAssignment().getName(), tm.getName(), RESUMED, "");
	}

	public void close(Team tm) {
		writeLine(tm.getAssignment().getName(), tm.getName(), CLOSED, "");
	}

	public void timeRemaining(Team tm, int time) {
		writeLine(tm.getAssignment().getName(), tm.getName(), TIME, String.valueOf(time));
	}

	public void score(Team tm) {
		writeLine(tm.getAssignment().getName(), tm.getName(), SCORE, String.valueOf(tm.getFinalScore()));
	}

	public void finish(Team tm) {
		writeLine(tm.getAssignment().getName(), tm.getName(), FINISHED, "");
	}

	public void submit(Team tm, int time) {
		writeLine(tm.getAssignment().getName(), tm.getName(), SUBMITTED, String.valueOf(time));
	}

	public void keyStrokes(Team tm, int strokes) {
		writeLine(tm.getAssignment().getName(), tm.getName(), KEYSTROKE, String.valueOf(strokes));
	}

	public void operation(Team tm, String name) {
		writeLine(tm.getAssignment().getName(), tm.getName(), OPERATION, name);
	}

	public void solutionSize(Team tm, int size) {
		writeLine(tm.getAssignment().getName(), tm.getName(), SOLUTIONSIZE, String.valueOf(size));
	}

	public void compileFailure(Team tm) {
		writeLine(tm.getAssignment().getName(), tm.getName(), COMPILEFAILURE, "");
	}

	public void compileSuccess(Team tm) {
		writeLine(tm.getAssignment().getName(), tm.getName(), COMPILESUCCESS, "");
	}

	public void testFailure(Team tm) {
		writeLine(tm.getAssignment().getName(), tm.getName(), TESTFAILURE, "");
	}

	public void testSuccess(Team tm) {
		writeLine(tm.getAssignment().getName(), tm.getName(), TESTSUCCESS, "");
	}

	//
	// Workspace
	//
	public String getLastHost(String workspaceName) {
		String host = workspaceHosts.get(workspaceName);
		if (host == null)
			return null;
		return host.substring(0, host.indexOf(":"));
	}

	public int getLastPort(String workspaceName) {
		String host = workspaceHosts.get(workspaceName);
		if (host == null)
			return -1;
		return Integer.parseInt(host.substring(host.indexOf(":") + 1));
	}

	public void workspaceHost(String workspaceName, String host, int port) {
		writeLine(workspaceName, host, LASTHOST, String.valueOf(port));
	}

	//
	// Dump 2 disk
	//
	public void flush() throws IOException {
		String[] lines = null;
		synchronized (myBuffer) {
			lines = myBuffer.toArray(new String[myBuffer.size()]);
			myBuffer.clear();
		}
		for (int t = 0; t < lines.length; t++) {
			myWriter.write(lines[t]);
		}
		myWriter.flush();
	}

	public void closeFile() throws IOException {
		flush();
		myWriter.close();
	}

}
