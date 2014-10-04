package nl.moj.gfx.ops;

import nl.moj.gfx.RoundList;
import nl.moj.model.Team;

/**
 * 
 */
public class WaitingFx extends AbstractFx implements RoundList.VisualEffect {

	public WaitingFx(RoundList rlst) {
		super(rlst,new String[] { "/data/img/waiting.jpg" });
	}
    public boolean qualifies(Team tm) {
        return tm.isWaiting();
    }

	
}
