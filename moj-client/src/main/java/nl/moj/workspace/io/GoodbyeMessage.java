package nl.moj.workspace.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class GoodbyeMessage extends AbstractWorkspaceMessage implements Message.Goodbye {
	
	private boolean dispose;
	
	public GoodbyeMessage(boolean dispose) {
		super(Message.MSG_GOODBYE);
		this.dispose=dispose;
	}

	public GoodbyeMessage(int type,DataInput in) throws IOException {
		super(type,in);
		if (type!=Message.MSG_GOODBYE) throw new IOException("Incorrect Type");
		dispose=in.readBoolean();
	}
	
	public void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeBoolean(dispose);
	}	
	
	public boolean isDispose() {
		return dispose;
	}
	
}
