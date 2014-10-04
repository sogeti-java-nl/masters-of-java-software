package nl.moj.client.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class EditorMessageImpl extends AbstractMessage implements Message.Editor {
	
	private String myFile,myContents;
	private boolean myIsJava,myIsReadOnly,myIsMonospaced;
	
	public EditorMessageImpl(String fileName,String contents,boolean isJava,boolean isReadOnly,boolean monospaced) {
		super(Message.MSG_EDITOR);
		myFile=fileName;
		myContents=contents;
		myIsJava=isJava;
		myIsReadOnly=isReadOnly;
		myIsMonospaced=monospaced;
	}
	
	public EditorMessageImpl(int type,DataInput in) throws IOException {
		super(type,in);
		if (type!=Message.MSG_EDITOR) throw new IOException("Incorrect Type"); 
		myFile=in.readUTF();
		myContents=in.readUTF();
		myIsJava=in.readBoolean();
		myIsReadOnly=in.readBoolean();
		myIsMonospaced=in.readBoolean();
	}
	
	public void write(DataOutput out) throws IOException {
        super.write(out);
        out.writeUTF(myFile);
		out.writeUTF(myContents);
		out.writeBoolean(myIsJava);
		out.writeBoolean(myIsReadOnly);
		out.writeBoolean(myIsMonospaced);
    }
	
	public String getContents() { return myContents;    }
	public String getFileName() { return myFile;	}
	public boolean isJava() { return myIsJava; }
	public boolean isReadOnly() { return myIsReadOnly;	}
	public boolean isMonospaced() { return myIsMonospaced;	}

}
