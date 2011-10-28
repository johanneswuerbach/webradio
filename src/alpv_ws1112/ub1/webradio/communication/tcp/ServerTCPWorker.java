package alpv_ws1112.ub1.webradio.communication.tcp;

import java.io.IOException;
import java.io.OutputStream;
import javax.sound.sampled.AudioFormat;
import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.communication.ByteArray;

public class ServerTCPWorker implements Runnable {

	private boolean _closed;
	private AudioFormat _audioFormat;
	private OutputStream _client;
	private boolean _play = false;

	public ServerTCPWorker(OutputStream client) throws IOException {
		_client = client;
	}

	/**
	 * Set the close flag
	 */
	public void close() {
		_closed = true;
	}

	/**
	 * Set the audio format
	 */
	public void setAudioFormat(AudioFormat audioFormat) {
		_audioFormat = audioFormat;
		this.sendAudioFormat();
	}

	/**
	 * Start playing
	 */
	public void play() {
		if (_audioFormat == null) {
			System.err.println("Can't play without an audio format.");
		} else {
			_play = true;
		}
	}

	/**
	 * Send music to his client
	 */
	public void sendMusic() {

		// try {
		// while (!_closed) {
		// _client.write(ServerTCP.musicBuffer);
		// }
		//
		// } catch (IOException e) {
		// System.err.println("Can't open audio file.");
		// }

	}

	/**
	 * Send audio format to client
	 */
	public void sendAudioFormat() {

		try {
			AudioFormatTransport aft = new AudioFormatTransport(_audioFormat);
			byte[] bytes = ByteArray.toBytes(aft);

			// Send size
			byte[] data = new byte[4];
			int length = bytes.length;

			// int -> byte[]
			for (int i = 0; i < 4; ++i) {
				int shift = i << 3; // i * 8
				data[3 - i] = (byte) ((length & (0xff << shift)) >>> shift);
			}

			_client.write(data);

			// Send format
			_client.write(bytes);

			System.out.println("AudioFormat transmitted.");
		} catch (IOException e) {
			System.out.println("Can't send audio format.");
			try {
				_client.close();
			} catch (IOException e1) {

			}

		}
	}

	public void run() {

		while (true) {
			if (_play) {
				this.sendAudioFormat();
				// this.sendMusic();
			}
		}

	}

	public void sendBuffer(byte[] musicBuffer) {
		try {
			while (!_closed) {
				_client.write(musicBuffer);
			}

		} catch (IOException e) {
			System.err.println("Can't open audio file.");
		}
	}
}
