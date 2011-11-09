package alpv_ws1112.ub1.webradio.communication.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import javax.sound.sampled.AudioFormat;

import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.communication.ByteArray;
import alpv_ws1112.ub1.webradio.communication.FixedInteger;
import alpv_ws1112.ub1.webradio.protobuf.Messages.WebradioMessage;

import com.google.protobuf.ByteString;

/**
 * Handeling a single client and send audio format and music
 */
public class ServerProtoBufWorker implements Runnable {

	private boolean _close = false, _play = false, _newAudioFormat = false;
	private AudioFormat _audioFormat;
	private Socket _client;
	private ServerProtoBuf _server;
	private InputStream _inputStream;
	private OutputStream _outputStream;
	private ArrayBlockingQueue<WebradioMessage> _messageQueue;

	public ServerProtoBufWorker(ServerProtoBuf server, Socket client)
			throws IOException {
		_client = client;
		_inputStream = _client.getInputStream();
		_outputStream = _client.getOutputStream();
		_server = server;
		_messageQueue = new ArrayBlockingQueue<WebradioMessage>(20);
	}

	/**
	 * Set the close flag
	 */
	public void close() {
		_close = true;
	}

	/**
	 * Set audio format
	 */
	public void setAudioFormat(AudioFormat audioFormat) {
		// Send new audio format
		// if(_audioFormat == null ||
		// !_audioFormat.toString().equals(audioFormat.toString())) {
		_audioFormat = audioFormat;
		_newAudioFormat = true;
		// }
	}

	/**
	 * Queues a chat message for sending
	 * 
	 * @param message
	 */
	public void queueMessage(WebradioMessage message) {
		_messageQueue.add(message);
	}

	/**
	 * Start playing
	 */
	public void play() {
		_play = true;
	}

	/**
	 * Stop playing
	 */
	public void stop() {
		_play = false;
	}

	/**
	 * Send audio format to client
	 */
	private void sendAudioFormat() {

		System.out.println("Start: sendAudioFormat");

		try {
			AudioFormatTransport aft = new AudioFormatTransport(_audioFormat);
			byte[] format = ByteArray.toBytes(aft);
			WebradioMessage.Builder builder = WebradioMessage.newBuilder();
			builder.setData(ByteString.copyFrom(format));
			builder.setIsAudioData(false);
			builder.setIsAudioFormat(true);
			builder.setIsChatMessage(false);
			WebradioMessage message = builder.build();

			sendMessage(message);

			_newAudioFormat = false;
			System.out.println("AudioFormat transmitted.");
		} catch (IOException e) {
			System.err.println("Can't send audio format.");
			close();
		}

		System.out.println("End: sendAudioFormat");
	}

	/**
	 * Receive a chat message
	 */
	private void receiveWebradioMessage() throws IOException {

		System.out.println("Start: receiveWebradioMessage");

		int size = _inputStream.read();
		byte[] bytes = new byte[size];
		_inputStream.read(bytes);
		WebradioMessage message = WebradioMessage.parseFrom(bytes);
		if (message.getIsChatMessage()) {
			System.out.println("Message from " + message.getUsername()
					+ " received: " + message.getTextMessage());
			_server.sendChatMessage(message, this);
		} else {
			System.err.println("not a text message");
		}

		System.out.println("End: receiveWebradioMessage");
	}

	/**
	 * Send audioData
	 * 
	 * @throws IOException
	 */
	private void sendAudioData() throws IOException {

		WebradioMessage.Builder builder = WebradioMessage.newBuilder();
		builder.setData(ByteString.copyFrom(_server.getBuffer()));
		builder.setIsAudioData(true);
		builder.setIsAudioFormat(false);
		builder.setIsChatMessage(false);
		WebradioMessage message = builder.build();
		assert (message.isInitialized());

		sendMessage(message);
	}

	/**
	 * Send a message to our client
	 * 
	 * @param message
	 * @throws IOException
	 */
	private void sendMessage(WebradioMessage message) throws IOException {
		byte[] size = FixedInteger.toBytes(message.getSerializedSize());
		_outputStream.write(size);
		_outputStream.write(message.toByteArray());
	}

	/**
	 * Send available bytes to client
	 */
	public void run() {
		System.out.println("Worker started.");

		while (!_close) {

			try {
				// Check for new messages
				if (_inputStream.available() > 0) {
					receiveWebradioMessage();
				}

				// Send queued a message
				if (!_messageQueue.isEmpty()) {
					sendMessage(_messageQueue.poll());
				}

				// Check for new audioFormat
				if (_newAudioFormat) {
					sendAudioFormat();
				}

				// Send music
				if (_play) {
					sendAudioData();
				}

				_server.awaitBarrier();

			} catch (IOException e) {
				System.err.println("Client disconnected.");
				_server.removeClient(this);
				_server.awaitBarrier();
				close();
			}
		}
		try {
			_outputStream.close();
		} catch (IOException e) {
			System.err.println("Can't send close client connection.");
		}
	}
}
