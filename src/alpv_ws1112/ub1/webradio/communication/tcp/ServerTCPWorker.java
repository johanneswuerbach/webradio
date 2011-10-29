package alpv_ws1112.ub1.webradio.communication.tcp;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.communication.ByteArray;

public class ServerTCPWorker implements Runnable {

	private boolean _close = false, _play = false;
	private AudioFormat _audioFormat;
	private OutputStream _client;
	private ServerTCP _server;

	public ServerTCPWorker(ServerTCP server, OutputStream client)
			throws IOException {
		_client = client;
		_server = server;
	}

	/**
	 * Set the close flag
	 */
	public void close() {
		_close = true;
	}

	/**
	 * Set the audio format
	 */
	public void setAudioFormat(AudioFormat audioFormat) {
		_audioFormat = audioFormat;
		sendAudioFormat();
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
	 * Send audio format to client
	 */
	private void sendAudioFormat() {

		try {
			AudioFormatTransport aft = new AudioFormatTransport(_audioFormat);
			byte[] format = ByteArray.toBytes(aft);

			// Send size
			byte[] size = new byte[4];
			int length = format.length;

			// int -> byte[]
			for (int i = 0; i < 4; ++i) {
				int shift = i << 3; // i * 8
				size[3 - i] = (byte) ((length & (0xff << shift)) >>> shift);
			}
			_client.write(size);

			// Send format
			_client.write(format);

			System.out.println("AudioFormat transmitted.");
		} catch (IOException e) {
			System.err.println("Can't send audio format.");
			close();
		}
	}

	/**
	 * Runs the server worker
	 */
	public void run() {
		
		System.out.println("Worker started.");
		
		while (!_close) {
			if (_play) {
				try {
					_client.write(_server.getBuffer());
					_server.awaitBarrier();
				} catch (IOException e) {
					System.err.println("Can't send music data.");
					close();
				}
			}
		}
		
		try {
			_client.close();
		} catch (IOException e) {
			System.err.println("Can't send close client connection.");
		}
	}
}
