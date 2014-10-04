package nl.moj.workspace;

import java.io.IOException;
import java.net.Socket;

/**
 * LoadBalancer allows a choice of which WorkspaceServer MoJ will connect to.  
 */

public interface LoadBalancer {

	public Socket getWorkspaceServerConnection(String workspaceName,boolean resume) throws IOException;
	
	public void   reportFailure(Socket s);
	
	public void   reportClosing(Socket s);
	
}
