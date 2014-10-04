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
 * Submit compiles tests and evaluates the program. There is no way back, so it better
 * be good :-)
 */

public class Submit extends AbstractOperation {

	public static class SubmitWrapper extends AbstractOperationRunnable implements Team.TestResults {
		private Tester tester;
		private Tester.TestResult results;
		boolean compilationError;
		
		public SubmitWrapper(Operation parent,Workspace.Internal ws,Tester tst,Context ctx)  {
			super(parent,ws,ctx);
			tester=tst;
			results=null;
		}
		
		public void run() {
			//
			int retries=0;
			//
			try {
				//
				while (results==null) {
					//
					boolean ok=true;
					//
					save();
					// 
					// Compile if neccecary.
					//
					if (!getWorkspace().isCompiled()) {
					    Logger.getLogger(getWorkspace().getName()).info("Compiling for Submit.");
						getWorkspace().getTarget().append(null,"Compiling..\n");
						ok=compile();								
					}
					//
					// Perform the test.
					//
					if (ok) {
						//
                        Logger.getLogger(getWorkspace().getName()).info("Testing for Submit.");
						compilationError=false;
						getWorkspace().getTarget().append(null,"Testing..\n");
						results=tester.performTest(getWorkspace());
                        Logger.getLogger(getWorkspace().getName()).info("Testing completed: "+(results!=null?(results.isOk()?"Ok":"Failed"):"Null"));						
						//
					} else {
						//
						// Allow retry (why ??) 
						//
						compilationError=true;
						if (retries++<2) {
	                        Logger.getLogger(getWorkspace().getName()).warning("Compilation for Submit failed. Retry.");
							getWorkspace().getTarget().append(null,"Compilation Failed. Trying again.. \n");
						} else {
							getWorkspace().getTarget().append(null,"Compilation Failed.\n");
							throw new RuntimeException("Compilation Failed.");
						}
						//						
					}
				}
			} catch (Throwable ex) {
				Logger.getLogger(getWorkspace().getName()).log(Level.SEVERE,"Test Failed.",ex);
			}			
        }   
        public Tester.TestResult getTestResults() {
			return results;
        }
		public void complete(Runnable r) {
			//
			OutputRedirector.Target out=getWorkspace().getTarget();
			if (results==null) {
				if (compilationError) {
					out.append(null,"Submit Rejected because of compilation error.");
				} else {
					out.append(null,"Submit Rejected : Illegal operation or timeout.");
				}
			} else {
				out.append(null,results.isOk() ? "Submit : Accepted.\n" : "Submit : Rejected.\n");
				StringBuffer sb=new StringBuffer();
				boolean[] b=results.getScore();
				for (int t=0;t<b.length;t++) {
					sb.append(b[t] ? "V " : "X ");    						
				}
				sb.append("\n");
				out.append(null,sb.toString());
			}			
		}
		
	}

	private Tester myTester;

	/**
	 * constructs a new Submit operation. Supported parameters are :
	 * CLASS            : name of the class to execute the submission tests.
	 * JAR              : name of the jar file which contains the test classes.
	 * TIMEOUT          : timeout of the submission test
	 * @param pool the thread-pool to get threads from.
	 * @param cfg the configuration to read parameters from.
	 * @throws Exception if something goes wrong.
     */
	public Submit(Assignment a,ProcessPool pool,Operation.Configuration cfg) throws Exception {
		super("Submit","Ends the game and evaluates your solution.",true,pool);
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
				throw new IOException("The submit-tester-jar file '"+testerJarFile+"' could not be found.");
			}
		}		
		//		
		myTester=new TesterImpl(a,testerClassName,testerJar,testerTimeout);
		//
	}
	
	public Submit(Assignment a,ProcessPool pool,File sourceJar) throws Exception {
		super("Submit","Ends the game and evaluates your solution.",true,pool);
		myTester=new TesterImpl(a,a.getSubmitClass(),sourceJar,a.getSubmitClassTimeout());
	}	
	public Submit(Assignment a,ProcessPool pool,File[] classFiles) throws Exception {
		super("Submit","Ends the game and evaluates your solution.",true,pool);
		myTester=new TesterImpl(a,a.getSubmitClass(),classFiles,a.getSubmitClassTimeout());
	}	
	
	public boolean isSubmit() {
		return true;
	}
	
	
	public void perform(Workspace.Internal ws,ProcessPool.ProcessListener lst,Context ctx) {
		//
		Runnable proc=new Submit.SubmitWrapper(this,ws,myTester,ctx); 
		//
		getPool().execute(proc,lst);
		//			
	}	

}
