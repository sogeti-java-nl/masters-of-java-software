package nl.moj.workspace;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.ctrlaltdev.io.OutputRedirector;
import nl.ctrlaltdev.ioc.ApplicationBuilder;
import nl.moj.assignment.JarFileAssignment;
import nl.moj.model.Operation;
import nl.moj.model.Team;
import nl.moj.model.Tester;
import nl.moj.process.ProcessPool;
import nl.moj.workspace.io.ConsoleMessage;
import nl.moj.workspace.io.ContentsReply;
import nl.moj.workspace.io.Message;
import nl.moj.workspace.io.Message.Assignment;
import nl.moj.workspace.io.Message.ContentsRequest;
import nl.moj.workspace.io.Message.Goodbye;
import nl.moj.workspace.io.Message.Perform;
import nl.moj.workspace.io.ProcessStateMessage;

public class WorkspaceClientServerImpl implements WorkspaceClientServer, OutputRedirector.Target, ProcessPool.ProcessListener {

	private static Logger log = Logger.getLogger("WorkspaceCS");

	private ProcessPool processPool;
	private ApplicationBuilder applicationBuilder;
	private LocalWorkspace ws;

	public WorkspaceClientServerImpl(ProcessPool p, ApplicationBuilder b) {
		this.processPool = p;
		this.applicationBuilder = b;
	}

	public boolean isInitial() {
		return false;
	}

	public Message[] onAssignment(Assignment msg) throws Exception {
		log.fine("Copying assignment..");
		File tmp = File.createTempFile("moj", ".jar");
		OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp));
		try {
			out.write(msg.getJarAssignment());
		} finally {
			out.close();
		}
		//
		log.fine("Reading assignment..");
		JarFileAssignment assignment = new JarFileAssignment(tmp, processPool, applicationBuilder);
		//
		log.fine("Creating local Workspace..");
		ws = new LocalWorkspace(msg.getTeamName(), this, this);
		//
		log.fine("Loading assignment into workspace..");
		ws.loadAssignment(assignment, msg.isResumeMode());
		//
		log.info("Workspace created (resume=" + msg.isResumeMode() + ")");
		return getAllMsgs();
	}

	public Message[] onGoodbye(Goodbye msg) {
		if (msg.isDispose())
			log.info("Goodbye - Disposing Workspace.");
		else
			log.info("Goodbye - Suspending Workspace.");
		if (msg.isDispose())
			ws.dispose();
		else
			ws.suspend();
		return getAllMsgs();
	}

	public Message[] onNoOp() {
		return getAllMsgs();
	}

	public Message[] onPerform(Perform msg) {
		if (!ws.isPerforming()) {
			Operation op = ws.getOperationByName(msg.getOperationName());
			if (op != null) {
				ws.perform(op, msg.getContext());
			}
		}
		return getAllMsgs();
	}

	public Message[] onRequestContents(ContentsRequest req) throws IOException {
		add(new ContentsReply(req.getName(), ws.getContents(req.getName())));
		return getAllMsgs();
	}

	//
	//
	//

	private List<Message> msgs = new ArrayList<>();

	private synchronized void add(Message msg) {
		msgs.add(msg);
	}

	private synchronized Message[] getAllMsgs() {
		Message[] result = msgs.toArray(new Message[msgs.size()]);
		msgs.clear();
		return result;
	}

	//
	//
	//

	public void append(String console, String content) {
		add(new ConsoleMessage(console == null ? "Output" : console, content));
	}

	//
	//
	//

	public void complete(Runnable r) {
		String name = "Unknown";
		boolean success = false;
		Tester.TestResult testResults = null;
		//
		if (r instanceof Team.CompileResults) {
			Team.CompileResults cr = (Team.CompileResults) r;
			name = cr.getOperation().getName();
			success = cr.wasSuccess();
		} else if (r instanceof Team.TestResults) {
			Team.TestResults tr = (Team.TestResults) r;
			name = tr.getOperation().getName();
			testResults = tr.getTestResults();
			success = (testResults == null ? false : testResults.isOk());
		} else if (r instanceof Team.Results) {
			Team.Results tr = (Team.Results) r;
			name = tr.getOperation().getName();
		}
		//
		add(new ProcessStateMessage(Message.ProcessState.STATE_FINISHED, name, success, testResults));
	}

	public void executing(Runnable r) {
		String name = "Unknown";
		if (r instanceof Team.Results) {
			Team.Results cr = (Team.Results) r;
			name = cr.getOperation().getName();
		}
		//
		add(new ProcessStateMessage(Message.ProcessState.STATE_EXECUTING, name, false, null));
	}

	public void queued(Runnable r) {
		String name = "Unknown";
		if (r instanceof Team.Results) {
			Team.Results cr = (Team.Results) r;
			name = cr.getOperation().getName();
		}
		//
		add(new ProcessStateMessage(Message.ProcessState.STATE_QUEUED, name, false, null));
		//
	}
}
