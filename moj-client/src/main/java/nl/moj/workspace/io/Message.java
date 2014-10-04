package nl.moj.workspace.io;

import java.io.DataOutput;
import java.io.IOException;

import nl.moj.model.Operation;
import nl.moj.model.Tester;

public interface Message {

	/** C->S : Initialises the workspace with the assignment */
	public interface Assignment extends Message {
		public String getTeamName();
		public boolean isResumeMode();
		public byte[] getJarAssignment();
	}
	
	/** C->S : Performs an operation */
	public interface Perform extends Message {
		public String getOperationName();
		public Operation.Context getContext();
	}
	
	/** C->S : Request for the contents of a resource */
	public interface ContentsRequest extends Message {
		public String getName();
	}
	/** S->C : Reply to a request for the contents of a resource */
	public interface ContentsReply extends Message {
		public String getName();
		public String getContents();
	}
	/** S->C : A console message */
	public interface Console extends Message {
		public String getContext();
		public String getContent();
	}
	/** S->C : A process state message */
	public interface ProcessState extends Message {
		public static final int STATE_QUEUED=0;
		public static final int STATE_EXECUTING=1;
		public static final int STATE_FINISHED=2;
		public String getOperationName();
		public boolean isQueued();
		public boolean isExecuting();
		public boolean isFinished();
		public boolean wasSuccess();
		public Tester.TestResult getTestResults();
	}
	/** C->S : End of game - Workspace may be suspended or disposed */
	public interface Goodbye extends Message {
		//
		public boolean isDispose();
		//
	}	
	
	public static final int MSG_ASSIGNMENT=0;
	public static final int MSG_PERFORM=1;
	public static final int MSG_REQ_CONTENTS=2;
	public static final int MSG_REP_CONTENTS=3;
	public static final int MSG_CONSOLE=4;
	public static final int MSG_PROCESSSTATE=5;
	public static final int MSG_GOODBYE=6;
	
	/** the ID of this message */
	public int getId();
	/** the ID of the message this message is a reply to or -1 */
	public int getSourceId();
	/** the type of this message */
	public int getType();
	/** writes the message to the dataoutput. */
	public void write(DataOutput out) throws IOException ;		
		
}
