package nl.ctrlaltdev.net.server;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple interface for writing stuff to the log.
 * @author E.Hooijmeijer / (C) 2003-2004 E.Hooijmeijer / Licence : LGPL 2.1   
 */

public interface Log {
	
	/** Adapter class for java.util.logging.* */
	public static class JavaUtilLoggerAdapter implements Log {
		private String myName;
		public JavaUtilLoggerAdapter(String namespace) {
			myName=namespace;
		}
		public void debug(Object o) {
			Logger.getLogger(myName).log(Level.FINEST,o.toString());
		}
		public void error(Object o) {
			Logger.getLogger(myName).log(Level.SEVERE,o.toString());
		}
		public void info(Object o) {
			Logger.getLogger(myName).log(Level.INFO,o.toString());
		}
		public void warn(Object o) {
			Logger.getLogger(myName).log(Level.WARNING,o.toString());
		}
	}
	
	
	/** writes a message to the log at error level. */
	public void error(Object o);
	/** writes a message to the log at debug level. */
	public void debug(Object o);
	/** writes a message to the log at warning level. */
	public void warn(Object o);
	/** writes a message to the log at info level. */
	public void info(Object o);
}
