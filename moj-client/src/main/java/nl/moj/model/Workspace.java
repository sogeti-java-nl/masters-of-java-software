package nl.moj.model;

import java.io.File;
import java.io.IOException;

import nl.ctrlaltdev.io.OutputRedirector;

/**
 * Workspace defines the working directory of the Team 
 */

public interface Workspace {

	public interface Internal extends Workspace {
		/** returns the root directory for source files. */
		public File getSourceRoot();
		/** returns the available source files. */
		public File[] getSourceFiles();
		/** erases all files in the binary directory. */
		public void clearBinary() throws IOException;
		/** returns the root directory for class files. */
		public File getBinaryRoot();
		/** marks the sources in sync with the binary files - should only be set after succesful compilation. */
		public void    markCompiled();
		/** returns true if the source files are in sync with binary files */
		public boolean isCompiled();
		/** 
		 * Saves the file if there are modifications with respect to the current version.
		 * @throws IOException if the saving failed. 
		 * @return true if the file was actually saved, false the the contents are identical. 
		 */ 
		public boolean save(String file,String contents) throws IOException;
		/** for storing the results of the opeation. */
		public OutputRedirector.Target getTarget();
	}
	
	/** returns the name of the workspace. */
	public String getName();
	
	/** sets up the workspace for the specified assignment */
	public void loadAssignment(Assignment a,boolean resume) throws IOException;

	/** returns the supported operations of this workspace */
	public Operation[] getAllOperations(); 
	/** returns the Operation specified by the name or null if it is unknown */	
	public Operation getOperationByName(String name);
	/** performs the operation. */
	public void perform(Operation op,Operation.Context ctx);
	
	
	/** returns the names of the files for the editor */ 
	public String[] getEditorFiles();
	/** returns the contents for the specified file */
	public String   getContents(String file) throws IOException;
	/** returns true if the specified file is a java file */
	public boolean  isJava(String file);
	/** returns true if the specified file is readonly */
	public boolean  isReadOnly(String file);
	/** returns true if the specified file should be renderered in a monospaced font. */
	public boolean  isMonospaced(String file);
	/** refreshes workspace state */
	public void update() throws IOException;
	/** cleans up the workspace - all data is lost. */
	public void dispose();
	/** suspends the workspace - data is retained. */
	public void suspend();
	
}
