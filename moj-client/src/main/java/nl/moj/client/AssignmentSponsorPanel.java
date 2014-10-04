package nl.moj.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import nl.moj.client.io.Message;

public class AssignmentSponsorPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5608610364199304979L;
	private String name;
	private String author;
	private Image icon;
	private Image sponsor;
	
	private Font textFont;
	private Font smallFont;
	
	public AssignmentSponsorPanel() {
		//
		textFont=new Font("Verdana",Font.BOLD,22);
		smallFont=new Font("Verdana",Font.PLAIN,12);		
		//
		this.setPreferredSize(new Dimension(321,65));
		//
	}
	
	public void setAssignmentData(Message.Assignment a) {
		//
		name=a.getName();
		//
		this.setPreferredSize(new Dimension(150+name.length()*16>321?150+name.length()*16:321,65));
		//
		author="by "+a.getAuthor();
		//
		if (a.getIcon()!=null) try {
			icon=ImageIO.read(new ByteArrayInputStream(a.getIcon()));
		} catch (IOException ex) {
			icon=null;
		}
		//
		if (a.getSponsorImage()!=null) try {
			sponsor=ImageIO.read(new ByteArrayInputStream(a.getSponsorImage()));
		} catch (IOException ex) {			
			sponsor=null;
		}		
		//
	}
	
	public void paint(Graphics g) {
		//
		g.setColor(getBackground());
		g.fillRect(0,0,getWidth(),getHeight());
		//
		super.paintBorder(g);
		//
		int left=0;
		int width=getWidth();
		int top=getHeight()/2-32;
		//
		g.setColor(Color.white);
		g.fillRect(0,top,width,64);
		g.setColor(Color.lightGray);
		if (icon!=null) {
			g.drawImage(icon,left+1,top,null);				
			g.drawRect(left,top,65,64);
		}
		if (sponsor!=null) {
			g.drawImage(sponsor,width-97,top,null);
			g.drawRect(width-98,top,97,64);
		}
		//
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		if (name!=null) {
			g.setColor(Color.black);
			g.setFont(textFont);
			g.drawString(name,left+72,top+22);
		}
		if (author!=null) {
			g.setColor(Color.gray);
			g.setFont(smallFont);
			g.drawString(author,left+73,top+36);				
		}
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
		//
		g.setColor(Color.lightGray);
		g.drawRect(left,top,width,64);		
		//
	}
	
}
