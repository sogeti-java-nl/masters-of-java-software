package nl.moj.workspace.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ContentsRequest extends AbstractWorkspaceMessage implements Message.ContentsRequest {

	private String myName;
	
	public ContentsRequest(String name) {
		super(Message.MSG_REQ_CONTENTS);
		myName=name;
	}
	
	public ContentsRequest(int type,DataInput in) throws IOException {
		super(type,in);
		if (type!=Message.MSG_REQ_CONTENTS) throw new IOException("Incorrect Type"); 
		myName=in.readUTF();
	}
	
	public void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeUTF(myName);
	}
	
	public String getName() { return myName;  }

}

