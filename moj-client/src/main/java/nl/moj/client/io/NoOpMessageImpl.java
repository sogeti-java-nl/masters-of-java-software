package nl.moj.client.io;

import java.io.DataInput;
import java.io.IOException;

public class NoOpMessageImpl extends AbstractMessage implements Message.NoOp {

	public NoOpMessageImpl() {
		super(Message.MSG_NOOP);
	}

	public NoOpMessageImpl(int type,DataInput in) throws IOException {
		super(type,in);
		if (type!=Message.MSG_NOOP) throw new IOException("Incorrect Type"); 
	}	
	
}
