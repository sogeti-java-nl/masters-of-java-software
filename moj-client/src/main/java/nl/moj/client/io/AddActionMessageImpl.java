package nl.moj.client.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */

public class AddActionMessageImpl extends AbstractMessage implements Message.AddAction {

	private String myAction,myTT;
	private boolean myConfirm;
	
	public AddActionMessageImpl(String action,boolean confirm,String tooltip) {
		super(Message.MSG_ADDACTION);
		myAction=action;
		myConfirm=confirm;
		myTT=tooltip;
	}
	
	public AddActionMessageImpl(int type,DataInput in) throws IOException {
		super(type,in);
		if (type!=Message.MSG_ADDACTION) throw new IOException("Incorrect Type"); 
		myAction=in.readUTF();
		myConfirm=in.readBoolean();
		myTT=in.readUTF();
	}
	
	public void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeUTF(myAction);
		out.writeBoolean(myConfirm);
		out.writeUTF(myTT);
	}

	public String getAction() {
        return myAction;
    }
	public boolean mustConfirm() {
    	return myConfirm;
	}
	public String getToolTip() {
        return myTT;
    }


}
