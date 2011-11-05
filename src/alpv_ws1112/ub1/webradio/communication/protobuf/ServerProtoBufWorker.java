package alpv_ws1112.ub1.webradio.communication.protobuf;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;

import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.communication.ByteArray;
import alpv_ws1112.ub1.webradio.protobuf.Messages.AudioFormatData;

import com.google.protobuf.ByteString;

/**
 * Handeling a single client and send audio format and music
 */
public class ServerProtoBufWorker implements Runnable {

	private boolean _close = false, _play = false;
	private AudioFormat _audioFormat;
	private OutputStream _client;
	private ServerProtoBuf _server;

	public ServerProtoBufWorker(ServerProtoBuf server, OutputStream client)
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
	 * Set the audio format and send it to client
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
			AudioFormatData.Builder builder = AudioFormatData.newBuilder();
			builder.setId(132);
			builder.setData(ByteString.copyFrom(format));
			AudioFormatData message = builder.build();
			assert (message.isInitialized());
			message.writeTo(_client);
			System.out.println("AudioFormat transmitted.");
		} catch (IOException e) {
			System.err.println("Can't send audio format.");
			close();
		}
	}

	/**
	 * Send available bytes to client
	 */
	public void run() {

		System.out.println("Worker started.");

		while (!_close) {
			if (_play) {
				try {
					_client.write(_server.getBuffer());
					_server.awaitBarrier();
				} catch (IOException e) {
					System.err.println("Client disconnected.");
					_server.removeClient(this);
					_server.awaitBarrier();
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
