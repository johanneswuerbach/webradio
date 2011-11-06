package alpv_ws1112.ub1.webradio.communication.protobuf;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;

import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.communication.ByteArray;
import alpv_ws1112.ub1.webradio.protobuf.Messages.WebradioMessage;

import com.google.protobuf.ByteString;

/**
 * Handeling a single client and send audio format and music
 */
public class ServerProtoBufWorker implements Runnable {

	private boolean _close = false, _play = false;
	private AudioFormat _audioFormat;
	private Socket _client;
	private ServerProtoBuf _server;
	private OutputStream _outputStream;

	public ServerProtoBufWorker(ServerProtoBuf server, Socket client)
			throws IOException {
		_client = client;
		_outputStream = _client.getOutputStream();
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
			WebradioMessage.Builder builder = WebradioMessage.newBuilder();
			builder.setData(ByteString.copyFrom(format));
			builder.setIsAudioData(false);
			builder.setIsAudioFormat(true);
			builder.setIsChatMessage(false);
			WebradioMessage message = builder.build();
			byte size = (byte) message.getSerializedSize();
			_outputStream.write(size);
			_outputStream.write(message.toByteArray());
			System.out.println("AudioFormat transmitted.");
		} catch (IOException e) {
			System.err.println("Can't send audio format.");
			close();
		}
	}

	public void sendMessage(WebradioMessage textMessage) {
		System.out.println("sending chat message to client.");
		byte size = (byte) textMessage.getSerializedSize();
		try {
			_outputStream.write(size);
			_outputStream.write(textMessage.toByteArray());
		} catch (IOException e) {
			System.err.println("Can't send message to client.");
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
					WebradioMessage.Builder builder = WebradioMessage
							.newBuilder();
					builder.setData(ByteString.copyFrom(_server.getBuffer()));
					builder.setIsAudioData(true);
					builder.setIsAudioFormat(false);
					builder.setIsChatMessage(false);
					WebradioMessage message = builder.build();
					assert (message.isInitialized());
					message.writeTo(_outputStream);
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
			_outputStream.close();
		} catch (IOException e) {
			System.err.println("Can't send close client connection.");
		}
	}
}
