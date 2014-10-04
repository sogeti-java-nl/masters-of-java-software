package nl.moj.assignment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import nl.ctrlaltdev.ioc.ApplicationBuilder;
import nl.ctrlaltdev.util.Tool;
import nl.moj.model.Assignment;
import nl.moj.model.Operation;
import nl.moj.model.Tester;
import nl.moj.model.Tester.SecurityDelegate;
import nl.moj.security.DefaultSecurityDelegate;
import nl.moj.security.SandboxSecurityManager;

public abstract class AbstractAssignment implements Assignment {

	public interface Resource {
		public String getName();

		public long getSize();
	}

	public static final String EXT_JAVA = ".java";
	public static final String EXT_CLASS = ".class";
	public static final String EXT_TXT = ".txt";
	public static final String EXT_MF = ".mf";
	public static final String EXT_GIF = ".gif";
	public static final String EXT_PNG = ".png";
	public static final String EXT_JPG = ".jpg";
	public static final String EXT_SOLUTION = ".solution";

	protected List<String> javaFiles = new ArrayList<String>();
	protected List<String> textFiles = new ArrayList<String>();
	protected String[] editableFiles;
	protected boolean textFileIsMonospaced;
	protected String name;
	protected String author;
	protected String assignmentIconName, sponsorIconName;
	protected byte[] assignmentIconData, sponsorImageData;
	protected Tester.SecurityDelegate delegate;
	protected Operation[] theOps;
	protected String displayName;
	protected String authorURL;
	protected String catagory;
	protected String assignmentHint;
	protected int quality;
	protected int fun;
	protected int difficulty;
	protected String testClass;
	protected String submitClass;
	protected int testClassTimeout;
	protected int submitClassTimeout;
	protected int duration;

	public AbstractAssignment() {
		super();
	}

	protected abstract Enumeration<Resource> getResources();

	protected abstract Resource getResource(String name);

	protected abstract InputStream getInputStream(Resource res) throws IOException;

	protected abstract ApplicationBuilder getApplicationBuilder();

	protected abstract ClassLoader createSecurityDelegateClassLoader();

	protected abstract Operation[] createOperations() throws Exception;

	protected void parseManifest(Manifest mf) throws Exception {
		Attributes a = mf.getMainAttributes();
		if (a == null)
			throw new IOException("Missing MainAttributes in Manifest File.");
		//
		String val = a.getValue("Editable");
		if (val == null)
			throw new IOException("'Editable' entry in MainAttributes is NULL - missing CRLF at the end ?");
		editableFiles = Tool.cut(val, " ");
		//
		String monospaced = a.getValue("Monospaced");
		textFileIsMonospaced = "TRUE".equalsIgnoreCase(monospaced);
		//
		String securityDelegate = a.getValue("SecurityDelegate");
		if (securityDelegate != null) {
			ClassLoader cl = createSecurityDelegateClassLoader();
			ApplicationBuilder ab = new ApplicationBuilder(getApplicationBuilder());
			ab.register(Manifest.class, mf);
			ab.build(new Class<?>[] { cl.loadClass(securityDelegate) });
			delegate = (Tester.SecurityDelegate) ab.get(Tester.SecurityDelegate.class);
			if (delegate == null)
				throw new NullPointerException("Error getting '" + securityDelegate + "' as Tester.SecurityDelegate");
		} else {
			delegate = new DefaultSecurityDelegate();
		}
		//
		registerAssignmentWithSecurityManager();
		//
		Enumeration<Resource> entries = getResources();
		while (entries.hasMoreElements()) {
			Resource e = entries.nextElement();
			String name = e.getName();
			if (name.endsWith(EXT_JAVA)) {
				javaFiles.add(name);
			} else if (name.endsWith(EXT_TXT)) {
				textFiles.add(name);
			} else if (name.endsWith(EXT_CLASS)) {
				//
			} else if (name.toLowerCase().endsWith(EXT_PNG)) {
				// metainf.mf
			} else if (name.toLowerCase().endsWith(EXT_JPG)) {
				// metainf.mf
			} else if (name.toLowerCase().endsWith(EXT_GIF)) {
				// metainf.mf
			} else if (name.toLowerCase().endsWith(EXT_MF)) {
				// metainf.mf
			} else if (name.endsWith("/")) {
				// directory.
			} else if (name.endsWith(EXT_SOLUTION)) {
				// Ignore.
			} else
				throw new IOException("Unexpected file : " + name);
		}
		//
		for (int t = 0; t < editableFiles.length; t++) {
			if (!javaFiles.contains(editableFiles[t]))
				throw new NullPointerException("Editable File '" + editableFiles[t] + "' is not present as a Java File!");
		}
		//
		testClass = a.getValue("TestClass");
		submitClass = a.getValue("SubmitClass");
		testClassTimeout = Integer.parseInt(a.getValue("TestClassTimeout") == null ? "-1" : a.getValue("TestClassTimeout"));
		submitClassTimeout = Integer.parseInt(a.getValue("SubmitClassTimeout") == null ? "-1" : a.getValue("SubmitClassTimeout"));
		//
		if (testClass == null)
			throw new IOException("Missing 'TestClass' entry.");
		if (submitClass == null)
			throw new IOException("Missing 'SubmitClass' entry.");
		if (testClassTimeout == -1)
			throw new IOException("Missing or invalid 'TestClassTimeout' entry.");
		if (submitClassTimeout == -1)
			throw new IOException("Missing or invalid 'SubmitClassTimeout' entry.");
		//
		theOps = createOperations();
		//
		author = a.getValue("Author");
		//
		// Assignment Sponsor Image
		//
		String tmp = a.getValue("SponsorImage");
		if (tmp != null) {
			Resource ze = getResource(tmp);
			if (ze == null)
				throw new NullPointerException("Resource not found for " + tmp);
			sponsorIconName = tmp;
			sponsorImageData = Tool.readBinary(getInputStream(ze), (int) ze.getSize());
		}
		//
		// Assignment Icon
		//
		tmp = a.getValue("Icon");
		if (tmp != null) {
			Resource ze = getResource(tmp);
			if (ze == null)
				throw new NullPointerException("ZipEntry not found for " + tmp);
			assignmentIconName = tmp;
			assignmentIconData = Tool.readBinary(getInputStream(ze), (int) ze.getSize());
		}
		//
		// Display Name
		//
		displayName = a.getValue("DisplayName");
		//
		// Other Meta Stuff
		//
		authorURL = a.getValue("AuthorURL");
		catagory = a.getValue("Catagory");
		assignmentHint = a.getValue("Hint");
		//
		quality = Integer.parseInt(a.getValue("Quality") == null ? "3" : a.getValue("Quality"));
		fun = Integer.parseInt(a.getValue("Fun") == null ? "3" : a.getValue("Fun"));
		difficulty = Integer.parseInt(a.getValue("Difficulty") == null ? "3" : a.getValue("Difficulty"));
		//
		String dur = a.getValue("Duration");
		if (dur == null)
			dur = "30";
		duration = Integer.parseInt(dur);
	}

	protected void registerAssignmentWithSecurityManager() {
		SandboxSecurityManager ssm = (SandboxSecurityManager) System.getSecurityManager();
		ssm.registerAssignment(this);
	}

	public String getTestClass() {
		return testClass;
	}

	public String getSubmitClass() {
		return submitClass;
	}

	public int getTestClassTimeout() {
		return submitClassTimeout;
	}

	public int getSubmitClassTimeout() {
		return submitClassTimeout;
	}

	public String[] getDescriptionFileNames() {
		return textFiles.toArray(new String[textFiles.size()]);
	}

	public String[] getSourceCodeFileNames() {
		return javaFiles.toArray(new String[javaFiles.size()]);
	}

	public Operation[] getOperations() {
		return (Operation[]) Tool.copy(theOps);
	}

	public SecurityDelegate getSecurityDelegate() {
		return delegate;
	}

	public String[] getEditableFileNames() {
		return (String[]) Tool.copy(editableFiles);
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return (displayName == null ? name : displayName);
	}

	public boolean isDescriptionRenderedInMonospaceFont() {
		return textFileIsMonospaced;
	}

	public String getAuthor() {
		return author;
	}

	public String getIconName() {
		return assignmentIconName;
	}

	public String getSponsorIconName() {
		return sponsorIconName;
	}

	public byte[] getIcon() {
		return assignmentIconData;
	}

	public byte[] getSponsorImage() {
		return sponsorImageData;
	}

	public String getAuthorURL() {
		return authorURL;
	}

	public String getCatagory() {
		return catagory;
	}

	public String getHint() {
		return assignmentHint;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public int getQuality() {
		return quality;
	}

	public int getFun() {
		return fun;
	}

	public int getDuration() {
		return duration;
	}

}
