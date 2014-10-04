package nl.moj.gfx.ops;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import nl.moj.gfx.RoundList;
import nl.moj.model.Team;
import nl.moj.model.Tester;

/**
 * 
 */
public class TypingFx extends AbstractFx implements RoundList.VisualEffect {

	public TypingFx(RoundList rlst) {
		super(rlst,new String[] { "/data/img/go.jpg" });
	}
	
    public boolean qualifies(Team tm) {
        return (tm.isPlaying())&&(!tm.isPerformingOperation());
    }
	
	public void paint(Team tm, Graphics g, int frame,Component owner) {
		//
		super.paint(tm,g,frame,owner);
		//
		int[] testResults=tm.getOldTestResults();
		//
		if ((testResults!=null)) {
			//
			int step=150/testResults.length;
			//
			int til=(frame<testResults.length?frame:testResults.length);
			
			//Below code cleans up the test result array, without it the test results of each team remain visible.
			//if (frame>(testResults.length+2)) {
				//tm.clearOldResults();
			//}
			//
			for (int t=0;t<til;t++) {
				switch (testResults[t]) {
					case Tester.TestResult.FAIL : g.setColor(Color.red);break;
					case Tester.TestResult.PASS : g.setColor(Color.green);break;
					default : g.setColor(Color.yellow);break;
				}
				//
				g.fillRect(4+t*step+1,27,step-1,11);
				//
			}
			//
		} 
	}
}
