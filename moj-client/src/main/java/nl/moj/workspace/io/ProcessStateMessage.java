package nl.moj.workspace.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import nl.moj.client.anim.Anim;
import nl.moj.client.anim.LayeredAnim;
import nl.moj.model.Tester;
import nl.moj.test.TesterImpl;


public class ProcessStateMessage extends AbstractWorkspaceMessage implements Message.ProcessState {
	
	private int state;
	private String operationName;
	private boolean success;
	private Tester.TestResult tst;
	
	public ProcessStateMessage(int state,String operationName,boolean success,Tester.TestResult testResults) {
		super(Message.MSG_PROCESSSTATE);
		this.state=state;
		this.operationName=operationName;
		this.success=success;
		this.tst=testResults;		
	}
	
	public ProcessStateMessage(int type,DataInput in) throws IOException {
		super(type,in);
		if (type!=Message.MSG_PROCESSSTATE) throw new IOException("Incorrect Type");
		state=in.readInt();
		operationName=in.readUTF();
		success=in.readBoolean();
		if (in.readBoolean()) {
			//
			int rs=in.readInt();
			int[] results=new int[rs];
			for (int t=0;t<results.length;t++) {
				results[t]=in.readInt();
			}
			//
			Anim[] anim=null;
			if (in.readBoolean()) {
				int as=in.readInt();
				anim=new Anim[as];
				for (int t=0;t<anim.length;t++) {
					if (in.readBoolean()) {
						anim[t]=new LayeredAnim();
						anim[t].read(in);
					} else {
						anim[t]=null;
					}
				}
			}
			//
			tst=new TesterImpl.TestResultImpl(results,anim);
			//
		} else {
			tst=null;
		}
	}
	
	public void write(DataOutput out) throws IOException {
		super.write(out);
		out.writeInt(state);
		out.writeUTF(operationName);
		out.writeBoolean(success);
		//
		if (tst==null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			//
			int[] r=tst.getResults();
			out.writeInt(r.length);
			for (int t=0;t<r.length;t++) out.writeInt(r[t]);
			//
			Anim[] anim=tst.getAnimationOutput();
			if (anim==null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				out.writeInt(anim.length);
				for (int t=0;t<anim.length;t++) {
					if (anim[t]!=null) {
						out.writeBoolean(true);
						anim[t].write(out);					
					} else {
						out.writeBoolean(false);
					}
				}
			}
		}
	}
	
	public String getOperationName() {
		return operationName;
	}
	public boolean isQueued() {
		return (state==STATE_QUEUED);
	}
	public boolean isExecuting() {
		return (state==STATE_EXECUTING);		
	}
	public boolean isFinished() {
		return (state==STATE_FINISHED);				
	}
	public boolean wasSuccess() {
		return success;		
	}
	public Tester.TestResult getTestResults() {
		return tst;
	}

}
