package nl.moj.workspace.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import nl.moj.model.Operation;
import nl.moj.operation.ContextImpl;

public class PerformMessage extends AbstractWorkspaceMessage implements Message.Perform {

	private int myIndex;
	private String myOperationName;
	private String[] names;
	private String[] values;
	
	public PerformMessage(String operationName,Operation.Context ctx) {
		super(Message.MSG_PERFORM);
		myOperationName=operationName;
		names=ctx.getNames();
		myIndex=ctx.getIndex();
		values=new String[names.length];
		for (int t=0;t<names.length;t++) {
			values[t]=ctx.getContents(names[t]);
		}
 	}
	
	public PerformMessage(int type,DataInput in) throws IOException {
		super(type,in);
		if (type!=Message.MSG_PERFORM) throw new IOException("Incorrect Type"); 
		myOperationName=in.readUTF();
		myIndex=in.readInt();
		int cnt=in.readInt();
		names=new String[cnt];
		values=new String[cnt];
		for (int t=0;t<cnt;t++) {
			names[t]=in.readUTF();
			values[t]=in.readUTF();
		}
	}
	
	public void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeUTF(myOperationName);
		out.writeInt(myIndex);
		out.writeInt(names.length);
		for (int t=0;t<names.length;t++) {
			out.writeUTF(names[t]);
			out.writeUTF(values[t]);
		}
	}
	
	public Operation.Context getContext() {
		return new ContextImpl(names,values,myIndex);
	}
	public int getIndex() { return myIndex;  }
	public String getOperationName() { return myOperationName;	}
	

}