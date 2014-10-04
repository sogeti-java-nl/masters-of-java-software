package nl.moj.assignment;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.jar.Manifest;

import nl.ctrlaltdev.ioc.ApplicationBuilder;
import nl.moj.model.Assignment;
import nl.moj.model.Operation;
import nl.moj.operation.Compile;
import nl.moj.operation.Save;
import nl.moj.operation.Submit;
import nl.moj.operation.Test;
import nl.moj.process.ProcessPool;
import nl.moj.test.FileArrayClassLoader;

/**
 * MoJEclipsePluginAssignment: Special Assignment loader for running together
 * with the MoJ Assignment Eclipse plugin.
 * 
 * @author E.Hooijmeijer
 */

public class MoJEclipsePluginAssignment extends AbstractAssignment implements Assignment {

	private static class FileResource implements Resource {
		private File f;

		public FileResource(File f) {
			this.f = f;
		}

		public String getName() {
			return f.getName();
		}

		public long getSize() {
			return f.length();
		}

		public InputStream getInputStream() throws IOException {
			return new FileInputStream(f);
		}
	}

	private ProcessPool pool;
	private ApplicationBuilder builder;
	private File[] classFiles;
	private FileResource manifestResource;
	private FileResource[] resources;

	public MoJEclipsePluginAssignment(File f, ProcessPool p, ApplicationBuilder b) throws Exception {
		super();
		this.pool = p;
		this.builder = b;
		//
		determineResources(f);
		//
		parseManifest(getManifest());
		//
	}

	protected Operation[] createOperations() throws Exception {
		theOps = new Operation[4];
		theOps[0] = new Save(pool);
		theOps[1] = new Compile(pool);
		theOps[2] = new Test(this, pool, classFiles);
		theOps[3] = new Submit(this, pool, classFiles);
		return theOps;
	}

	protected ClassLoader createSecurityDelegateClassLoader() {
		return new FileArrayClassLoader(classFiles, this.getClass().getClassLoader(), false);
	}

	protected ApplicationBuilder getApplicationBuilder() {
		return builder;
	}

	public InputStream getAssignmentFileData(String name) throws IOException {
		Resource res = getResource(name);
		if (res == null)
			return null;
		return getInputStream(res);
	}

	protected InputStream getInputStream(Resource res) throws IOException {
		return ((FileResource) res).getInputStream();
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

	//
	//
	//

	protected void determineResources(File root) throws IOException {
		//
		// We assume the root dir is the project root.
		// There are 2 source folders : xxxTest and xxx.
		// There is 1 binary folder : bin
		//
		File[] dirs = root.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return ((!f.getName().startsWith(".")) && (f.isDirectory()));
			}
		});
		String sourceFolderName = null;
		for (int t = 0; t < dirs.length; t++) {
			if (dirs[t].getName().endsWith("Test")) {
				sourceFolderName = dirs[t].getName().substring(0, dirs[t].getName().length() - 4);
			}
		}
		//
		if (sourceFolderName == null)
			throw new IOException("xxxTest folder not found.");
		//
		File sourceFolder = null;
		File binFolder = null;
		for (int t = 0; t < dirs.length; t++) {
			if (dirs[t].getName().equals(sourceFolderName)) {
				sourceFolder = dirs[t];
			}
			if (dirs[t].getName().equals("bin")) {
				binFolder = dirs[t];
			}
		}
		//
		if (sourceFolder == null)
			throw new IOException("Missing source folder.");
		if (binFolder == null)
			throw new IOException("Missing bin folder");
		//
		File[] sourceFiles = sourceFolder.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.isFile();
			}
		});
		//
		File[] binFiles = binFolder.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.isFile();
			}
		});
		//
		List<Resource> resources = new ArrayList<>();
		List<File> classFiles = new ArrayList<>();
		//
		for (int t = 0; t < sourceFiles.length; t++) {
			FileResource fr = new FileResource(sourceFiles[t]);
			resources.add(fr);
			if (fr.getName().endsWith(EXT_MF))
				manifestResource = fr;
		}
		//
		for (int t = 0; t < binFiles.length; t++) {
			FileResource fr = new FileResource(binFiles[t]);
			if (binFiles[t].getName().endsWith(EXT_CLASS)) {
				String javaName = binFiles[t].getName();
				javaName = javaName.substring(0, javaName.length() - EXT_CLASS.length()) + ".java";
				//
				// No class files for those files we have java files for.
				//
				boolean found = false;
				for (int y = 0; y < resources.size(); y++) {
					if (resources.get(y).getName().equals(javaName))
						found = true;
				}
				if (found)
					continue;
				classFiles.add(binFiles[t]);
			} else {
				//
				// No Duplicates
				//
				boolean found = false;
				for (int y = 0; y < resources.size(); y++) {
					if (resources.get(y).getName().equals(fr.getName()))
						found = true;
				}
				//
				if (!found) {
					resources.add(fr);
				}
			}
			//
		}
		//
		this.resources = resources.toArray(new FileResource[resources.size()]);
		this.classFiles = classFiles.toArray(new File[classFiles.size()]);
		//
		if (manifestResource == null)
			throw new IOException("No Manifest found.");
		//
	}

	protected Manifest getManifest() throws IOException {
		if (manifestResource == null)
			throw new IOException("No Manifest found.");
		return new Manifest((manifestResource).getInputStream());
	}

}
