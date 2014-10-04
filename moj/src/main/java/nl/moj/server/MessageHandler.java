package nl.moj.server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import nl.ctrlaltdev.net.server.Log;
import nl.moj.client.anim.Anim;
import nl.moj.client.io.AddActionMessageImpl;
import nl.moj.client.io.AnimationMessageImpl;
import nl.moj.client.io.ConsoleMessageImpl;
import nl.moj.client.io.EditorMessageImpl;
import nl.moj.client.io.LogonFailureMessageImpl;
import nl.moj.client.io.Message;
import nl.moj.client.io.MessageFactory;
import nl.moj.client.io.ProtocolVersionMismatchMessageImpl;
import nl.moj.client.io.TestSetMessageImpl;
import nl.moj.client.io.UpdateClientStatisticsMessageImpl;
import nl.moj.model.Operation;
import nl.moj.model.Round;
import nl.moj.model.Team;
import nl.moj.model.Tester;
import nl.moj.model.Workspace;
import nl.moj.operation.ContextImpl;
import nl.moj.operation.Test;

/**
 * @deprecated
 */
@Deprecated
public class MessageHandler implements Runnable {

	private MessageFactory factory = new MessageFactory();
	private Socket mySocket;
	private DataOutputStream out;
	private DataInputStream in;
	private Log myLog;

	private Round myRound;
	private Team myTeam;

	public MessageHandler(Socket s, Log l, Round rnd) {
		try {
			mySocket = s;
			myLog = l;
			myRound = rnd;
			s.setSoTimeout(1000);
			out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
			in = new DataInputStream(s.getInputStream());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void run() {
		boolean loginOk = false;
		try {
			//
			// Signal Wait State.
			//
			UpdateClientStatisticsMessageImpl up = new UpdateClientStatisticsMessageImpl(0, 3600, 3600, 0, myRound.getTeamsOnline(),
					myRound.getTeamCount(), null);
			up.write(out);
			//
			// There should be a HELLO message waiting for us to authenticate
			// the user.
			// If it is not here within a second, abort. If the username
			// password combination
			// is invalid, abort.
			//
			Message msg = factory.createMessage(in);
			if (msg.getType() == Message.MSG_HELLO) {
				Message.Hello hello = ((Message.Hello) msg);
				//
				if (hello.getProtocolVersion() != Message.PROTOCOLVERSION) {
					new ProtocolVersionMismatchMessageImpl().write(out);
					out.flush();
					throw new IOException("Protocol version mismatch for Team " + hello.getTeamName());
				}
				//
				Team t = myRound.getTeamByName(hello.getTeamName());
				if (t != null) {
					// Cannot login twice.
					if (t.isOnline())
						return;
				}
				if ((t != null) && (t.isValidPassword(hello.getPassword()))) {
					myTeam = t;
					loginOk = true;
				} else {
					myLog.warn("Logon Failure for Team " + hello.getTeamName());
					new ConsoleMessageImpl("ERROR", "Invalid username and password combination.").write(out);
					new LogonFailureMessageImpl().write(out);
					out.flush();
					throw new IOException("Authentication Failed.");
				}
			} else
				throw new IOException("No Hello, No Game.");
			//
			// Now, depending on the state of the game, we should do different
			// things.
			//
			out.flush();
			//
			// Wait State means that the client has to wait for the start of the
			// game.
			//
			while (myTeam.isWaiting()) {
				//
				// Periodically update the client.
				//
				msg = new UpdateClientStatisticsMessageImpl(0, myTeam.getClock().getDuration(), myTeam.getClock().getSecondsRemaining(),
						Message.UpdateClientStatistics.STATE_WAIT, myRound.getTeamsOnline(), myRound.getTeamCount(), null);
				msg.write(out);
				out.flush();
				//
				// Read and ignore any messages (hah !)
				//
				try {
					msg = factory.createMessage(in);
				} catch (SocketTimeoutException ex) {
					// Ingore.
				}
			}
			//
			// We have left wait state, so now it can be either programming or
			// finished.
			//
			if (!myTeam.isFinished()) {
				//
				// Send the assignment.
				//
				Workspace ws = myTeam.getWorkspace();
				String[] editorFiles = ws.getEditorFiles();
				for (int t = 0; t < editorFiles.length; t++) {
					String current = editorFiles[t];
					if (!ws.isJava(current)) {
						Message ed = new EditorMessageImpl(current, ws.getContents(current), ws.isJava(current), ws.isReadOnly(current),
								ws.isMonospaced(current));
						ed.write(out);
					}
				}
				//
				// Send the allowed Operations.
				//
				Tester tester = null;
				Operation[] ops = myRound.getAssignment().getOperations();
				for (int t = 0; t < ops.length; t++) {
					msg = new AddActionMessageImpl(ops[t].getName(), ops[t].needsConfirm(), ops[t].getTooltip());
					msg.write(out);
					if (ops[t] instanceof Test) {
						tester = ((Test) ops[t]).getTester();
					}
				}
				//
				// Send the TestSet
				//
				if (tester != null) {
					msg = new TestSetMessageImpl(tester.getTestNames(), tester.getTestDescriptions());
					msg.write(out);
				}
				//
				// Send the Source Files.
				//
				for (int t = 0; t < editorFiles.length; t++) {
					String current = editorFiles[t];
					if (ws.isJava(current)) {
						Message ed = new EditorMessageImpl(current, ws.getContents(current), ws.isJava(current), ws.isReadOnly(current),
								ws.isMonospaced(current));
						ed.write(out);
					}
				}
				//
				// Print a console message.
				//
				msg = new ConsoleMessageImpl("Output", "The game has begun.");
				msg.write(out);
				out.flush();
				//
				while (!myTeam.isFinished()) {
					//
					// Periodically update the client.
					//
					int[] testResults = myTeam.getTestResults();
					Anim[] testAnimResults = myTeam.getAnimatedTestResults();
					//
					msg = new UpdateClientStatisticsMessageImpl(0, myTeam.getClock().getDuration(), myTeam.getClock().getSecondsRemaining(),
							Message.UpdateClientStatistics.STATE_PROGRAMMING, myRound.getTeamsOnline(), myRound.getTeamCount(), testResults);
					msg.write(out);
					//
					// Write any animations to the client.
					//
					if ((testResults != null) && (testAnimResults != null)) {
						for (int t = 0; t < testAnimResults.length; t++) {
							if (testAnimResults[t] != null) {
								msg = new AnimationMessageImpl(t, testAnimResults[t]);
								msg.write(out);
							}
						}
					}
					//
					String[][] l = myTeam.getLines();
					for (int t = 0; t < l.length; t++) {
						msg = new ConsoleMessageImpl((l[t][0] == null ? "Output" : l[t][0]), l[t][1]);
						msg.write(out);
					}
					//
					out.flush();
					//
					// Read and process any messages
					//
					try {
						msg = factory.createMessage(in);
						switch (msg.getType()) {
						case Message.MSG_ACTION:
							Message.Action action = (Message.Action) msg;
							ops = myRound.getAssignment().getOperations();
							for (int t = 0; t < ops.length; t++) {
								if (ops[t].getName().equals(action.getAction())) {
									myTeam.doOperation(ops[t], new ContextImpl(action.getFileName(), action.getContents(), action.getIndex()));
									myTeam.addStatistics(action.getKeyStrokes(), action.getAction(), action.getContents().length());
								}
							}
							break;
						default:
							throw new IOException("Unknown message type : " + msg.getType());
						}
					} catch (SocketTimeoutException ex) {
						// Ingore.
					}
				}
			}
			//
			// We have left programming state, so now the game must be finished.
			//
			while (myTeam.isFinished()) {
				//
				// Periodically update the client.
				//
				msg = new UpdateClientStatisticsMessageImpl((myTeam.isScoreAvailable() ? myTeam.getFinalScore() : myTeam.getTheoreticalScore()),
						myTeam.getClock().getDuration(), myTeam.getClock().getSecondsRemaining(), Message.UpdateClientStatistics.STATE_FINISHED,
						myRound.getTeamsOnline(), myRound.getTeamCount(), null);
				msg.write(out);
				//
				//
				//
				String[][] l = myTeam.getLines();
				for (int t = 0; t < l.length; t++) {
					msg = new ConsoleMessageImpl((l[t][0] == null ? "Output" : l[t][0]), l[t][1]);
					msg.write(out);
				}
				//
				out.flush();
				//
				// Read and ignore any messages (hah !)
				//
				try {
					msg = factory.createMessage(in);
				} catch (SocketTimeoutException ex) {
					// Ingore.
				}
			}
		} catch (IOException ex) {
			myLog.error("Error in MessageHandler : " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			try {
				// Sign out
				if (loginOk)
					myTeam.isValidPassword(null);
				myLog.info("Closing connection.");
				mySocket.close();
			} catch (IOException ex) {
				myLog.error("Error closing Socket : " + ex.getMessage());
			}
		}
	}

}
