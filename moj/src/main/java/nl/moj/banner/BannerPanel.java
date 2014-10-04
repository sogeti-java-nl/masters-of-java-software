package nl.moj.banner;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import nl.moj.model.Clock;

/**
 * Displays banner images, synchronized with the clock.
 */

public class BannerPanel extends JPanel implements Clock.Notification {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2565268148110373995L;

	/**
	 * allowed extension for image files.
	 */
	private static final String[] validExtensions = new String[] { ".jpg", ".gif", ".png" };

	private File[] srcFiles;
	private Image currentImage;
	private int currentFile;

	/**
	 * creates a new BannerPanel and scans the specified directory for images.
	 */
	public BannerPanel(File imageDir) {
		System.out.println(imageDir);
		srcFiles = imageDir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return false;
				String name = f.getName().toLowerCase();
				System.out.println(name);
				for (int t = 0; t < validExtensions.length; t++) {
					if (name.endsWith(validExtensions[t]))
						return true;
				}
				return false;
			}
		});
		//
		// initialise the first image.
		//
		nextImage();
	}

	/**
	 * paints the banner if there is a currentImage.
	 */
	public void paint(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		if (currentImage != null) {
			g.drawImage(currentImage, this.getWidth() / 2 - currentImage.getWidth(null) / 2, this.getHeight() / 2 - currentImage.getHeight(null) / 2,
					null);
		}
	}

	/**
	 * displays the next image
	 */
	public void nextImage() {
		if ((srcFiles == null) || (srcFiles.length == 0))
			return;
		//
		// Load the Image in a separate thread to avoid the current Thread to be
		// delayed.
		//
		new Thread(new Runnable() {
			public void run() {
				try {
					currentImage = ImageIO.read(srcFiles[currentFile]);
					//
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							BannerPanel.this.repaint();
						}
					});
					//
				} catch (IOException ex) {
					Logger.getLogger("BANNER").log(Level.WARNING, "Failed loading : " + srcFiles[currentFile] + " " + ex);
				}
			}
		}).start();
		//
		//
		currentFile++;
		if (currentFile >= srcFiles.length)
			currentFile = 0;
	}

	//
	// Clock.Notification implementation
	//

	public void clockReset() {
		//
	}

	public void clockStarted() {
		nextImage();
	}

	public void clockStopped() {
		nextImage();
	}

	public void minutePassed(int remaining) {
		nextImage();
	}

	public void clockFinished() {
		//
	}
}
