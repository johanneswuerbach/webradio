package alpv_ws1112.ub1.webradio.communication;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * A server that handles incoming and outgoing communication. Different
 * implementations may provide support for different communication-protocols.
 * 
 * @author juauer
 */
public interface Server extends Runnable {
	/**
	 * Close this server and free any resources associated with it.
	 */
	public void close();

	/**
	 * Change the currently played song in case the stream has already started.
	 * Start streaming, otherwise.
	 * 
	 * @param path Relative path to a sound-file in the file-system.
	 * @throws MalformedURLException
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 */
	public void playSong(String path) throws MalformedURLException, UnsupportedAudioFileException, IOException;
}
