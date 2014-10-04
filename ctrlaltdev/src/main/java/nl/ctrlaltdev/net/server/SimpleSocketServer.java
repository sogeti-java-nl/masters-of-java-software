package nl.ctrlaltdev.net.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * SimpleSocketServer is a simple socket server, listening at some port and
 * dispatching incoming requests in a separate thread to a Runnable created with
 * a SocketHandler. This class is ideal if you need some kind of server fast.
 * Added option to automatically terminate after 25 seconds of no activity,
 * which is useful for testing.
 * 
 * @author E.Hooijmeijer / (C) 2003-2004 E.Hooijmeijer / Licence : LGPL 2.1
 */
public class SimpleSocketServer implements Runnable, Log {

	private ServerSocket myServerSocket;
	private boolean shouldStop;
	private boolean isRunning;
	private Log myLog;
	private SocketHandlerFactory myFactory;
	private ThreadGroup mySocketThreadGroup;
	private boolean testMode;
	private int noActivitiyCnt;

	/** alternative constructor to bind to a specific InetAddress */
	public SimpleSocketServer(int port, Log l, SocketHandlerFactory hf, InetAddress addr) throws IOException {
		super();
		myServerSocket = new ServerSocket(port, 50, addr);
		myServerSocket.setSoTimeout(5000);
		if (l == null)
			l = this;
		myLog = l;
		myFactory = hf;
		noActivitiyCnt = 0;
	}

	public SimpleSocketServer(int port, Log l, SocketHandlerFactory hf) throws IOException {
		super();
		myServerSocket = new ServerSocket(port);
		myServerSocket.setSoTimeout(5000);
		if (l == null)
			l = this;
		myLog = l;
		myFactory = hf;
		noActivitiyCnt = 0;
	}

	public SimpleSocketServer(int port, Log l, SocketHandlerFactory hf, boolean testMode) throws IOException {
		this(port, l, hf);
		this.testMode = testMode;
	}

	public void run() {
		myLog.info("Running on " + myServerSocket.getLocalSocketAddress());
		shouldStop = false;
		isRunning = true;
		mySocketThreadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), "ServerThreadGroup");
		try {
			while (!shouldStop) {
				try {
					Socket s = myServerSocket.accept();
					noActivitiyCnt = 0;
					myLog.info("Accepted connection from " + s.getInetAddress() + ":" + s.getPort());
					Runnable r = myFactory.createHandler(s, myLog);
					new Thread(mySocketThreadGroup, r).start();
				} catch (SocketTimeoutException ex) {
					noActivitiyCnt++;
					if ((testMode) && (noActivitiyCnt > 10)) {
						myLog.info("Terminated.");
						System.exit(0);
					}
				} catch (IOException e) {
					myLog.error("Failed to accept a socket : " + e);
				}
			}
		} finally {
			isRunning = false;
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void stop() {
		shouldStop = true;
	}

	//
	// (Simple) Logging interface
	//

	public void debug(Object o) {
		System.out.println(o);
	}

	public void error(Object o) {
		System.out.println(o);
	}

	public void info(Object o) {
		System.out.println(o);
	}

	public void warn(Object o) {
		System.out.println(o);
	}

}
