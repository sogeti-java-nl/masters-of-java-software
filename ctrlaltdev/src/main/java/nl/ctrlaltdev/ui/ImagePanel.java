package nl.ctrlaltdev.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

/**
 * ImagePanel displays an image.
 * @author E.Hooijmeijer / (C) 2003-2004 E.Hooijmeijer / Licence : LGPL 2.1   
 */
public class ImagePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5227564535962373166L;
	private Image img;
	public ImagePanel(Image img,int dx,int dy,Color background) {
		this.img=img;
		this.setPreferredSize(new Dimension(dx,dy));
		this.setBackground(background);
	}
	public void paint(Graphics g) {		
		g.setColor(getBackground());
		g.fillRect(0,0,getWidth(),getHeight());
		if (img != null) {
			g.drawImage(img,this.getWidth()/2-img.getWidth(null)/2,0,null);
		}
	}
	public void setImage(Image img) {
		this.img=img;
	}
}
