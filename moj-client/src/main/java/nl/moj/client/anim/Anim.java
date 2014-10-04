package nl.moj.client.anim;

import java.awt.Graphics;

/**
 * The anim interface is used by the anim player to draw animations on the screen.
 */
public interface Anim extends Persistable {

	public interface Frame {
		public void draw(Graphics g,int viewWidth,int viewHeight);
	}

	public Frame current();
	public int getFrameCount();
	public int currentFrame();
	public void setCurrentFrame(int f);
	public void first();
	public boolean next();
	public void prev();
	public void last();

}
