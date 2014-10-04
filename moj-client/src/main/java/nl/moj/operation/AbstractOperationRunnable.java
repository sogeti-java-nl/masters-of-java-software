package nl.moj.operation;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.moj.compile.Compiler;
import nl.moj.model.Operation;
import nl.moj.model.Team;
import nl.moj.model.Workspace;
import nl.moj.process.ProcessPool;

/**
 * 
 */
public abstract class AbstractOperationRunnable implements Runnable, ProcessPool.ProcessListener,Team.Results {
	
	private Operation owner;
	private Workspace.Internal workspace;
	private Operation.Context context;
	
	public AbstractOperationRunnable(Operation owner,Workspace.Internal ws,Operation.Context ctx) {
		this.owner=owner;
		this.workspace=ws;
		this.context=ctx;
	} 
	
	public void complete(Runnable r) {
		//
	}
	public void executing(Runnable r) {
		//
	}
	public void queued(Runnable r) {
		//
	}
	
	public abstract void run();
	
	protected Workspace.Internal getWorkspace() { return workspace; }
	protected Operation.Context getContext() { return context; }
	public Operation getOperation() { return owner; }
	
	protected void save() throws IOException {
		String[] files=context.getNames();
		for (int t=0;t<files.length;t++) {
			String src=getWorkspace().getContents(files[t]);
			String cmp=context.getContents(files[t]);
			if (!src.equals(cmp)) {
				getWorkspace().save(files[t],cmp);
			}
		}
	}
	
	protected boolean compile() throws IOException {
		try {
			//
			return new Compiler().compile(getWorkspace());
			//
		} catch (ClassNotFoundException ex) {
			//
			// If a ClassNotFoundException is thrown then the compiler class in tools.jar could not be found.
			// Both the presence of tools.jar and the correct method should be checked on startup so these
			// errors should not happen at runtime.  
			//
			Logger.getLogger("Compiler").log(Level.SEVERE,ex.getMessage()+" : Is the tools.jar (J2SDK) on the classpath ?");
			//
			return false;
		} catch (NoSuchMethodException ex) {
			//
			// If a NoSuchMethodException is thrown then the compiler does not support the method we're using. 
			// Both the presence of tools.jar and the correct method should be checked on startup so these
			// errors should not happen at runtime.
			//  
			Logger.getLogger("Compiler").log(Level.SEVERE,ex.getMessage()+" : Are you using JDK 1.4.2_04 or better ?");
			//
			return false;
		}
	}

}
