package nl.moj.test;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.ctrlaltdev.io.OutputRedirector;
import nl.ctrlaltdev.util.Tool;
import nl.moj.client.anim.Anim;
import nl.moj.model.Assignment;
import nl.moj.model.HasWorkspace;
import nl.moj.model.Tester;
import nl.moj.model.Workspace;
import nl.moj.security.SandboxClassLoader;
import nl.moj.security.SandboxSecurityManager;

/**
 * TesterImpl works with 2 separate JarFiles, one containing the acceptance
 * tests and one containing the subject to be tested. The test is invoked in a
 * separate thread and using a seperate class loader.
 */
public class TesterImpl implements Tester {

    private static final Logger log = Logger.getLogger("Tester");
    private static final int[] TESTFAILED = new int[]{TestResult.FAIL};

    /**
     * Container for test results. The score value can be written once.
     */
    public static class TestResultImpl implements Tester.TestResult {

        private int[] score;
        private Anim[] output;

        public TestResultImpl() {
            score = null;
            output = null;
        }

        public TestResultImpl(int[] score, Anim[] anim) {
            this();
            setScore(score, anim);
        }

        void setScore(int[] score) {
            if (this.score == null) {
                this.score = score;
            }
        }

        void setScore(int[] score, Anim[] anim) {
            if (this.score == null) {
                this.score = score;
                if (this.output == null) {
                    this.output = anim;
                }
            }
        }

        public Anim[] getAnimationOutput() {
            if (output == null) {
                return new Anim[0];
            }
            return output;
        }

        public boolean[] getScore() {
            if (score == null) {
                return new boolean[0];
            }
            boolean[] copy = new boolean[score.length];
            for (int t = 0; t < score.length; t++) {
                copy[t] = (score[t] == 1 ? true : false);
            }
            return copy;
        }

        public int[] getResults() {
            if (score == null) {
                return new int[0];
            }
            int[] copy = new int[score.length];
            for (int t = 0; t < score.length; t++) {
                copy[t] = score[t];
            }
            return copy;
        }

        public boolean isUnknown() {
            return (score == null);
        }

        public boolean isFaulty() {
            if (score == null) {
                return false;
            }
            for (int t = 0; t < score.length; t++) {
                if (score[t] == FAIL) {
                    return true;
                }
            }
            return false;
        }

        public boolean isOk() {
            if (score == null) {
                return false;
            }
            for (int t = 0; t < score.length; t++) {
                if (score[t] == FAIL) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * runs the test
     */
    private static class TestRunner implements Runnable {

        private TestResultImpl result = new TestResultImpl();
        private Testable myTestable;
        private int idx;
        private final Workspace.Internal ws;

        TestRunner(Workspace.Internal ws, Class<?> c, int idx) {
            this.ws=ws;
            Class<?> myTesterClass = c;
            this.idx = idx;
            if (myTesterClass == null) {
                throw new NullPointerException("Tester class is null.");
            }
            try {
                myTestable = (Testable) myTesterClass.newInstance();
                if (myTestable instanceof HasWorkspace) {
                    ((HasWorkspace) myTestable).setWorkspace(ws);
                }

            } catch (Exception ex) {
                throw new RuntimeException("Unable to instantiate tester : ", ex);
            }
            //
            if (idx >= 0) {
                if (idx >= myTestable.getTestCount()) {
                    throw new RuntimeException("Incorrect test #" + idx);
                }
            }
            //
        }

        private void redirectOutput(int testNr) {
            OutputRedirector.getSingleton().setContext(String.valueOf(testNr));
        }

        private void cancelRedirection() {
            OutputRedirector.getSingleton().setContext(null);
        }

        private void performAllTests(Testable tst, int[] results, Anim[] output) throws Throwable {
            Random rnd = new Random();
            boolean[] did = new boolean[tst.getTestCount()];
            for (int t = 0; t < did.length; t++) {
                int nxt = -1;
                do {
                    nxt = rnd.nextInt(did.length);
                } while (did[nxt]);
                //
                redirectOutput(nxt);
                performSingleTest(tst, nxt, results, output);
                did[nxt] = true;
                //
            }
        }

        private void performSingleTest(Testable tst, int idx, int[] results, Anim[] output) throws Throwable {
            redirectOutput(idx);
            try {
                if (tst instanceof AnimatedTestable) {
                    results[idx] = (((AnimatedTestable) tst).performTest(idx, output) ? Tester.TestResult.PASS : Tester.TestResult.FAIL);
                } else {
                    results[idx] = (tst.performTest(idx) ? Tester.TestResult.PASS : Tester.TestResult.FAIL);
                }
            } finally {
                cancelRedirection();
            }
        }

        public void run() {
            try {
                Anim[] output = new Anim[myTestable.getTestCount()];
                int[] results = new int[myTestable.getTestCount()];
                //
                if (idx < 0) {
                    performAllTests(myTestable, results, output);
                } else {
                    performSingleTest(myTestable, idx, results, output);
                }
                //
                result.setScore(results, output);
                //
            } catch (Throwable t) {
				//
                // Lock the score.
                //
                result.setScore(TESTFAILED);
                //
                System.out.println("Test Aborted because of : " + t.getClass().getName());
                t.printStackTrace(System.out);
                //
            } finally {

            }
        }

        public TestResult getResult() {
            return result;
        }

    }

    public static final int TESTTIMEOUT = 1000;
    private Assignment myAssignment;
    private ThreadGroup myTesterThreadGroup;
    private File myTesterJarFile;
    private File[] myTesterClassFiles;
    private String myTesterClassName;
    private int myTimeout;
    private String[] myPrefetchNames;
    private String[] myPrefetchDescriptions;

    public TesterImpl(Assignment a, String testerClassName, File jarFile, int timeout) throws IOException, ClassNotFoundException {
        //
        if (a == null) {
            throw new NullPointerException("NULL assingment.");
        }
        if (jarFile == null) {
            throw new NullPointerException("jarFile is null.");
        }
        if (testerClassName == null) {
            throw new NullPointerException("testerClassName is null.");
        }
        if (!jarFile.exists()) {
            throw new IOException("The jarFile " + jarFile.getAbsolutePath() + " does not exist.");
        }
        //
        myAssignment = a;
        myTimeout = timeout;
        myTesterClassName = testerClassName;
        myTesterJarFile = jarFile;
        //
        prepare();
        //
        prefetch();
        //
    }

    public TesterImpl(Assignment a, String testerClassName, File[] classFiles, int timeout) throws IOException, ClassNotFoundException {
        //
        if (a == null) {
            throw new NullPointerException("NULL assingment.");
        }
        if (classFiles == null) {
            throw new NullPointerException("classFiles are null.");
        }
        if (testerClassName == null) {
            throw new NullPointerException("testerClassName is null.");
        }
        //
        myAssignment = a;
        myTimeout = timeout;
        myTesterClassName = testerClassName;
        myTesterJarFile = null;
        myTesterClassFiles = classFiles;
        //
        prepare();
        //
        prefetch();
        //
    }

    private void prepare() {
        //
        if (System.getSecurityManager() instanceof SandboxSecurityManager) {
            myTesterThreadGroup = ((SandboxSecurityManager) System.getSecurityManager()).getEvilThreadGroup(myAssignment);
        } else {
            throw new RuntimeException("SandboxSecurityManager not installed.");
        }
        //
    }

    private ClassLoader createTesterClassLoader(ClassLoader parent) throws IOException {
        if (myTesterJarFile != null) {
            return new JarClassLoader(new JarFile(myTesterJarFile), parent);
        }
        if (myTesterClassFiles != null) {
            return new FileArrayClassLoader(myTesterClassFiles, parent, true);
        }
        throw new RuntimeException("Unable to createTesterClassloader.");
    }

    private void prefetch() {
        try {
            ClassLoader myTesterClassLoader = createTesterClassLoader(this.getClass().getClassLoader());
            //
            Class<?> myTesterClass = myTesterClassLoader.loadClass(myTesterClassName);
            //
            Tester.Testable tst = (Tester.Testable) myTesterClass.newInstance();
            //
            int nr = tst.getTestCount();
            myPrefetchNames = new String[nr];
            myPrefetchDescriptions = new String[nr];
            for (int t = 0; t < nr; t++) {
                myPrefetchNames[t] = tst.getTestName(t);
                // Special error messages for Robert :-)
                if (myPrefetchNames[t] == null) {
                    throw new NullPointerException("Name of TestCase #" + t + " is NULL.");
                }
                myPrefetchDescriptions[t] = tst.getTestDescription(t);
                if (myPrefetchDescriptions[t] == null) {
                    throw new NullPointerException("Description of TestCase #" + t + " is NULL.");
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error prefetching test names and descriptions.", ex);
        }
    }

    public String[] getTestDescriptions() {
        return (String[]) Tool.copy(myPrefetchDescriptions);
    }

    public String[] getTestNames() {
        return (String[]) Tool.copy(myPrefetchNames);
    }

    @SuppressWarnings("deprecation")
    public TestResult performTest(Workspace.Internal workspace, int idx) throws Exception {
        //
        if (workspace == null) {
            throw new NullPointerException("Workspace is NULL.");
        }
        //
        String workspaceName = workspace.getName();
		//
        // Create a stack of 2 class loaders :
        // A sandboxed classloader for the classes that need to be tested.
        // A normal classloader for the code implementing the tests.
        // If MOJ.ECLIPSEPLUGIN property is true reverse classloading order is
        // used for classes in the default package.
        //
        ClassLoader mySubjectClassLoader = new SandboxClassLoader(workspace, myTesterThreadGroup, Boolean.getBoolean("MOJ.ECLIPSEPLUGIN"));
        ClassLoader myTesterClassLoader = createTesterClassLoader(mySubjectClassLoader);
        //
        Class<?> myTesterClass = myTesterClassLoader.loadClass(myTesterClassName);
        //
        TestRunner runner = new TestRunner(workspace, myTesterClass, idx);
        //
        Thread thread = new Thread(myTesterThreadGroup, runner, workspaceName + "-Tester");
		//
        // Redirect output to network.
        //
        OutputRedirector.getSingleton().redirect(thread, workspace.getTarget());
        //
        log.log(Level.INFO, "Starting test : " + workspaceName);
        //
        thread.start();
        int tmo = 0;
        //
        while (thread.isAlive()) {
            try {
                if (tmo > myTimeout) {
                    break;
                }
                tmo++;
                Thread.sleep(TESTTIMEOUT);
            } catch (InterruptedException ex) {
                // Ignore.
            }
        }
        //
        if (thread.isAlive()) {
			//
            // Block changing the score.
            //
            ((TestResultImpl) runner.getResult()).setScore(TESTFAILED);
			//
            // Stop the thread the hard (and deprecated) way.
            //
            log.log(Level.INFO, "Test TimedOut : " + workspaceName);
            thread.stop();

			//
            // Wait a bit for the other thread to clean up.
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
            }
            //
        } else {
            log.log(Level.INFO, "Test Ended : " + workspaceName);
        }
        //
        OutputRedirector.getSingleton().cancel(thread);
        //
        return runner.getResult();
    }

    public TestResult performTest(Workspace.Internal tm) throws Exception {
        return performTest(tm, -1);
    }

}
