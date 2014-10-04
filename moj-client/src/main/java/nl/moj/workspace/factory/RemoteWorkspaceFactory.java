package nl.moj.workspace.factory;

import java.io.IOException;
import java.net.Socket;

import nl.ctrlaltdev.io.OutputRedirector.Target;
import nl.moj.model.Workspace;
import nl.moj.process.ProcessPool.ProcessListener;
import nl.moj.workspace.LoadBalancer;
import nl.moj.workspace.RemoteWorkspaceClient;
import nl.moj.workspace.WorkspaceRetrier;

/** 
 * Remote workspace : Workspace in another Virtual Machine somewhere on the network. 
 */

public class RemoteWorkspaceFactory implements WorkspaceFactory,LoadBalancer {

	private String host;
	private int port;
	
	/**
	 * constructs a new WorkspaceFactory for remote workspaces which reside on a 
	 * single machine with the specified hostname and port.
	 * @param host the host name of the machine
	 * @param port the port on which the Workspace server runs.
	 */
	public RemoteWorkspaceFactory(String host,int port) {
		this.host=host;
		this.port=port;
	}
	
	public Workspace createWorkspace(String team, Target target, ProcessListener list) throws IOException {
		return new WorkspaceRetrier(new RemoteWorkspaceClient(team,this,target,list));
	}
	
	public Socket getWorkspaceServerConnection(String workspaceName,boolean resume) throws IOException {
		Socket socket=new Socket(host,port);
		socket.setSoTimeout(1000);
		return socket;
	}
	
	public void reportClosing(Socket s) {
		// We do not care. There is only one server.
	}
	
	public void reportFailure(Socket s) {
		// We do not care. There is only one server.
	}
	
}