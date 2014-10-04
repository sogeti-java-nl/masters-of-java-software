package nl.moj.operation;

import java.io.File;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

import nl.ctrlaltdev.io.OutputRedirector;
import nl.moj.model.Assignment;
import nl.moj.model.Operation;
import nl.moj.model.Team;
import nl.moj.model.Tester;
import nl.moj.model.Workspace;
import nl.moj.process.ProcessPool;
import nl.moj.test.TesterImpl;

/**
 * Test compiles and tests the code.
 */
public class Test extends AbstractOperation {

	public static class TestWrapper extends AbstractOperationRunnable implements Team.TestResults {
		private Tester myTst;
		private Tester.TestResult results;
		public TestWrapper(Operation op,Tester tst,Workspace.Internal ws,Context ctx) {
			super(op,ws,ctx);
			myTst=tst;
		}
		public void run() {
			try {
				//
				save();
				//
				// Assume it works :-)
				//
				boolean ok=true;
				//
				// Compile if there is need.
				//
				if (!getWorkspace().isCompiled()) {
					getWorkspace().getTarget().append(null,"Compiling..\n");
					ok=compile();									
				}
				//
				// Perform the test
				//
				if (ok) {
					//
					getWorkspace().getTarget().append(null,"Testing..\n");
					if (getContext().getIndex()<0) {
						results=myTst.performTest(getWorkspace());
					} else {
						results=myTst.performTest(getWorkspace(),getContext().getIndex());
					}
					//
				} else {
					getWorkspace().getTarget().append(null,"Compilation Failed.\n");
				}
				//
				//
				//
			} catch (Throwable ex) {
				Logger.getLogger(getWorkspace().getName()).log(Level.SEVERE,"Test Failed.",ex);
			}
		}

		public Tester.TestResult getTestResults() {
			return results;
		}

		public void complete(Runnable r) {
			OutputRedirector.Target out=getWorkspace().getTarget();
			if (results==null) {
				out.append(null,"Failed Test : Illegal operation, timeout or compilation error.\n");
			} else {
				out.append(null,results.isOk() ? "Test : Pass.\n" : "Test : Failed.\n");
				StringBuffer sb=new StringBuffer();
				int[] b=results.getResults();
				for (int t=0;t<b.length;t++) {
					if (b[t]==0) sb.append(". ");
					if (b[t]==1) sb.append("V ");
					if (b[t]==-1) sb.append("X ");
				}
				sb.append("\n");
				out.append(null,sb.toString());
			}
		}
	}
	
	

	private Tester myTester;
	
	/**
	 * constructs a new Test operation. Supported parameters are :
	 * CLASS            : name of the class to execute the tests.
	 * JAR              : name of the jar file which contains the test classes.
	 * TIMEOUT          : timeout of the test
	 * @param pool the thread-pool to get threads from.
	 * @param cfg the configuration to read parameters from.
	 * @throws Exception if something goes wrong.
	 */
	
	public Test(Assignment a,ProcessPool pool,Operation.Configuration cfg) throws Exception {
		super("Test","Tests your implementation against unit tests.",false,pool);
		//
		String testerClassName=cfg.getParameter(this,"CLASS");
		String testerJarFile=cfg.getParameter(this,"JAR");
		File   base=cfg.getParentDir(this);
		int testerTimeout=Integer.parseInt(cfg.getParameter(this,"TIMEOUT"));
		//
		File testerJar=new File(base,testerJarFile);
		if (!testerJar.exists()) {
			testerJar=new File(testerJarFile);
			if (!testerJar.exists()) {
				throw new IOException("The tester-jar file '"+testerJarFile+"' could not be found.");
			}
		}
		//
		myTester=new TesterImpl(a,testerClassName,testerJar,testerTimeout);
		//
	}
	
	public Test(Assignment a,ProcessPool pool,File sourceJar) throws Exception {
		super("Test","Tests your implementation against unit tests.",false,pool);
		myTester=new TesterImpl(a,a.getTestClass(),sourceJar,a.getTestClassTimeout());
	}
	public Test(Assignment a,ProcessPool pool,File[] classFiles) throws Exception {
		super("Test","Tests your implementation against unit tests.",false,pool);
		myTester=new TesterImpl(a,a.getTestClass(),classFiles,a.getTestClassTimeout());
	}

	
	public boolean isSubmit() {
		return false;
	}
	

	public void perform(Workspace.Internal ws,ProcessPool.ProcessListener lst,Context ctx) {
		//		
		Runnable proc=new TestWrapper(this,myTester,ws,ctx); 
		//
		getPool().execute(proc,lst);
		//
	}
	
	public Tester getTester() {
		return myTester;
	}

}
