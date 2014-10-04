package nl.moj.client.io;

import java.io.DataInput;
import java.io.IOException;

public class GoodbyeMessageImpl extends AbstractMessage implements Message.GoodBye {
	
	public GoodbyeMessageImpl() {
		super(Message.MSG_GOODBYE);
	}

	public GoodbyeMessageImpl(int type,DataInput in) throws IOException {
		super(type,in);
		if (type!=Message.MSG_GOODBYE) throw new IOException("Incorrect Type"); 
	}
}
