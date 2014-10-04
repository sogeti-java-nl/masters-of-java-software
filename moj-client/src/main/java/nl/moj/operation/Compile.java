package nl.moj.operation;

import java.io.IOException;

import nl.moj.model.Operation;
import nl.moj.model.Team;
import nl.moj.model.Workspace;
import nl.moj.process.ProcessPool;

/**
 * Compile compiles the code.
 */

public class Compile extends AbstractOperation {

	private static class InprocessCompiler extends AbstractOperationRunnable implements Team.CompileResults {
		private boolean ok;
		public InprocessCompiler(Operation parent,Workspace.Internal tm,Context ctx) {
			super(parent,tm,ctx);
		}
		public void run() {
			try {
				//
				ok=false;
				//
				save();
				//
				Workspace.Internal ws=getWorkspace();
				//
				ws.clearBinary();
				//
				ok=compile();
				//
			} catch (IOException ex) {
				getWorkspace().getTarget().append(null,"Failed Compilation : "+ex.getMessage());
			}
        }
        public boolean wasSuccess() {
            return ok;
        }
	}

	public Compile(ProcessPool pool) {
		super("Compile","Compiles the source",false,pool);
	}
	
	public boolean isSubmit() {
		return false;
	}
	

	public void perform(Workspace.Internal ws,ProcessPool.ProcessListener lst,Context ctx) {
		//
		getPool().execute(new InprocessCompiler(this,ws,ctx),lst);
		//
	}

}
