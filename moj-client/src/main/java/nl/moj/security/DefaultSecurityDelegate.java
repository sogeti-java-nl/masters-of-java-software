package nl.moj.security;

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.util.PropertyPermission;
import java.util.logging.Level;

import nl.moj.model.Tester;

/**
 * Default Security Delegate : The default implementation for the security of assignments.
 */
public class DefaultSecurityDelegate extends AbstractSecurityDelegate implements Tester.SecurityDelegate {
	
	
	public DefaultSecurityDelegate() {
		super();
	}
	
	/** creates the list of illegal classes - override to change. */
	protected void setIllegalClasses() {
		addIllegalClass("java.lang.reflect.*");
		addIllegalClass("java.sql.*");
		addIllegalClass("java.awt.*");
		addIllegalClass("java.rmi.*");
		addIllegalClass("java.net.*");
		addIllegalClass("java.nio.*");
		addIllegalClass("javax.*");
		addIllegalClass("com.*");
		addIllegalClass("org.*");
	}
	
	protected void setAllowedClasses() {
		addAllowedClass("java.awt.Color");
		addAllowedClass("java.awt.Point");
	}

	/**
	 * Checks if the assignment and test code have permission to perform the specified operation.
	 * @param perm the permission to grant or deny.
	 * @param context (optional) context object (may be null).
	 * @throws SecurityException if the assignment code has no permission.
	 */
	public void checkPermission(Permission perm, Object context) throws SecurityException {
		//
		// Allow some things (required for loading clases from workspace)
		//
		if (perm instanceof PropertyPermission) {
			PropertyPermission pp=(PropertyPermission)perm;
			if (pp.getActions().indexOf("write")<0) {
				return;
			}
		} else if (perm instanceof FilePermission) {
			FilePermission fp=(FilePermission)perm;
			if (fp.getActions().equals("read")) {
				if (fp.getName().indexOf("workspace")>=0) {
					if (fp.getName().indexOf("bin")>=0) return;
				} /*else if (fp.getName().endsWith("awt.dll")) {
					// For Windows
					return;
				} else if (fp.getName().endsWith("libawt.so")) {
					// Linux
					return;
				}*/
				// The above is not needed when you preload some AWT class before the assignment starts.

				//TODO Dirty quick-fix for classloading of client library class files, solve in a better way maybe? 
				if (perm.getName().endsWith(".class") && (perm.getName().indexOf("nl\\moj")<0 || perm.getName().indexOf("nl/moj")<0)) return;
			}
		} else if (perm instanceof RuntimePermission) {
			RuntimePermission rp=(RuntimePermission)perm;
			//
			// Needed for Date and Calendar
			//
			if ("accessClassInPackage.sun.text.resources".equals(rp.getName())) return;
			//
			// Needed for Point and Color
			//
			if ("accessClassInPackage.sun.awt.resources".equals(rp.getName())) return;
			//
			// Needed for Color in 1.5.0_05 ??
			//
			if ("loadLibrary.awt".equals(rp.getName())) return;
			//
			// Needed to stop a Thread in 1.6.0_10 ?!
			//
			if ("stopThread".equals(rp.getName())) return;
		}
		else if (perm instanceof ReflectPermission) {
			//Certain language constructs such as Enum.valueOf() require Reflection internally
			return;
		}
		//
		// Disallow the rest
		// 
		log.log(Level.SEVERE,Thread.currentThread().getName()+" : Permission Denied : "+perm);
		throw new SecurityException("Permission Denied : "+perm);
	}
}
