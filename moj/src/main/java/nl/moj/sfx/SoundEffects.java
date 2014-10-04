package nl.moj.sfx;

import java.io.BufferedInputStream;
import java.io.InputStream;

import nl.ctrlaltdev.sound.SoundPlayer;
import nl.moj.model.Clock;

public class SoundEffects implements Clock.Notification {

	private SoundPlayer myPlayer;
	private String sfxPath = "/data/sfx/";

	public SoundEffects() {
		try {
			myPlayer = new SoundPlayer(200);
		} catch (SoundPlayer.SoundPlayerException ex) {
			myPlayer = null;
		}
	}

	public void clockStarted() {
		playAudio("gong.wav");
	}

	public void clockStopped() {
		playAudio("gong.wav");
	}

	public void minutePassed(int remaining) {
		if ((remaining <= 5) && (remaining != 0)) {
			playAudio("alarm.wav");
		} else {
			// myPlayer.play(new File("./data/sfx/tick.wav"));
		}
	}

	public void submitFailed() {
		playAudio("buzzer.wav");
	}

	public void submitSuccess() {
		playAudio("dowah.wav");
	}

	private void playAudio(String file) {
		if (myPlayer != null) {
			try {
				InputStream audioSrc = this.getClass().getResourceAsStream(sfxPath + file);
				InputStream bufferedAudio = new BufferedInputStream(audioSrc);
				myPlayer.play(bufferedAudio);
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}
	}

	public void clockReset() {

	}

	public void clockFinished() {

	}

}
