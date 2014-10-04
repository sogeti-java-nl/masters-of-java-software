package nl.moj.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.moj.client.anim.Anim;
import nl.moj.client.io.AddActionMessageImpl;
import nl.moj.client.io.AnimationMessageImpl;
import nl.moj.client.io.AssignmentMessageImpl;
import nl.moj.client.io.ConsoleMessageImpl;
import nl.moj.client.io.EditorMessageImpl;
import nl.moj.client.io.LogonFailureMessageImpl;
import nl.moj.client.io.Message;
import nl.moj.client.io.Message.Action;
import nl.moj.client.io.Message.Hello;
import nl.moj.client.io.Message.NoOp;
import nl.moj.client.io.ProtocolVersionMismatchMessageImpl;
import nl.moj.client.io.TestSetMessageImpl;
import nl.moj.client.io.UpdateClientStatisticsMessageImpl;
import nl.moj.model.Operation;
import nl.moj.model.Round;
import nl.moj.model.Scheduler;
import nl.moj.model.Team;
import nl.moj.model.Tester;
import nl.moj.model.Workspace;
import nl.moj.operation.ContextImpl;
import nl.moj.operation.Test;

public class ClientServerImpl implements ClientServerInterface {

	private Scheduler scheduler;
	private Round round;
	private Team team;

	private int lastState = -1;

	public ClientServerImpl(Scheduler scheduler) {
		if (scheduler == null)
			throw new NullPointerException("Cannot function without a Scheduler.");
		this.scheduler = scheduler;
	}

	//
	// State indicators.
	//

	public boolean isStateChanged() {
		if (isInitial()) {
			if (lastState != -1) {
				lastState = -1;
				return true;
			}
		} else if (isWaiting()) {
			if (lastState != 0) {
				lastState = 0;
				return true;
			}
		} else if (isPlaying()) {
			if (lastState != 1) {
				lastState = 1;
				return true;
			}
		} else if (isFinished()) {
			if (lastState != 2) {
				lastState = 2;
				return true;
			}
		}
		return false;
	}

	public boolean isInitial() {
		return (team == null);
	}

	public boolean isScoreAvailable() {
		if (team == null)
			return false;
		return team.isScoreAvailable();
	}

	public boolean isFinished() {
		if (team == null)
			return false;
		return team.isFinished();
	}

	public boolean isPlaying() {
		if (team == null)
			return false;
		return team.isPlaying();
	}

	public boolean isWaiting() {
		if (team == null)
			return false;
		return team.isWaiting();
	}

	//
	// Messages
	//

	public Message[] onHello(Hello msg) throws InvalidStateException {
		if (!isInitial())
			throw new InvalidStateException("State is not Initial. Cannot say Hello.");
		//
		Round tmpRound = null;
		Team tmpTeam = null;
		//
		Round[] rnd = scheduler.getActiveRounds();
		for (int t = 0; t < rnd.length; t++) {
			Team tmp = rnd[t].getTeamByName(msg.getTeamName());
			if (tmp == null)
				continue;
			tmpRound = rnd[t];
			tmpTeam = tmp;
		}
		//
		if (!isOnline(tmpTeam)) {
			if (isCorrectPassword(tmpTeam, msg)) {
				if (isCorrectProtocolVersion(msg)) {
					//
					team = tmpTeam;
					round = tmpRound;
					//
					return onNoOp(null, new AssignmentMessageImpl(tmpTeam.getAssignment()));
					//
				} else {
					return new Message[] { new ProtocolVersionMismatchMessageImpl() };
				}
			} else {
				// Incorrect Password.
				return new Message[] { new LogonFailureMessageImpl() };
			}
		} else {
			// Already Online.
			return new Message[] { new LogonFailureMessageImpl() };
		}
	}

	public Message[] onAction(Action action) throws InvalidStateException {
		if (!isPlaying())
			throw new InvalidStateException("State is not Playing. Cannot perform an Action.");
		//
		// Perform the Action.
		//
		Operation[] ops = round.getAssignment().getOperations();
		for (int t = 0; t < ops.length; t++) {
			if (ops[t].getName().equals(action.getAction())) {
				team.doOperation(ops[t], new ContextImpl(action.getFileName(), action.getContents(), action.getIndex()));
				team.addStatistics(action.getKeyStrokes(), action.getAction(), action.getContents().length());
			}
		}
		//
		// Send back Results.
		//
		return onNoOp(null);
	}
	
	public Message[] onMultiAction(Message.Actions action) throws InvalidStateException {
		if (!isPlaying())
			throw new InvalidStateException("State is not Playing. Cannot perform an Action.");
		//
		// Perform the Action.
		//
		Operation[] ops = round.getAssignment().getOperations();
		for (int t = 0; t < ops.length; t++) {
			if (ops[t].getName().equals(action.getAction(0))) {
				ContextImpl ctx = new ContextImpl(action.getFileNames(), action.getAllContents(), action.getIndex(0));
				team.doOperation(ops[t], ctx);
				team.addStatistics(action.getKeyStrokes(0), action.getAction(0), action.getContents(0).length());
			}
		}
		//
		// Send back Results.
		//
		return onNoOp(null);
	}

	public Message[] onNoOp(NoOp msg) throws InvalidStateException {
		return onNoOp(msg, null);
	}

	protected Message[] onNoOp(NoOp msg, Message additionalMsg) throws InvalidStateException {
		if (isInitial())
			throw new InvalidStateException("State is Initial. First say Hello, then do a NoOp.");
		//
		List<Message> results = new ArrayList<>();
		//
		if (additionalMsg != null)
			results.add(additionalMsg);
		//
		if (isStateChanged()) {
			if (isPlaying())
				try {
					getWaitingToPlayingTransitionMessages(results);
				} catch (IOException ex) {
					// Ai...
					throw new RuntimeException(ex);
				}
		}
		//
		if (isWaiting()) {
			getWaitingMessages(results);
		} else if (isPlaying()) {
			getPlayingMessages(results);
		} else if (isFinished()) {
			getFinishedMessages(results);
		} else
			throw new InvalidStateException();
		//
		return results.toArray(new Message[results.size()]);
	}

	public Message[] onGoodBye(Message.GoodBye msg) throws InvalidStateException {
		if (isInitial())
			throw new InvalidStateException("State is Initial. First say Hello, then do a Goodbye.");
		logout(team);
		team = null;
		round = null;
		return new Message[0];
	}

	//
	//
	//

	protected boolean isCorrectProtocolVersion(Message.Hello msg) {
		return (msg.getProtocolVersion() == Message.PROTOCOLVERSION);
	}

	protected boolean isOnline(Team tm) {
		return tm.isOnline();
	}

	protected boolean isCorrectPassword(Team tm, Message.Hello msg) {
		if (tm == null)
			return false;
		return tm.isValidPassword(msg.getPassword());
	}

	protected void logout(Team tm) {
		tm.isValidPassword(null);
	}

	public void update() {
		try {
			if (team != null) {
				team.getWorkspace().update();
			}
		} catch (IOException ex) {
			throw new RuntimeException("Workspace update failed", ex);
		}
	}

	//
	//
	//

	protected void getWaitingMessages(List<Message> results) {
		results.add(new UpdateClientStatisticsMessageImpl(0, team.getClock().getDuration(), team.getClock().getSecondsRemaining(),
				Message.UpdateClientStatistics.STATE_WAIT, round.getTeamsOnline(), round.getTeamCount(), null));
	}

	protected void getWaitingToPlayingTransitionMessages(List<Message> results) throws IOException {
		//
		// Send the assignment.
		//
		Workspace ws = team.getWorkspace();
		String[] editorFiles = ws.getEditorFiles();
		for (int t = 0; t < editorFiles.length; t++) {
			String current = editorFiles[t];
			if (!ws.isJava(current)) {
				results.add(new EditorMessageImpl(current, ws.getContents(current), ws.isJava(current), ws.isReadOnly(current), ws
						.isMonospaced(current)));
			}
		}
		//
		// Send the allowed Operations.
		//
		Tester tester = null;
		Operation[] ops = round.getAssignment().getOperations();
		for (int t = 0; t < ops.length; t++) {
			results.add(new AddActionMessageImpl(ops[t].getName(), ops[t].needsConfirm(), ops[t].getTooltip()));
			if (ops[t] instanceof Test) {
				tester = ((Test) ops[t]).getTester();
			}
		}
		//
		// Send the TestSet
		//
		if (tester != null) {
			results.add(new TestSetMessageImpl(tester.getTestNames(), tester.getTestDescriptions()));
		}
		//
		// Send the Source Files.
		//
		for (int t = 0; t < editorFiles.length; t++) {
			String current = editorFiles[t];
			if (ws.isJava(current)) {
				results.add(new EditorMessageImpl(current, ws.getContents(current), ws.isJava(current), ws.isReadOnly(current), ws
						.isMonospaced(current)));
			}
		}
	}

	protected void getPlayingMessages(List<Message> results) {
		//
		// Periodically update the client.
		//
		int[] testResults = team.getTestResults();
		Anim[] testAnimResults = team.getAnimatedTestResults();
		//
		results.add(new UpdateClientStatisticsMessageImpl(0, team.getClock().getDuration(), team.getClock().getSecondsRemaining(),
				Message.UpdateClientStatistics.STATE_PROGRAMMING, round.getTeamsOnline(), round.getTeamCount(), testResults));
		//
		// Write any animations to the client.
		//
		if ((testResults != null) && (testAnimResults != null)) {
			for (int t = 0; t < testAnimResults.length; t++) {
				if (testAnimResults[t] != null) {
					results.add(new AnimationMessageImpl(t, testAnimResults[t]));
				}
			}
		}
		//
		// Console Msgs
		//
		String[][] l = team.getLines();
		for (int t = 0; t < l.length; t++) {
			results.add(new ConsoleMessageImpl((l[t][0] == null ? "Output" : l[t][0]), l[t][1]));
		}
	}

	protected void getFinishedMessages(List<Message> results) {
		results.add(new UpdateClientStatisticsMessageImpl((team.isScoreAvailable() ? team.getFinalScore() : team.getTheoreticalScore()), team
				.getClock().getDuration(), team.getClock().getSecondsRemaining(), Message.UpdateClientStatistics.STATE_FINISHED, round
				.getTeamsOnline(), round.getTeamCount(), null));
		//
		String[][] l = team.getLines();
		for (int t = 0; t < l.length; t++) {
			results.add(new ConsoleMessageImpl((l[t][0] == null ? "Output" : l[t][0]), l[t][1]));
		}
	}

}