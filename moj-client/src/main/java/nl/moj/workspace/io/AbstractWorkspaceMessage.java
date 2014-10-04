package nl.moj.workspace.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import nl.moj.client.io.Message;

public abstract class AbstractWorkspaceMessage implements Message {

	private static int lastId=0;

	private static synchronized int nextId() {
		return lastId++;
	}

	private int myType;
	private int myId;
	private int mySourceId;

	public AbstractWorkspaceMessage(int type) {
		myType=type;
		myId=nextId();
		mySourceId=-1;
	}

	public AbstractWorkspaceMessage(int type,int sourceId) {
		myType=type;
		myId=nextId();
		mySourceId=sourceId;
	}
	
	public AbstractWorkspaceMessage(int type,DataInput in) throws IOException {
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

