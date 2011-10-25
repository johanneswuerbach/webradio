package alpv_ws1112.ub1.webradio.audioplayer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Simple wrapper for the package {@link javax.sound.sampled} to illustrate its
 * usage.
 * 
 * @author juauer
 */
public class AudioPlayer implements Runnable {
	private SourceDataLine	line;

	/**
	 * Constructs a {@link javax.sound.sampled.SourceDataLine} to play
	 * sound-files in the given {@link javax.sound.sampled.AudioFormat}.
	 * 
	 * @param audioFormat Instance of {@link javax.sound.sampled.AudioFormat}.
	 *            Every sound-file played by this player has to be in this
	 *            format.
	 */
	public AudioPlayer(AudioFormat audioFormat) {
		try {
			line = AudioSystem.getSourceDataLine(audioFormat);
		}
		catch(LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the {@link javax.sound.sampled.AudioInputStream} of a sound-file
	 * specified by its relative patch in the file-system.
	 * 
	 * @param path Relative path to a sound-file.
	 * @return {@link javax.sound.sampled.AudioInputStream} of the given file.
	 * @throws MalformedURLException
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 */
	public static AudioInputStream getAudioInputStream(String path) throws MalformedURLException, UnsupportedAudioFileException, IOException {
		return AudioSystem.getAudioInputStream(new URL("file:" + path));
	}

	@Override
	public void run() {
	}

	/**
	 * Get the AudioFormat of the underlying
	 * {@link javax.sound.sampled.SourceDataLine}.
	 * 
	 * @return The AudioFormat of the underlying
	 *         {@link javax.sound.sampled.SourceDataLine}.
	 */
	public AudioFormat getAudioFormat() {
		return line.getFormat();
	}

	/**
	 * Get the {@link javax.sound.sampled.SourceDataLine} managed by this
	 * player, to perform other tasks than playing it or determining the
	 * AudioFormat.
	 * 
	 * @return {@link javax.sound.sampled.SourceDataLine} of this player.
	 */
	public SourceDataLine getSourceDataLine() {
		return line;
	}

	/**
	 * Non-blocking call to start playing the underlying
	 * {@link javax.sound.sampled.SourceDataLine}. Available bytes will be
	 * played continuously until the line gets stopped, closed or the end of the
	 * stream has been reached.
	 */
	public void start() {
		try {
			line.open();
			line.start();
		}
		catch(LineUnavailableException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Write bytes to the underlying {@link javax.sound.sampled.SourceDataLine}.
	 * 
	 * @param data A array of bytes to write.
	 */
	public void writeBytes(byte[] data) {
		line.write(data, 0, data.length);
	}
}
