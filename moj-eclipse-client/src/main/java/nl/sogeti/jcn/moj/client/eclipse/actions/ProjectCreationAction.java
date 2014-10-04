package nl.sogeti.jcn.moj.client.eclipse.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import nl.sogeti.jcn.moj.client.eclipse.Activator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry; 
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class ProjectCreationAction implements IWorkbenchWindowActionDelegate {
	//private IWorkbenchWindow window;
	
	/**
	 * The constructor.
	 */
	public ProjectCreationAction() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		try {
			IProgressMonitor progressMonitor = new NullProgressMonitor();
	
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject("MyProject");
			project.create(progressMonitor);
			project.open(progressMonitor);
	
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = JavaCore.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, progressMonitor);
	
			IJavaProject javaProject = JavaCore.create(project);
	
			IFolder sourceFolder = project.getFolder("src");
			sourceFolder.create(false, true, null);

			Set<IClasspathEntry> entries = new HashSet<IClasspathEntry>();
			entries.addAll(Arrays.asList(javaProject.getRawClasspath()));
			IVMInstall vmInstall= JavaRuntime.getDefaultVMInstall();
			LibraryLocation[] locations= JavaRuntime.getLibraryLocations(vmInstall);
			for (LibraryLocation element : locations) {
				entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
			}
			
			IPackageFragmentRoot fragmentRoot = javaProject.getPackageFragmentRoot(sourceFolder);
			entries.add(JavaCore.newSourceEntry(fragmentRoot.getPath()));
			entries.remove(JavaCore.newSourceEntry(javaProject.getPath()));
			javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), progressMonitor);

			IPackageFragment pack = javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment("", false, null);

			StringBuffer buffer = new StringBuffer();
			buffer.append("public class TestImpl implements Test {\n");
			buffer.append("    public boolean doTest(int i) {\n");
			buffer.append("        //To implement\n");
			buffer.append("        return false;\n");
			buffer.append("    }\n");
			buffer.append("}\n");

			pack.createCompilationUnit("TestImpl.java", buffer.toString(), false, null);

			buffer = new StringBuffer();
			buffer.append("public interface Test {\n");
			buffer.append("    public boolean doTest(int i);\n");
			buffer.append("}\n");

			pack.createCompilationUnit("Test.java", buffer.toString(), false, null);
			
		}
		catch(CoreException e) {
			Activator.getDefault().getLog().log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.OK, "CoreException while creating project", e));
		}
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		//this.window = window;
	}
}