package nl.moj.operation;

import nl.moj.model.Operation;
import nl.moj.model.Workspace;
import nl.moj.process.ProcessPool;
import nl.moj.process.ProcessPool.ProcessListener;

/**
 *
 */
public abstract class AbstractOperation implements Operation {

	private ProcessPool myPool;
	private boolean myConfirm;
	private String myName,myTooltip;

	public AbstractOperation(String name,String tooltip,boolean confirm,ProcessPool pool) {
		myName=name;
		myPool=pool;
		myTooltip=tooltip;
		myConfirm=confirm;
	}
	
	public String getName() {
		return myName;		
	}
	
	public String getTooltip() {
		return myTooltip;
	}
	
	protected ProcessPool getPool() {
		return myPool;
	}
	
	public boolean needsConfirm() {
		return myConfirm;
	}
	
	public abstract void perform(Workspace.Internal ws, ProcessListener lst,Context ctx);

}
