package nl.moj.security;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractSecurityDelegate {

	protected static final Logger log = Logger.getLogger("SecurityDelegate");

	private List<String> myIllegalClasses = new ArrayList<String>();
	private List<String> myAllowedClasses = new ArrayList<String>();

	public AbstractSecurityDelegate() {
		setIllegalClasses();
		setAllowedClasses();
	}

	/** creates the list of illegal classes - override to change. */
	protected abstract void setIllegalClasses();

	/** creates the list of allowed classes - override to change. */
	protected abstract void setAllowedClasses();

	protected void addAllowedClass(String className) {
		myAllowedClasses.add(className);
	}

	/**
	 * Adds an illegal class to the illegal class list.
	 * 
	 * @param className
	 *            the classname to declare illegal. The string may contain a
	 *            wildcard (*) at the end.
	 */
	protected void addIllegalClass(String className) {
		if (className == null)
			throw new NullPointerException("Cannot add a NULL illegal class.");
		if (className.indexOf("*") > 0) {
			if (!className.endsWith("*"))
				throw new RuntimeException(
						"Wildcards are only allowed as last character.");
		}
		myIllegalClasses.add(className);
	}

	/**
	 * Checks if the specified class may be loaded by the assignment and test
	 * code.
	 * 
	 * @param className
	 *            the name of the class to load.
	 * @throws SecurityException
	 *             if the assignment and test code may not load this class.
	 */
	public void checkClassLoading(String className) throws SecurityException {
		for (int t = 0; t < myAllowedClasses.size(); t++) {
			String tmp = myAllowedClasses.get(t);
			if (tmp.equals(className))
				return;
		}
		for (int t = 0; t < myIllegalClasses.size(); t++) {
			String tmp = myIllegalClasses.get(t);
			if (tmp.endsWith("*")) {
				tmp = tmp.substring(0, tmp.length() - 1);
				if (className.startsWith(tmp)) {
					log.log(Level.SEVERE, Thread.currentThread().getName()
							+ " : Disallowed : " + className);
					throw new SecurityException("Loading of Class " + className
							+ " is not allowed.");
				}
			} else {
				if (className.equals(tmp)) {
					log.log(Level.SEVERE, Thread.currentThread().getName()
							+ " : Disallowed : " + className);
					throw new SecurityException("Loading of Class " + className
							+ " is not allowed.");
				}
			}
		}
	}

}
