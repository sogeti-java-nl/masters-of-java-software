package nl.moj.clock;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import nl.moj.model.Assignment;
import nl.moj.model.Clock;

/**
 * Improved clock renderer with support for a variable number of minutes (max
 * 60)
 * 
 * @author E.Hooijmeijer
 */

public class ClockPanel extends JPanel {

	/**
	 * <code>serialVersionUID</code> indicates/is used for.
	 */
	private static final long serialVersionUID = 2790826072723979150L;

	public static final Logger log = Logger.getLogger("Clock-Panel");

	/*
	 * Old array with hardcoded colors public static final Color[] timeColor =
	 * new Color[] { new Color(0x80FF80), new Color(0x80FF80), new
	 * Color(0x80FF80), new Color(0x80FF80), new Color(0x80FF80), new
	 * Color(0x80FF80), new Color(0x90FF80), new Color(0xA0FF80), new
	 * Color(0xB0FF80), new Color(0xC0FF80), new Color(0xD0FF80), new
	 * Color(0xE0FF80), new Color(0xF0FF80), new Color(0xFFFF80), new
	 * Color(0xFFF080), new Color(0xFFE080), new Color(0xFFC080), new
	 * Color(0xFFB080), new Color(0xFFA080), new Color(0xFF9080), new
	 * Color(0xFF8080), new Color(0xFF7070), new Color(0xFF6060), new
	 * Color(0xFF5050), new Color(0xFF4040), new Color(0xFF3030), new
	 * Color(0xFF2020), new Color(0xFF1010), new Color(0xFF0000), new
	 * Color(0xFF0000) };
	 */

	public static Color[] timeColor;

	private Font textFont;
	private Font smallFont;
	private Clock clock;
	private boolean assignment;
	private String assignmentName;
	private String assignmentAuthor;
	private Image assignmentIcon;
	private Image assignmentSponsor;

	public ClockPanel(Clock c, Assignment a) {
		timeColor = new Color[60];
		for (int i = 0; i < 15; i++) {
			// 0x00FF00 naar 0x80FF80
			int colorcode = 0x00FF00;
			colorcode += ((i * 127) / 15) * 0x010001;
			timeColor[i] = new Color(colorcode);
		}
		for (int i = 0; i < 15; i++) {
			// 0x80FF80 naar 0xFFFF80
			int colorcode = 0x80FF80;
			colorcode += ((i * 127) / 15) * 0x010000;
			timeColor[i + 15] = new Color(colorcode);
		}
		for (int i = 0; i < 15; i++) {
			// 0xFFFF80 naar 0xFF8080
			int colorcode = 0xFFFF80;
			colorcode -= ((i * 127) / 15) * 0x000100;
			timeColor[i + 30] = new Color(colorcode);
		}
		for (int i = 0; i < 15; i++) {
			// 0xFF8080 naar 0xFF0000
			int colorcode = 0xFF8080;
			colorcode -= ((i * 127) / 15) * 0x000101;
			timeColor[i + 45] = new Color(colorcode);
		}

		textFont = new Font("Verdana", Font.BOLD, 22);
		smallFont = new Font("Verdana", Font.PLAIN, 12);
		clock = c;
		//
		if (a != null) {
			assignment = true;
			assignmentName = a.getDisplayName();
			assignmentAuthor = "by " + a.getAuthor();
			//
			if (a.getIcon() != null)
				try {
					assignmentIcon = ImageIO.read(new ByteArrayInputStream(a.getIcon()));
				} catch (IOException ex) {
					log.log(Level.WARNING, "Unable to read Assignment Icon", ex);
					assignmentIcon = null;
				}
			//
			if (a.getSponsorImage() != null)
				try {
					assignmentSponsor = ImageIO.read(new ByteArrayInputStream(a.getSponsorImage()));
				} catch (IOException ex) {
					log.log(Level.WARNING, "Unable to read Assignment Sponsor", ex);
					assignmentSponsor = null;
				}
			//
		}
		//
	}

	public boolean isFocusable() {
		return true;
	}

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int w = getWidth();
		int h = getHeight();
		//
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, w, h);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//
		//
		if (w > h)
			w = h;
		if (h > w)
			h = w;

		// Progress of the clock out of 60 to the end of the exercise
		// This is (minutes passed / total minutes) * 60, or in this case
		// (seconds passed / total minutes)
		int progress = (clock.getSecondsPassed() / clock.getDurationInMinutes());

		int rsecs = clock.getSecondsPassed() % 60;
		//
		//
		//
		g2d.translate(w / 2, h / 2);
		//
		Graphics2D local = (Graphics2D) g2d.create();
		//
		try {
			local.rotate(Math.PI);
			//
			double step = (2 * Math.PI) / 60;
			for (int t = 0; t < 60; t++) {
				//
				local.setColor(Color.black);
				if (t % 5 == 0) {
					local.fillRect(-2, h / 2 - 32, 4, 32);
				} else {
					local.fillRect(-2, h / 2 - 16, 4, 16);
				}
				//
				if (progress > t) {
					int colorProgress = (t * timeColor.length) / 60;
					if (colorProgress == timeColor.length)
						colorProgress--;

					local.setColor(timeColor[colorProgress]);
					local.fillArc(-h / 4, -h / 4, h / 2, h / 2, -97, 7);
				}
				//
				if (rsecs == t) {
					local.setColor(Color.red);
					local.fillRect(-2, -2, 4, h / 2 - 24);
				}
				//
				// local.setColor(new Color(t*0x10));
				// local.drawString(String.valueOf(t),0,196);
				local.rotate(step);
				//
			}
			//
			local.setColor(Color.red);
			local.fillArc(-8, -8, 16, 16, 0, 360);
			//
		} finally {
			local.dispose();
		}
		//
		g2d.translate(-w / 2, -h / 2);
		//
		if (!clock.isRunning()) {
			//
			// Only Draw Assignment Info when there is room.
			//
			if ((assignment) && (w > 512)) {
				int width = 320;
				if (assignmentName != null)
					width = (150 + assignmentName.length() * 16 > 320 ? 150 + assignmentName.length() * 16 : 320);
				int left = w / 2 - width / 2;
				int top = 4 * h / 7;
				g.setColor(Color.white);
				g.fillRect(left, top, width, 64);
				g.setColor(Color.lightGray);
				if (assignmentIcon != null) {
					g2d.drawImage(assignmentIcon, left + 1, top, null);
					g.drawRect(left, top, 65, 64);
				}
				if (assignmentSponsor != null) {
					g2d.drawImage(assignmentSponsor, left + width - 96, top + 1, null);
					g.drawRect(left + width - 97, top, 97, 64);
				}
				if (assignmentName != null) {
					g2d.setColor(Color.black);
					g2d.setFont(textFont);
					g2d.drawString(assignmentName, left + 72, top + 22);
				}
				if (assignmentAuthor != null) {
					g2d.setColor(Color.gray);
					g2d.setFont(smallFont);
					g2d.drawString(assignmentAuthor, left + 73, top + 36);
				}
				g.setColor(Color.lightGray);
				g.drawRect(left, top, width, 64);
			}
			//
			if (!clock.isFinished()) {
				g2d.setColor(Color.gray);
				String s = "Paused.";
				g2d.setFont(textFont);
				int tw = g2d.getFontMetrics().stringWidth(s);
				g2d.drawString(s, w / 2 - tw / 2, (h * 3) / 4);
				if (clock.isStartPosition()) {
					g2d.setFont(smallFont);
					s = "Press any key to start.";
					tw = g2d.getFontMetrics().stringWidth(s);
					g2d.drawString(s, w / 2 - tw / 2, (h * 4) / 5);
				}
			} else {
				g2d.setColor(Color.gray);
				String s = "The End !";
				g2d.setFont(textFont);
				int tw = g2d.getFontMetrics().stringWidth(s);
				g2d.drawString(s, w / 2 - tw / 2, (h * 4) / 5);
			}
		}

	}

	public static void main(String[] args) {
		//
		final Clock clock = new SimpleClock(60);
		final JFrame f = new JFrame("Masters Of Java");
		f.setSize(512, 512);
		final ClockPanel cp = new ClockPanel(clock, null);
		f.getContentPane().add(cp);
		//
		Timer tm = new Timer();
		tm.schedule(new TimerTask() {
			public void run() {
				clock.run();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						cp.repaint();
					}
				});
			}
		}, 250l, 250l);
		//
		clock.start();
		//
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		cp.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (!clock.isRunning())
					clock.start();
				else
					clock.stop();
			}

			public void keyReleased(KeyEvent e) {

			};

			public void keyTyped(KeyEvent e) {
			}
		});
		//
		f.setVisible(true);
	}

}
