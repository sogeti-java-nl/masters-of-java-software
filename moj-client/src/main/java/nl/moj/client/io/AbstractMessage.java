package nl.moj.client.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * 
 */

public abstract class AbstractMessage implements Message {

	private static int lastId=0;

	private static synchronized int nextId() {
		return lastId++;
	}

	private int myType;
	private int myId;
	private int mySourceId;

	public AbstractMessage(int type) {
		myType=type;
		myId=nextId();
		mySourceId=-1;
	}

	public AbstractMessage(int type,int sourceId) {
		myType=type;
		myId=nextId();
		mySourceId=sourceId;
	}
	
	public AbstractMessage(int type,DataInput in) throws IOException {
		myType=type;
		myId=in.readInt();
		mySourceId=in.readInt();		
	}
	
	public void write(DataOutput out) throws IOException {
		out.writeInt(myType);
		out.writeInt(myId);
		out.writeInt(mySourceId);
	}
	
	public int getId() {
        return myId;
    }
	public int getSourceId() {
		return mySourceId;
	}
	public int getType() {
		return myType;
	}

}
