package nl.moj.server.socket;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctrlaltdev.net.server.Log;
import nl.ctrlaltdev.net.server.SimpleSocketServer;
import nl.ctrlaltdev.net.server.SocketHandlerFactory;
import nl.moj.model.Scheduler;

public class SocketGameServer implements SocketHandlerFactory {

	private SimpleSocketServer mySSS;
	private Scheduler scheduler;

	public SocketGameServer(Scheduler scheduler, ThreadGroup group) {
		this(scheduler, group, Integer.parseInt(System.getProperty("MOJ.SERVER.PORT", "8080")));
	}

	public SocketGameServer(Scheduler scheduler, ThreadGroup group, int port) {
		try {
			this.scheduler = scheduler;
			mySSS = new SimpleSocketServer(port, new Log.JavaUtilLoggerAdapter("Server"), this);
		} catch (IOException ex) {
			Logger.getLogger("").log(Level.SEVERE, "Failed opening server socket." + ex);
			throw new RuntimeException("Failed starting Server.");
		}

		new Thread(group, mySSS).start();
	}

	public Runnable createHandler(Socket s, Log l) {
		return new SocketMessageHandler(s, l, scheduler);
	}

}
