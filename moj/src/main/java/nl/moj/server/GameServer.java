package nl.moj.server;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctrlaltdev.net.server.Log;
import nl.ctrlaltdev.net.server.SimpleSocketServer;
import nl.ctrlaltdev.net.server.SocketHandlerFactory;
import nl.moj.model.Round;

/**
 * @deprecated
 */
@Deprecated
public final class GameServer implements SocketHandlerFactory {

	private SimpleSocketServer mySSS;
	private Round round;

	public GameServer(Round rnd, ThreadGroup group) {
		try {
			this.round = rnd;
			mySSS = new SimpleSocketServer(8080, new Log.JavaUtilLoggerAdapter("Server"), this);
		} catch (IOException ex) {
			Logger.getLogger("").log(Level.SEVERE, "Failed opening server socket." + ex);
			throw new RuntimeException("Failed starting Server.");
		}
		new Thread(group, mySSS).start();
	}

	public Runnable createHandler(Socket s, Log l) {
		return new MessageHandler(s, l, round);
	}

	public static void main(String[] args) {
		new GameServer(null, Thread.currentThread().getThreadGroup());
	}

}