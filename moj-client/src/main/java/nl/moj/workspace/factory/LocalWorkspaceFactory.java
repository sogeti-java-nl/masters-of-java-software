package nl.moj.workspace.factory;

import java.io.IOException;

import nl.ctrlaltdev.io.OutputRedirector.Target;
import nl.moj.model.Workspace;
import nl.moj.process.ProcessPool.ProcessListener;
import nl.moj.workspace.LocalWorkspace;

/** 
 * Local workspace implementation : Workspace in the same Virtual Machine.
 * Good up to 32 teams on a fast machine. 
 */
public class LocalWorkspaceFactory implements WorkspaceFactory {
	
	public Workspace createWorkspace(String name, Target target, ProcessListener lst) throws IOException {
		return new LocalWorkspace(name,target,lst);
	}
	
}