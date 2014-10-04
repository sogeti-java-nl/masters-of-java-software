package nl.moj.model;

import java.io.File;

import nl.moj.process.ProcessPool;

/**
 * An operation is an action that a Team can take. Like saving, compiling or testing.
 * Each operation should implement this interface. 
 */
public interface Operation {
	
	/** callback interface for parameter retrieval at construction time */
	public interface Configuration {
		public File   getParentDir(Operation which);
		/** returns the specified configuration parameter or null if it does not exist. */
		public String getParameter(Operation which,String name);
	}
	
	/** interface describing any parameters for the operation */ 
	public interface Context {
		public int getIndex(); 
		public String[] getNames();
		public String getContents(String name);			  
	}
	
	/** constant indicating everything */
	public static final int IDX_EVERYTHING=-1;
	
	/** returns the name of this operation */
	public String getName();
	/** returns the tooltip (for display on the client ) */
	public String getTooltip();
	/** return true if this operation needs to be confirmed by the team */
	public boolean needsConfirm();
	/** 
	 * actually performs the operation.
	 * @param ws the workspace to perform the operation.
	 * @param lst the process listener to notify of progress
	 * @param ctx the context of this operation. 
	 */
	public void perform(Workspace.Internal ws,ProcessPool.ProcessListener lst,Context ctx);

	/**
	 * If this returns true this is the submit action and will end the round for the Team.
	 */
	public boolean isSubmit();
	
}
