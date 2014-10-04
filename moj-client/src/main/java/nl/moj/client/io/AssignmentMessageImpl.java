package nl.moj.client.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class AssignmentMessageImpl extends AbstractMessage implements Message.Assignment { 	

	private String myName,myAuthor;
	private byte[] myIcon,mySponsor;

	public AssignmentMessageImpl(nl.moj.model.Assignment a) {
		super(Message.MSG_ASSIGNMENT);
		myName=((a.getDisplayName()==null) ? "Unknown" : a.getDisplayName());
		myAuthor=((a.getAuthor()==null) ? "Unknown" : a.getAuthor());
		myIcon=((a.getIcon()==null) ? null : a.getIcon());
		mySponsor=((a.getSponsorImage()==null) ? null : a.getSponsorImage());
	}

	public AssignmentMessageImpl(int type,DataInput in) throws IOException {
		super(type,in);
		if (type!=Message.MSG_ASSIGNMENT) throw new IOException("Incorrect Type"); 
		myName=in.readUTF();
		myAuthor=in.readUTF();
		int l=in.readInt();
		if (l>=0) {
			myIcon=new byte[l];
			in.readFully(myIcon);
		} 
		l=in.readInt();
		if (l>=0) {
			mySponsor=new byte[l];
			in.readFully(mySponsor);			
		}
	}

	public void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeUTF(myName);
		out.writeUTF(myAuthor);
		if (myIcon!=null) {
			out.writeInt(myIcon.length);
			out.write(myIcon);
		} else {
			out.writeInt(-1);
		}
		if (mySponsor!=null) {
			out.writeInt(mySponsor.length);
			out.write(mySponsor);
		} else {
			out.writeInt(-1);
		}
	}

	public String getName() { return myName;  }
	public String getAuthor() { return myAuthor;	}
	public byte[] getIcon() { return myIcon; }
	public byte[] getSponsorImage() { return mySponsor; }
}
