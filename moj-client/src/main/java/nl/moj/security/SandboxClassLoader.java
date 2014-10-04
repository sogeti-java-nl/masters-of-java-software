package nl.moj.security;

import nl.moj.model.Tester;
import nl.moj.model.Workspace;
import nl.moj.test.WorkspaceClassloader;

/**
 * Blocks access to certain classes and packages.
 */

public class SandboxClassLoader extends WorkspaceClassloader {
	
	private Tester.SecurityDelegate myDelegate;
	
	public SandboxClassLoader(Workspace.Internal j,ThreadGroup evil,boolean reverse) {
		super(j,reverse);
		myDelegate=((SandboxSecurityManager)System.getSecurityManager()).getSecurityDelegate(evil);
	}

	public SandboxClassLoader(Workspace.Internal j, ClassLoader parent,ThreadGroup evil,boolean reverse) {
		super(j, parent,reverse);
		myDelegate=((SandboxSecurityManager)System.getSecurityManager()).getSecurityDelegate(evil);
	}
	
	/** override to disallow access to certain classes. */	
	public void checkPermissionToLoad(String name) {
		myDelegate.checkClassLoading(name);
	}	

}
