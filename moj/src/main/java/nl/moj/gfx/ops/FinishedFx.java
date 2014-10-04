package nl.moj.gfx.ops;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;

import nl.moj.gfx.RoundList;
import nl.moj.model.Team;

/**
 * 
 */
public class FinishedFx extends AbstractFx implements RoundList.VisualEffect {

	public FinishedFx(RoundList rlst) {
		super(rlst,new String[] { "/data/img/duke_winner1.jpg","/data/img/duke_loser.jpg","/data/img/duke_winner2.jpg" });
	}
	
	public boolean qualifies(Team tm) {
		return (tm.isFinished());		
	}
	
	public Image getIcon(Team tm,int frame) {		
		return ((tm.getFinalScore()!=0) ? myIcons[0] : myIcons[1]); 		
	}	
	
	public void paint(Team tm, Graphics g, int frame,Component owner) {
		//
		int visibleScore=frame/2;
		boolean[] sc=tm.getSubmitTestResults();
		//
		if (sc!=null) {
			int step=150/sc.length;
			if (visibleScore>sc.length) visibleScore=sc.length;
			//
			for (int t=0;t<sc.length;t++) {
				g.setColor(Color.darkGray);
				g.drawRect(4+t*step,26,step,12);
			}
			//
			for (int t=0;t<visibleScore;t++) {
				if (sc[t]) {
					g.setColor(Color.green);
				} else {
					g.setColor(Color.red);
				}
				g.fillRect(4+t*step+1,27,step-1,11);
			}
		}
		//
		Image img=this.getIcon(tm,frame);
		if ((sc!=null)&&(visibleScore<sc.length)) img=myIcons[2];
		if (img!=null) {
			g.drawImage(img,owner.getWidth()-33,6,owner);
		}		
		//
		if ((sc==null)||(visibleScore==sc.length)) {
			g.setColor(Color.black);
			g.drawString(tm.getFinalScore()+" pts",162,36);		
		} else {
			g.setColor(Color.gray);
			g.drawString(tm.getTheoreticalScore()+" pts",162,36);
		}
		//		
	}

}
