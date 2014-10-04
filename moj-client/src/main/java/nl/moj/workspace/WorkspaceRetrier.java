package nl.moj.workspace;

import java.io.IOException;

import nl.moj.model.Assignment;
import nl.moj.model.Operation;
import nl.moj.model.Workspace;
import nl.moj.model.Operation.Context;

/**
 * Wrapper around RemoteWorkspaceClient to allow graceful 
 * recovery from a failing server (only once !)
 * @author E.Hooijmeijer
 */

public class WorkspaceRetrier implements Workspace {

	private RemoteWorkspaceClient ws;
	
	public WorkspaceRetrier(RemoteWorkspaceClient ws) {
		this.ws=ws;
	}
	
	public String getName() {
		return ws.getName();
	}

	public void loadAssignment(Assignment a, boolean resume) throws IOException {
		try {
			ws.loadAssignment(a,resume);
		} catch (OperationFailedException x) {
			ws.loadAssignment(a,resume);
		}
	}

	public Operation[] getAllOperations() {
		try {
			return ws.getAllOperations();
		} catch (OperationFailedException x) {
			return ws.getAllOperations();
		}
	}

	public Operation getOperationByName(String name) {
		try {
			return ws.getOperationByName(name);
		} catch (OperationFailedException x) {
			return ws.getOperationByName(name);
		}
	}

	public void perform(Operation op, Context ctx) {
		try {
			ws.perform(op,ctx);
		} catch (OperationFailedException x) {
			ws.perform(op,ctx);
		}
	}

	public String[] getEditorFiles() {
		try {
			return ws.getEditorFiles();
		} catch (OperationFailedException x) {
			return ws.getEditorFiles();
		}
	}

	public String getContents(String file) throws IOException {
		try {
			return ws.getContents(file);
		} catch (OperationFailedException x) {
			return ws.getContents(file);
		}
		
	}

	public boolean isJava(String file) {
		try {
			return ws.isJava(file);
		} catch (OperationFailedException x) {
			return ws.isJava(file);
		}
	}

	public boolean isReadOnly(String file) {
		try {
			return ws.isReadOnly(file);
		} catch (OperationFailedException x) {
			return ws.isReadOnly(file);
		}
	}

	public boolean isMonospaced(String file) {
		try {
			return ws.isMonospaced(file);
		} catch (OperationFailedException x) {
			return ws.isMonospaced(file);
		}
	}

	public void update() throws IOException {
		try {
			ws.update();
		} catch (OperationFailedException x) {
			ws.update();
		}
	}
	
	public void dispose() {
		try {
			ws.dispose();
		} catch (OperationFailedException x) {
			ws.dispose();
		}
	}
	
	public void suspend() {
		try {
			ws.suspend();
		} catch (OperationFailedException x) {
			ws.suspend();
		}
	}

}
