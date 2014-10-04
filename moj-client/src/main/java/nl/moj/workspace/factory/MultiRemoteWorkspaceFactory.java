package nl.moj.workspace.factory;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import nl.ctrlaltdev.io.OutputRedirector.Target;
import nl.moj.model.State;
import nl.moj.model.Workspace;
import nl.moj.process.ProcessPool.ProcessListener;
import nl.moj.util.InetAddressUtil;
import nl.moj.workspace.LoadBalancer;
import nl.moj.workspace.RemoteWorkspaceClient;
import nl.moj.workspace.WorkspaceRetrier;

/**
 * MultiRemote : Workspace may reside on a number of machines on the network.
 */

public class MultiRemoteWorkspaceFactory implements WorkspaceFactory, LoadBalancer {

	private static final Logger log = Logger.getLogger("MRWorkspaceFactory");

	private String[] hosts;
	private int[] ports;
	private int[] connections;
	private int[] failures;
	private Map<Socket, Integer> sockets;
	private State.Writer stateWriter;
	private State state;

	public MultiRemoteWorkspaceFactory(String[] hosts, int[] ports, State state, State.Writer stateWriter) {
		if (hosts.length != ports.length)
			throw new RuntimeException("# of hosts does not match # of ports.");
		this.hosts = hosts;
		this.ports = ports;
		this.state = state;
		this.stateWriter = stateWriter;
		connections = new int[hosts.length];
		failures = new int[hosts.length];
		sockets = new HashMap<>();
	}

	public Workspace createWorkspace(String team, Target target, ProcessListener list) throws IOException {
		return new WorkspaceRetrier(new RemoteWorkspaceClient(team, this, target, list));
	}

	/**
	 * Contains the actual load balancing algoritm. In this case a simple one :
	 * Connect to the host which has the least (active connections + reported
	 * errors).
	 * 
	 * @param team
	 *            the workspace name
	 * @return the number of the host to connect to.
	 */
	protected synchronized int selectHost(String team) {
		int max = Integer.MAX_VALUE;
		int selected = -1;
		for (int t = 0; t < hosts.length; t++) {
			if (connections[t] + failures[t] < max) {
				selected = t;
				max = connections[t] + failures[t];
			}
		}
		return selected;
	}

	public synchronized Socket getWorkspaceServerConnection(String workspaceName, boolean resume) throws IOException {
		//
		int selected = -1;
		//
		if (resume) {
			String host = state.getLastHost(workspaceName);
			int port = 0;
			if (host != null) {
				port = state.getLastPort(workspaceName);
				for (int t = 0; t < hosts.length; t++) {
					if (hosts[t].equals(host) && (ports[t] == port)) {
						selected = t;
					}
				}
			}
			//
			if (selected == -1)
				log.warning("Unable to find the previous host for '" + workspaceName + "'");
			else
				log.info("Found previous host '" + host + ":" + port + "' for '" + workspaceName + "'");
			//
			//
		}
		if (selected == -1) {
			selected = selectHost(workspaceName);
		}
		//
		if (selected == -1)
			throw new RuntimeException("Unable to select a Workspace Host : None available.");
		//
		// Works only for JDK 1.5 :
		// Socket socket = new Socket(Proxy.NO_PROXY);
		// InetSocketAddress socketAddress = new
		// InetSocketAddress(hosts[selected],ports[selected]);
		// socket.connect(socketAddress, 1000);
		//
		// Works for both JDK 1.4 and JDK 1.5 :
		Socket socket = new Socket(InetAddressUtil.makeInetAddress(hosts[selected]), ports[selected]);
		//
		// Works only for JDK 1.4 (long connect time on 1.5 because of host-name
		// lookup) :
		// Socket socket=new Socket(hosts[selected],ports[selected]);
		//
		socket.setSoTimeout(1000);
		//
		sockets.put(socket, new Integer(selected));
		connections[selected]++;
		stateWriter.workspaceHost(workspaceName, hosts[selected], ports[selected]);
		//
		return socket;
	}

	public synchronized void reportClosing(Socket s) {
		//
		Integer i = sockets.get(s);
		if (i != null) {
			int sel = i.intValue();
			connections[sel]--;
		}
		//
		sockets.remove(s);
	}

	public void reportFailure(Socket s) {
		//
		Integer i = sockets.get(s);
		if (i != null) {
			int sel = i.intValue();
			failures[sel]++;
		}
		//
	}
}