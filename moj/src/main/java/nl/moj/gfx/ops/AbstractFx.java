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
public class AbstractFx {

	protected Image[] myIcons;

	public AbstractFx(RoundList rlst,String[] iconNames) {
		myIcons=new Image[iconNames.length];
		for (int t=0;t<iconNames.length;t++) {
			myIcons[t]=rlst.loadImage(iconNames[t]);
		}
	} 

	public Image getIcon(Team tm,int frame) {
		if (myIcons.length==0) return null; 
		return myIcons[0]; 
	}

	public void paint(Team tm, Graphics g, int frame,Component owner) {
		Image img=this.getIcon(tm,frame);
		if (img!=null) {
			g.drawImage(img,owner.getWidth()-33,6,owner);
		}		
		g.setColor(Color.gray);
		g.drawString(tm.getTheoreticalScore()+" pts",162,36);
	}

}
