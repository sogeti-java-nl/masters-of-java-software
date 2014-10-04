package nl.ctrlaltdev.net.server;

import java.net.Socket;


/**
 * Interface describing the creation process of an object handling a socket connection.
 * @author E.Hooijmeijer / (C) 2003-2004 E.Hooijmeijer / Licence : LGPL 2.1   
 */

public interface SocketHandlerFactory {
	
	/** Sample implementation of a SocketHandlerFactory for HTTP requests */
	public static class HTTP implements SocketHandlerFactory {
		public Runnable createHandler(Socket s,Log l) {
			return new HTTPSocketHandler(s,l);
		}
	}

	/**
	 * creates a new Handler for the the specified socket with the specified log. 
	 * @param s the socket for this handler.
	 * @param l the log for this handler.
	 * @return Runnable the runnable able to handle the request.
	 */
	public Runnable createHandler(Socket s,Log l);

}
