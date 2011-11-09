package alpv_ws1112.ub1.webradio.communication.protobuf;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import alpv_ws1112.ub1.webradio.audioplayer.AudioPlayer;

/**
 * AudioFile handling
 */
public class ServerProtoBufStreamer implements Runnable {

	private static final int BUFFER_SIZE = 64;

	private AudioInputStream _ais;
	private AudioFormat _audioFormat;
	private byte[] _musicBuffer;
	private ServerProtoBuf _server;
	private String _path;

	public ServerProtoBufStreamer(ServerProtoBuf server) {
		_server = server;
		_musicBuffer = new byte[BUFFER_SIZE];
	}

	/**
	 * Read the next list of bytes from the audio file
	 */
	public void run() {
		if (_ais != null) {
			try {
				if (_ais.read(_musicBuffer) <= 0) {
					_ais = AudioPlayer.getAudioInputStream(_path);
					_ais.read(_musicBuffer);
				}
			} catch (IOException e) {
				System.err.println("IO-Error while reading the file.");
				e.printStackTrace();
				_server.close();
			} catch (UnsupportedAudioFileException e) {
				System.err.println("Unsupported file type.");
				_server.close();
			}
		}
		_server.resetBarrier();
	}

	/**
	 * Returns the current buffer
	 * 
	 * @return
	 */
	public byte[] getBuffer() {
		return _musicBuffer;
	}

	/**
	 * Returns the current audio format
	 * 
	 * @return
	 */
	public AudioFormat getAudioFormat() {
		return _audioFormat;
	}

	/**
	 * Sets the close flag
	 */
	public void close() {
		try {
			_ais.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Play a song
	 * 
	 * @param path
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 * @throws MalformedURLException
	 */
	public void playSong(String path) throws MalformedURLException,
			UnsupportedAudioFileException, IOException {
		_path = path;
		_ais = AudioPlayer.getAudioInputStream(_path);
		_audioFormat = _ais.getFormat();
		_server.newAudioFormat(_audioFormat);
		_ais.read(_musicBuffer);
	}
}