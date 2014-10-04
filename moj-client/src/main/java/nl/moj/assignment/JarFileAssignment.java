package nl.moj.assignment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import nl.ctrlaltdev.ioc.ApplicationBuilder;
import nl.ctrlaltdev.util.Tool;
import nl.moj.model.Assignment;
import nl.moj.model.Operation;
import nl.moj.operation.Compile;
import nl.moj.operation.Save;
import nl.moj.operation.Submit;
import nl.moj.operation.Test;
import nl.moj.process.ProcessPool;
import nl.moj.test.JarClassLoader;

/**
 * JarFileAssignment : holds a complete case in a single Jar File, including the
 * starting source code, description, testsets and security delegate. The
 * following entries are read from the manifest file :
 * --------------------------
 * ----------------------------------------------------------- Editable : lists
 * the (space separated) user editable files in the jar Monospaced : if the
 * description must be rendered in a monospace font. SecurityDelegate : the
 * class name of the security delegate (optional) TestClass : the class name of
 * the test class. SubmitClass : the class name of the submit class (may be
 * identical to the test class) TestClassTimeout : max duration in seconds of
 * the tests. SubmitClassTimeout : max duration in seconds of the submit tests.
 * Author : the (optional) Author of the assignment AuthorURL : the (optional)
 * website of the Author. Icon : the (optional) assignment Icon (max 64x64)
 * SponsorImage : the (optional) sponsored-by image (96x64). DisplayName : the
 * (optional) name for displaying purposes. Catagory : the (optional) Catagory
 * of this assignment (Basic API or Algorithm) Hint : An (optional) hint with
 * this assignment Fun : (optional) How much fun this assignment is. (1..5, 1=
 * No fun at all, 5= Big Fun!) Quality : (optional) How good a quality this
 * assignment is. (1..5) Difficulty : (optional) How difficult this assignment
 * is. (1..5)
 * --------------------------------------------------------------------
 * -----------------
 * 
 * @author E.Hooijmeijer
 */

public class JarFileAssignment extends AbstractAssignment implements Assignment {

	private static class JarResource implements Resource {
		private JarEntry f;

		public JarResource(JarEntry f) {
			this.f = f;
		}

		public String getName() {
			return f.getName();
		}

		public long getSize() {
			return f.getSize();
		}

		public JarEntry getJarEntry() {
			return f;
		}
	}

	private ProcessPool pool;
	private ApplicationBuilder builder;
	private File jarFile;
	private JarFile assignmentJar;
	private Resource[] resources;

	public JarFileAssignment(File f, ProcessPool pool, ApplicationBuilder parent)
			throws Exception {
		super();
		//
		this.pool = pool;
		this.builder = parent;
		//
		//
		jarFile = f;
		//
		name = f.getName();
		if (name.indexOf('.') >= 0)
			name = name.substring(0, name.indexOf('.'));
		//
		assignmentJar = new JarFile(f);
		//
		findResources(assignmentJar);
		//
		parseManifest(getManifest());
		//
	}

	protected void findResources(JarFile j) {
		List<JarResource> l = new ArrayList<JarResource>();
		Enumeration<JarEntry> e = j.entries();
		while (e.hasMoreElements()) {
			JarEntry je = e.nextElement();
			l.add(new JarResource(je));
		}
		resources = l.toArray(new Resource[l.size()]);
	}

	protected Manifest getManifest() throws IOException {
		Manifest mf = assignmentJar.getManifest();
		if (mf == null)
			throw new IOException(jarFile.getName()
					+ " does not contain a Manifest file.");
		return mf;
	}

	public byte[] getJarFileData() throws IOException {
		return Tool.readBinary(new BufferedInputStream(new FileInputStream(
				jarFile)), (int) jarFile.length());
	}

	public InputStream getAssignmentFileData(String name) throws IOException {
		ZipEntry ze = assignmentJar.getEntry(name);
		if (ze == null)
			throw new NullPointerException("ZipEntry not found for " + name);
		return assignmentJar.getInputStream(ze);
	}

	protected InputStream getInputStream(Resource res) throws IOException {
		return assignmentJar.getInputStream(((JarResource) res).getJarEntry());
	}

	protected Resource getResource(String name) {
		for (int t = 0; t < resources.length; t++) {
			if (resources[t].getName().equals(name))
				return resources[t];
		}
		return null;
	}

	protected Enumeration<Resource> getResources() {
		Vector<Resource> v = new Vector<>();
		for (int t = 0; t < resources.length; t++)
			v.add(resources[t]);
		return v.elements();
	}

	protected ClassLoader createSecurityDelegateClassLoader() {
		return new JarClassLoader(assignmentJar, this.getClass()
				.getClassLoader());
	}

	protected Operation[] createOperations() throws Exception {
		theOps = new Operation[4];
		theOps[0] = new Save(pool);
		theOps[1] = new Compile(pool);
		theOps[2] = new Test(this, pool, jarFile);
		theOps[3] = new Submit(this, pool, jarFile);
		return theOps;
	}

	protected ApplicationBuilder getApplicationBuilder() {
		return builder;
	}

}
