package nl.moj.model;

import java.io.IOException;
import java.io.InputStream;

/**
 * Assignment Descriptor
 */
public interface Assignment {

	/** returns the name of this assignment. */
	public String getName();
	
	/** returns the name of this assignment for displaying purposes */
	public String getDisplayName();
	
	/** returns an array of file names that contain descriptions. */
	public String[] getDescriptionFileNames();
	/** returns an array of file names that contain source files */
	public String[] getSourceCodeFileNames();
	/** returns an array of file names that may be changed by the team */
	public String[] getEditableFileNames();
	/** returns true if the description must be renderered in a monospace font */
	public boolean  isDescriptionRenderedInMonospaceFont();
	/** 
	 * returns the data of the specified file.
	 * @throws IOException if there is an error reading the data.
	 */	
	public InputStream getAssignmentFileData(String name) throws IOException;
	/**
	 * returns the allowed operations for this assignment.
	 */
	public Operation[] getOperations();
	
	/**
	 * returns the security delegate for this assignment.
	 * @return the security delegate for this assignment.
	 */
	public Tester.SecurityDelegate getSecurityDelegate();

	/**
	 * returns the (optional) name of the author 
	 * @return the name of the author of this assignment or null. 
	 */
	public String  getAuthor();
	/**
	 * An optional image for this assignment of max 64x64 pixels.  
	 * @return the icon data for this assignment or null. 
	 */
	public byte[]   getIcon();
	/**
	 * An assignment can have one (optional) sponsor image attached to it. This 
	 * image has a predefined size of 96x64 pixels and is displayed at the start
	 * and end of this assignment below the clock center and in the client.
	 * @return the sponsor image data for this assignment or null. 
	 */
	public byte[]   getSponsorImage();
	
	/** returns the classname a class implementing Tester.Testable used to submit the assignment. */
	public String getSubmitClass();
	/** returns the classname a class implementing Tester.Testable used to test the assignment. */
	public String getTestClass();
	/** returns the timeout of the submit class in seconds. */
	public int getSubmitClassTimeout();
	/** returns the timeout of the test class in seconds. */
	public int getTestClassTimeout();
	/** returns the duration in minutes */
	public int getDuration();
}
