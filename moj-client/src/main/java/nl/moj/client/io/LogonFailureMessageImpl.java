package nl.moj.client.io;

import java.io.DataInput;
import java.io.IOException;

public class LogonFailureMessageImpl extends AbstractMessage implements Message {

	public LogonFailureMessageImpl() {
		super(Message.MSG_UNKNOWNUSERPASSWORD);
	}
	public LogonFailureMessageImpl(int type,DataInput in) throws IOException {		
		super(type,in);
	}
	

}
