package nl.moj.operation;

import java.io.IOException;

import nl.moj.model.Operation;
import nl.moj.model.Workspace;
import nl.moj.process.ProcessPool;
import nl.moj.process.ProcessPool.ProcessListener;

/**
 * Save Operations allows for a 'Save' Button to save the work.
 * However, the operation itself does nothing because the actual saving
 * is done in TeamImpl.doOperation. 
 */

public class Save extends AbstractOperation {

	private static class SaveWrapper extends AbstractOperationRunnable {
		public SaveWrapper(Operation parent,Workspace.Internal tm,Context ctx) {
			super(parent,tm,ctx);
		}
		public void run() {
			try {
				//
				save();
				//
			} catch (IOException ex) {
				getWorkspace().getTarget().append(null,"Failed Save : "+ex.getMessage());
			}
		}
	}


    public Save(ProcessPool pool) {
        super("Save","Saves your work",false,pool);
    }

	public boolean isSubmit() {
    	return false;
	}
	
	public void perform(Workspace.Internal ws, ProcessListener lst, Context ctx) {
		//
		getPool().execute(new SaveWrapper(this,ws,ctx),lst);		
		//
	}

}
