package nl.moj.client.anim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.ctrlaltdev.ui.Build;

/**
 * A simple Swing Component that allows the playing of Anims. 
 */
public class AnimPlayer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4229346325532634553L;

	private class Updater extends TimerTask  {
		public void run() {
			if (EventQueue.isDispatchThread()) {
				if (currentAnimation==null) return;
				if ((currentAnimation.next())&&(!playButton.isEnabled())) {
					timer.schedule(new Updater(),100l);
				} else {
					playButton.setEnabled(true);
					stopButton.setEnabled(false);
				}
				display.repaint();
				frameSelector.setValue(currentAnimation.currentFrame());
			} else {
				SwingUtilities.invokeLater(this);	
			}
		}		
	}

	private class DisplayPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4691130844609305161L;

		public void paint(Graphics g) {
			Anim.Frame af=getCurrentFrame();
			if (af!=null) {				
				Graphics2D tmp=(Graphics2D)g.create();
				try {
					tmp.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
					af.draw(tmp,getWidth(),getHeight());
				} finally {
					tmp.dispose();
				}
			} else {
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(0,0,getWidth(),getHeight());
			}
			super.paintBorder(g);
		}
	}

	private JSlider frameSelector;
	private JButton playButton;
	private JButton stopButton;
	private JPanel  display;
	
	private static Timer timer=new Timer();   
	private Anim    currentAnimation;

	public AnimPlayer() {
		super(new BorderLayout());
		frameSelector=new JSlider(JSlider.HORIZONTAL);
		playButton=new JButton(">");
		stopButton=new JButton("[]");
		display=new DisplayPanel();
		this.setPreferredSize(new Dimension(128,160));
		display.setPreferredSize(new Dimension(128,128));
		display.setBorder(BorderFactory.createEtchedBorder());
		//
		this.add(display,BorderLayout.CENTER);
		this.add(new Build.BOXX(new JComponent[] {
			playButton,stopButton,frameSelector
		}),BorderLayout.SOUTH);
		//
		frameSelector.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
            	if (currentAnimation!=null) {
					currentAnimation.setCurrentFrame(((JSlider)e.getSource()).getValue());
            	}
				display.repaint();
            }
		});
		//
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playButton.setEnabled(true);
				stopButton.setEnabled(false);
			}
		});
		//
		playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				currentAnimation.first();
				display.repaint();
				playButton.setEnabled(false);
				stopButton.setEnabled(true);
				timer.schedule(new Updater(),100l);
            }

		});	
		//
		setAnimation(null);	
	}
	
	public void setAnimation(Anim a) {
		currentAnimation=a;
		if (currentAnimation!=null) {
			currentAnimation.first();
			frameSelector.getModel().setMinimum(0);
			frameSelector.getModel().setMaximum(a.getFrameCount()-1);
			frameSelector.getModel().setValue(0);
			frameSelector.setEnabled(true);
			playButton.setEnabled(true);
			stopButton.setEnabled(false);
		} else {
			frameSelector.getModel().setMinimum(0);
			frameSelector.getModel().setMaximum(0);
			frameSelector.getModel().setValue(0);
			frameSelector.setEnabled(false);
			playButton.setEnabled(false);
			stopButton.setEnabled(false);
		}
	}

	private Anim.Frame getCurrentFrame() {
		if (currentAnimation==null) return null;
		if (currentAnimation.getFrameCount()==0) return null;
		return currentAnimation.current();
	}
	
}
