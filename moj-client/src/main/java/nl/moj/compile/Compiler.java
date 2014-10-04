package nl.moj.compile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctrlaltdev.io.OutputRedirector;
import nl.moj.model.Workspace;

/**
 * Compiles java source code inprocess by making use of the tools.jar in the JDK
 * distribution. Note, this implementation is specific to J2SE 1.4.2
 */

public class Compiler {
	private static Logger log = Logger.getLogger("Compiler");

	private static final String COMPILERCLASSNAME = "com.sun.tools.javac.Main";
	private static final String COMPILERMETHODNAME = "compile";

	private Method compilerMethod;

	/**
	 * Constructs the compiler instance. Finds the required class and method.
	 * 
	 * @throws ClassNotFoundException
	 *             if the tools.jar compiler class cannot be found.
	 * @throws NoSuchMethodException
	 *             if the compile(String[],PrintWriter) method cannot be found.
	 */
	public Compiler() throws ClassNotFoundException, NoSuchMethodException, IOException {
		Class<?> compilerClass = this.getClass().getClassLoader().loadClass(COMPILERCLASSNAME);
		Class<?>[] methodSignature = { String[].class, PrintWriter.class };
		compilerMethod = compilerClass.getMethod(COMPILERMETHODNAME, methodSignature);
	}

	/**
	 * Compiles (all) the sources for the specified team and redirects output to
	 * that teams target.
	 */
	public boolean compile(Workspace.Internal ws) throws IOException {
		//
		// Set up paths.
		//
		String binRoot = ws.getBinaryRoot().toString().replace('\\', '/');
		String srcRoot = ws.getSourceRoot().toString().replace('\\', '/');
		//
		File[] src = ws.getSourceFiles();
		//
		// Construct the command line parameters
		//
		List<String> largs = new ArrayList<String>();
		largs.add("-d");
		largs.add(binRoot);
		for (int t = 0; t < src.length; t++)
			largs.add(srcRoot + "/" + src[t].getName());
		//
		String[] args = largs.toArray(new String[largs.size()]);
		//
		// Log the command line parameters
		//
		StringBuffer cmd = new StringBuffer();
		cmd.append("javac ");
		for (int t = 0; t < args.length; t++) {
			cmd.append(args[t]);
			cmd.append(" ");
		}
		log.log(Level.FINE, cmd.toString());
		//
		// Start the compiler:
		//
		long cmp = System.currentTimeMillis();
		//
		boolean result = (invokeCompiler(args, new OutputRedirector.RedirectorPrintWriter(ws.getTarget())) == 0);
		//
		long delta = (System.currentTimeMillis() - cmp);
		log.log((delta > 1000 ? Level.WARNING : Level.INFO), "Compile took : " + delta + " ms");
		//
		if (result)
			ws.markCompiled();
		//
		return result;
	}

	/**
	 * actually invokes the compiler. Does forced garbage collection afterwards.
	 */
	protected int invokeCompiler(String[] arguments, PrintWriter out) {
		try {
			Integer i = (Integer) compilerMethod.invoke(null, new Object[] { arguments, out });
			return i.intValue();
		} catch (InvocationTargetException ex) {
			log.log(Level.WARNING, "Compiler-call failed.", ex);
			out.println("Compilation failed.");
			return 1;
		} catch (Exception ex) {
			log.log(Level.WARNING, "Compiler-call failed.", ex);
			out.println("Compilation failed.");
			return 1;
		} finally {
			// Removed for performance reasons.
			// System.gc();
		}
	}
}
