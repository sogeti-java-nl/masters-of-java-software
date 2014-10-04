package nl.ctrlaltdev.util;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Utility class to quickly set up some form of basic logging.
 * @author E.Hooijmeijer
 */

public class SimpleLogFormatter extends Formatter {
	
	private long start=System.currentTimeMillis(); 
	
	public String format(LogRecord record) {
		StringBuffer sb=new StringBuffer();
		sb.append(record.getMillis()-start);
		while (sb.length()<16) sb.append(" ");
		sb.append(" ");
		sb.append(record.getLoggerName());				
		while (sb.length()<32) sb.append(" ");
		sb.append(" ");
		sb.append(record.getLevel().toString());
		while (sb.length()<40) sb.append(" ");
		sb.append(" ");
		sb.append(record.getMessage());
		sb.append(" ");
		//
		Throwable tr=record.getThrown();
		if (tr!=null) sb.append(tr.getMessage());
		//
		sb.append("\r\n");
		//
		return sb.toString();
	}
	
	/** removes any (default) handlers from the root logger */
	public static void clearLogConfig() {
		Logger root=Logger.getLogger("");
		Handler[] h=root.getHandlers();
		for (int t=0;t<h.length;t++) root.removeHandler(h[t]);		
	}
	
	/** adds plain console logging */
	public static void addConsoleLogging() {
		Handler ch = new ConsoleHandler();
		ch.setFormatter(new SimpleLogFormatter());
		Logger root=Logger.getLogger("");
		root.addHandler(ch);
	}

	public static void addFileLogging(String logFile) throws IOException {
		Handler fh = new FileHandler(logFile,true);
		fh.setFormatter(new SimpleLogFormatter());
		Logger root=Logger.getLogger("");
		root.addHandler(fh);		
	}
	
	public static void verbose() {
		Logger.getLogger("").setLevel(Level.ALL);	
	}
    public static void verbose(String pck) {
        Logger.getLogger(pck).setLevel(Level.ALL);   
    }

	public static void info() {
		Logger.getLogger("").setLevel(Level.INFO);	
	}

	public static void info(String pck) {
        Logger.getLogger(pck).setLevel(Level.INFO);  
    }

}
