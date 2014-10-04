package nl.moj.workspace.factory;

import java.io.IOException;

import nl.ctrlaltdev.io.OutputRedirector;
import nl.moj.model.Workspace;
import nl.moj.process.ProcessPool.ProcessListener;

/**
 * The WorkspaceFactory allows the creation of various types of Workspaces.
 * Typically a Remote and Local workspace.
 */		

public interface WorkspaceFactory {

	/**
	 * Creates a new Workspace.
	 * @param name the name of this workspace.
	 * @param target an output redirector target for the console output of this Workspace. 
	 * @param lst the process listener to get notifications of Operations (queued,executing,finished)
	 * @return a new Workspace.
	 * @throws IOException If the workspace could not be created.
	 */
			
	public Workspace createWorkspace(String name,OutputRedirector.Target target,ProcessListener lst) throws IOException;
	
}
