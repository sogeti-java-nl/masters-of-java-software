package nl.moj.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Wraps a Runtime.exec in a Runnable and reads the standard Output and standard
 * Error streams. DO NOT USE THIS CLASS : It sometimes causes the VM to
 * terminate without warning.
 * 
 * @deprecated No Longer In Use.
 */
@Deprecated
public class ShellProcess implements Runnable {

	private String command;
	private String[] env;
	private File work;

	private BufferedReader stdOut;
	private BufferedReader stdErr;

	private List<String> results;
	private List<String> errors;
	private int exitCode;

	public ShellProcess(String command, String[] env, File workDir) {
		if (command == null)
			throw new NullPointerException("Null command.");
		if (workDir == null)
			throw new NullPointerException("Null WorkDir.");
		if (!workDir.exists())
			throw new RuntimeException("WorkDir does not exist.");
		if (env == null)
			env = new String[0];
		this.command = command;
		this.env = env;
		this.work = workDir;
		results = new ArrayList<>();
		errors = new ArrayList<>();
	}

	public void run() {
		try {
			//
			Process p = Runtime.getRuntime().exec(command, env, work);
			stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
			stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			//
			boolean goOn;
			do {
				goOn = false;
				if (stdOut.ready()) {
					String out = stdOut.readLine();
					if (out != null)
						results.add(out);
					goOn = true;
				}
				if (stdErr.ready()) {
					String err = stdErr.readLine();
					if (err != null)
						errors.add(err);
					goOn = true;
				}
				if (!goOn)
					try {
						Thread.sleep(250);
						exitCode = p.exitValue();
					} catch (IllegalThreadStateException ex) {
						goOn = true;
					} catch (InterruptedException ex) {
						//
					}
			} while (goOn);
			//
		} catch (Exception ex) {
			Logger.getLogger("shell").warning("Shell process failed : " + ex);
		}
	}

	public int getExitCode() {
		return exitCode;
	}

	public String[] getResults() {
		return results.toArray(new String[results.size()]);
	}

	public String[] getErrors() {
		return errors.toArray(new String[errors.size()]);
	}

}
