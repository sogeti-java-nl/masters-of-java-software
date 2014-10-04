package nl.moj.workspace.io;

import java.io.DataInput;
import java.io.IOException;

public class WorkspaceMessageFactory {

	public Message createMessage(DataInput in) throws IOException {
		int type=in.readInt();
		switch (type) {
			case Message.MSG_ASSIGNMENT : return new AssignmentMessage(type,in); 
			case Message.MSG_GOODBYE : return new GoodbyeMessage(type,in); 
			case Message.MSG_PERFORM : return new PerformMessage(type,in); 
			case Message.MSG_REQ_CONTENTS : return new ContentsRequest(type,in);
			//
			case Message.MSG_CONSOLE      : return new ConsoleMessage(type,in);
			case Message.MSG_PROCESSSTATE : return new ProcessStateMessage(type,in);
			case Message.MSG_REP_CONTENTS : return new ContentsReply(type,in);
			default : throw new IOException("Unsupported Message type : "+type);
		}
	}
}
