package nl.moj.client.io;

import java.io.DataInput;
import java.io.IOException;

/**
 * MessageFactory reads a message from the DataInput stream and converts them into the
 * appropriate objects.
 * @author E.Hooijmeijer
 */
public class MessageFactory {

	public Message createMessage(DataInput in) throws IOException {
		int type=in.readInt();
		switch (type) {
			case Message.MSG_HELLO  : return new HelloMessageImpl(type,in);
			case Message.MSG_ACTION : return new ActionMessageImpl(type,in);
			case Message.MSG_MULTI_ACTION : return new ActionMessagesImpl(type,in);
			case Message.MSG_ADDACTION : return new AddActionMessageImpl(type,in);
			case Message.MSG_CONSOLE : return new ConsoleMessageImpl(type,in);
			case Message.MSG_EDITOR : return new EditorMessageImpl(type,in);
			case Message.MSG_UPDATE_CLIENT : return new UpdateClientStatisticsMessageImpl(type,in);
			case Message.MSG_UNKNOWNUSERPASSWORD : return new LogonFailureMessageImpl(type,in);
			case Message.MSG_TESTSET : return new TestSetMessageImpl(type,in);
			case Message.MSG_PROTOCOLVERSIONMISMATCH : return new ProtocolVersionMismatchMessageImpl(type,in);
			case Message.MSG_ANIMATION : return new AnimationMessageImpl(type,in);
			case Message.MSG_NOOP : return new NoOpMessageImpl(type,in);
			case Message.MSG_GOODBYE : return new GoodbyeMessageImpl(type,in);
			case Message.MSG_ASSIGNMENT : return new AssignmentMessageImpl(type,in);
			default : throw new IOException("Unsupported Message type : "+type);
		}
	}

}