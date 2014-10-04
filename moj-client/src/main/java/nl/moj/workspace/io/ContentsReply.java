package nl.moj.workspace.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ContentsReply extends AbstractWorkspaceMessage implements Message.ContentsReply {

	private String myName,myContents;
	
	public ContentsReply(String name,String contents) {
		super(Message.MSG_REP_CONTENTS);
		myName=name;
		myContents=contents;
	}
	
	public ContentsReply(int type,DataInput in) throws IOException {
		super(type,in);
		if (type!=Message.MSG_REP_CONTENTS) throw new IOException("Incorrect Type"); 
		myName=in.readUTF();
		myContents=in.readUTF();
	}
	
	public void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeUTF(myName);
		out.writeUTF(myContents);
	}
	
	public String getName() { return myName;  }
	public String getContents() { return myContents;	}

}

