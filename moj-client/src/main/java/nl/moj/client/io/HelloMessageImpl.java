package nl.moj.client.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * The Hello message is the first message sent.
 * It identifies the user and protocol version.
 */

public class HelloMessageImpl extends AbstractMessage implements Message.Hello {

	private String myUser,myPass;
	private int myVersion;
	
	public HelloMessageImpl(String username,String password) {
		super(Message.MSG_HELLO);
		myUser=username;
		myPass=password;
	}
	
	public HelloMessageImpl(int type,DataInput in) throws IOException {
		super(type,in);
		if (type!=Message.MSG_HELLO) throw new IOException("Incorrect Type");
		myVersion=in.readInt(); 
		myUser=in.readUTF();
		myPass=in.readUTF();
	}
	
	public void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeInt(Message.PROTOCOLVERSION);
		out.writeUTF(myUser);
		out.writeUTF(myPass);
	}
	
	public String getTeamName() { return myUser;  }
	public String getPassword() { return myPass;	}
	public int    getProtocolVersion() { return myVersion; }

}
