package nl.moj.gfx.ops;

import nl.moj.gfx.RoundList;
import nl.moj.model.Operation;
import nl.moj.model.Team;
import nl.moj.operation.Compile;

/**
 * 
 */
public class CompilingFx extends AbstractFx implements RoundList.VisualEffect {
	
	public CompilingFx(RoundList rlst) {
		super(rlst,new String[] { "/data/img/compile.jpg" });
	}
	
	public boolean qualifies(Team tm) {
		boolean pop=tm.isPerformingOperation();
		if (pop) {
			Operation op=tm.getCurrentOperation();
			if (op!=null) {
				return (op.getClass().equals(Compile.class));	
			}
		} 
		return false;
	}	

}
