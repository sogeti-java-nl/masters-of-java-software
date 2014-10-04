package nl.moj.security;

import java.security.Permission;

import nl.moj.model.Tester;

/**
 * Dummy Security Delegate allowing everything.
 * In case of emergency.
 * @author E.Hooijmeijer
 */

public class NoSecurityDelegate implements Tester.SecurityDelegate {
	
	public void checkClassLoading(String className) throws SecurityException {
	}
	public void checkPermission(Permission perm, Object context) throws SecurityException {
	}

}
