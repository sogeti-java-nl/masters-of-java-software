package nl.moj.test;

import java.util.Random;

import nl.moj.model.Tester;

/**
 * Utility class for implementing Testables. Implements the performTest() method,
 * which fires all the tests in a random sequence. 
 * @deprecated Test Randomizing was moved to tester so performTest() is no longer needed. 
 */
@Deprecated
public abstract class AbstractTestable implements Tester.Testable {
	
	public AbstractTestable() {
		super();
	}
	
    public abstract int getTestCount();    
    public abstract String getTestDescription(int nr);
	public abstract String getTestName(int nr);
    public abstract boolean performTest(int nr) throws Throwable;
    
	@Deprecated
	public boolean[] performTest() throws Throwable {
		Random rnd=new Random();
		boolean[] results=new boolean[getTestCount()];
		boolean[] did=new boolean[getTestCount()];
		for (int t=0;t<getTestCount();t++) {
			int nxt=-1;
			do {
				nxt=rnd.nextInt(getTestCount());				
			} while (did[nxt]);
			//
			results[nxt]=performTest(nxt);
			did[nxt]=true;
			//
		}
		return results;
	}

}
