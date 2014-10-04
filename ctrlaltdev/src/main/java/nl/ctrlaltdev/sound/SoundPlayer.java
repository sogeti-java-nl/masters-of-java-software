package nl.ctrlaltdev.sound;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.Mixer;

/**
 * @author E.Hooijmeijer / (C) 2003-2005 E.Hooijmeijer / Licence : LGPL 2.1
 *         28-04-2005 : added optional delay to compensate for bug 4434125
 */

public class SoundPlayer {

	public class SoundPlayerException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1796151240761345182L;

		public SoundPlayerException(String s) {
			super(s);
		}
	}

	private Mixer myMixer;
	private Timer soundStopper;
	private long closeAfterStopDelay;

	public SoundPlayer() throws SoundPlayerException {
		Mixer.Info[] mi = AudioSystem.getMixerInfo();
		if (mi.length == 0)
			throw new SoundPlayerException("No Audio Hardware available.");
		myMixer = AudioSystem.getMixer(mi[0]);
	}

	public SoundPlayer(long closeAfterStopdelay) throws SoundPlayerException {
		this();
		soundStopper = new Timer(true);
		this.closeAfterStopDelay = closeAfterStopdelay;
	}

	public void play(InputStream input) throws SoundPlayerException {
		if (input == null)
			throw new NullPointerException("The file is NULL.");
		try {
			AudioFormat format = AudioSystem.getAudioFileFormat(input).getFormat();
			//
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			//
			final Clip line = (Clip) myMixer.getLine(info);
			line.open(AudioSystem.getAudioInputStream(input));
			//
			line.addLineListener(new LineListener() {
				public void update(LineEvent le) {
					//
					// See BUG 4434125 for the reason of this rather interesting
					// delay.
					//
					if (le.getType().equals(LineEvent.Type.STOP)) {
						if (soundStopper != null) {
							soundStopper.schedule(new TimerTask() {
								public void run() {
									line.close();
								}
							}, closeAfterStopDelay);
						} else {
							line.close();
						}
					}
				}
			});
			//
			line.start();
			//
		} catch (Exception e) {
			throw new SoundPlayerException("Unable to play sample : " + e);
		}
	}

	// public static void main(String[] args) throws Throwable {
	// new SoundPlayer(200).play(new
	// File("D:\\music\\wav\\ss2\\shodan\\shodan asterisk.wav"));
	// }

}
