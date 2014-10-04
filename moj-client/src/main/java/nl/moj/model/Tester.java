package nl.moj.model;

import java.security.Permission;

import nl.moj.client.anim.Anim;

/**
 * Tester defines the external functions of the test module in Masters of Java.
 * There are 3 different aspects to it :
 * - The actual invocation of the tests (Tester interface)
 * - The test results (TestResult interface)
 * - The implementation of the test in the assignment (Testable interface)  
 */

public interface Tester {

	public interface TestResult {		
		public static final int PASS=1;
		public static final int FAIL=-1;		
		/** indicates test result = 100 % */
		public boolean isOk();
		/** indicates test result < 100 % */
		public boolean isFaulty();
		/** indicates the tests could not be completed */
		public boolean isUnknown();  
		/** returns the results of the tests. Not tested defaults to false.*/
		public boolean[] getScore();
		/** returns the results of the tests -1 = fail, 0 = not tested, 1 = passed. */
		public int[] getResults();
		/** returns an array of Anim which may contain null values. */
		public Anim[] getAnimationOutput();
	}
	
	/**
	 * Interface to be implemented by the Assignment 
	 * See also nl.moj.test.AbstractTestable for a partial implementation.
	 */	
	public interface Testable {
		/** returns the number of tests in this testable */
		public int getTestCount();
		/** returns the name of the test with index nr*/
		public String getTestName(int nr);
		/** returns the description of the test with index nr*/
		public String getTestDescription(int nr);
		/** 
		 * performs all tests in _random_ order.
		 * No longer used. 
		 */
		//public boolean[] performTest() throws Throwable;
		/** performs the specified test. */
		public boolean performTest(int nr) throws Throwable;
	}
	
	/**
	 * Special Testable which allows the returning of an animation as test results.
	 */
	public interface AnimatedTestable extends Testable {
		public boolean   performTest(int nr,Anim[] a) throws Throwable;
	}
	
	/**
	 * Extension inteface for special security needs of the Assignment.
	 * @see nl.moj.security.DefaultSecurityDelegate for the default implementation. 
	 */	
	public interface SecurityDelegate {
		/**
		 * Checks if the assignment and test code have permission to perform the specified operation.
		 * @param perm the permission to grant or deny.
		 * @param context (optional) context object (may be null).
		 * @throws SecurityException if the assignment code has no permission.
		 */
		public void checkPermission(Permission perm,Object context) throws SecurityException;
		/**
		 * Checks if the specified class may be loaded by the assignment and test code.
		 * @param className the name of the class to load.
		 * @throws SecurityException if the assignment and test code may not load this class.
		 */
		public void checkClassLoading(String className) throws SecurityException;
	}

	/** returns an array with all the testnames */
	public String[] getTestNames();
	/** returns an array with all the test descriptions */
	public String[] getTestDescriptions();
	/** performs a single test on behalf of the specified team. */
	public TestResult performTest(Workspace.Internal tm,int idx) throws Exception;
	/** performs a all tests on behalf of the specified team. */
	public TestResult performTest(Workspace.Internal tm) throws Exception;

}
