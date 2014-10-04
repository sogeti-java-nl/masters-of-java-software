package nl.moj.workspace;

import java.io.IOException;

import nl.moj.workspace.io.Message;

public interface WorkspaceClientServer {

	public boolean isInitial();
	
	public Message[] onAssignment(Message.Assignment msg) throws Exception;
	public Message[] onPerform(Message.Perform msg);	
	public Message[] onRequestContents(Message.ContentsRequest req) throws IOException;
	public Message[] onGoodbye(Message.Goodbye msg);
	
	public Message[] onNoOp();
	
}
