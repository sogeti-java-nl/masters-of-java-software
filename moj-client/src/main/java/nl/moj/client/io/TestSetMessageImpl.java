package nl.moj.client.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class TestSetMessageImpl extends AbstractMessage implements Message.TestSet {

	private String[] myNames;
	private String[] myDescriptions;

	public TestSetMessageImpl(String[] names,String[] descriptions) {
		super(Message.MSG_TESTSET);
		if(names.length!=descriptions.length) throw new RuntimeException("names do not match descriptions.");
		myNames=names;
		myDescriptions=descriptions;
	}
	
	public TestSetMessageImpl(int type,DataInput in) throws IOException {
		super(type,in);
		if (type!=Message.MSG_TESTSET) throw new IOException("Incorrect Type"); 
		int myCnt=in.readInt();
		myNames=new String[myCnt];
		myDescriptions=new String[myCnt];
		for (int t=0;t<myCnt;t++) {
			myNames[t]=in.readUTF();
			myDescriptions[t]=in.readUTF();
		}
	}
	
	public void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeInt(myNames.length);
		for (int t=0;t<myNames.length;t++) {
			out.writeUTF(myNames[t]);
			out.writeUTF(myDescriptions[t]);
		}
	}
	
	public int getCount() {
		return myNames.length;
	}
	public String getDescription(int nr) {
		return myDescriptions[nr];
	}
	public String getName(int nr) {
		return myNames[nr];
	}
	

}
