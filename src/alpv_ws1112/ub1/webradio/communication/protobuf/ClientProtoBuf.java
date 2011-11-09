package alpv_ws1112.ub1.webradio.communication.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;

import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.audioplayer.AudioPlayer;
import alpv_ws1112.ub1.webradio.communication.ByteArray;
import alpv_ws1112.ub1.webradio.communication.Client;
import alpv_ws1112.ub1.webradio.communication.FixedInteger;
import alpv_ws1112.ub1.webradio.protobuf.Messages.WebradioMessage;
import alpv_ws1112.ub1.webradio.webradio.Main;

/**
 * A TCP client for the webradio
 */
public class ClientProtoBuf implements Client {

	private Socket _socket;
	private boolean _close = false;
	private InputStream _inputStream;
	private OutputStream _outputStream;
	private AudioPlayer _audioPlayer;

	public ClientProtoBuf(String host, int port) {
		try {
			connect(InetSocketAddress.createUnresolved(host, port));
		} catch (IOException e) {
			System.err.println("Can't connect to host " + host + " on port "
					+ port);
		}

	}

	@Override
	public void run() {

		byte[] sizeBuffer = new byte[4];
		int size;

		while (!_close) {
			try {
				_inputStream.read(sizeBuffer);
				size = FixedInteger.toInt(sizeBuffer);
				byte[] bytes = new byte[size];
				_inputStream.read(bytes);
				WebradioMessage message = WebradioMessage.parseFrom(bytes);
				if (message.getIsChatMessage()) {
					receiveChatMessage(message);
				} else if (message.getIsAudioFormat()) {
					receiveAudioFormat(message);
				} else if (message.getIsAudioData()) {
					receiveAudioData(message);
				}
			} catch (IOException e) {
				System.err.println("Can't reveice message from server.");
				close();
			}
		}
		
		Main.clientUIThread.interrupt();
	}

	private void receiveAudioData(WebradioMessage message) {
		_audioPlayer.start();
		_audioPlayer.writeBytes(message.getData().toByteArray());
	}

	private void receiveChatMessage(WebradioMessage message) {
		System.out.println("Message received: " + message.getTextMessage());
		displayMessage(message.getUsername(), message.getTextMessage());
	}

	private void displayMessage(String username, String message) {
		Main.clientUI.pushChatMessage(username + ": " + message);
	}

	/**
	 * Connect to a specific server
	 */
	public void connect(InetSocketAddress serverAddress) throws IOException {
		String host = serverAddress.getHostName();
		int port = serverAddress.getPort();

		System.out.println("Client connecting to \"" + host + ":" + port
				+ "\".");

		_socket = new Socket(host, port);
		_inputStream = _socket.getInputStream();
		_outputStream = _socket.getOutputStream();
	}

	/**
	 * Closes the client
	 */
	public void close() {
		_close = true;
	}

	@Override
	public void sendChatMessage(String message) throws IOException {
		
		displayMessage(Main.clientUI.getUserName(), message);
		
		System.out.println("sending chat message to server.");
		WebradioMessage.Builder builder = WebradioMessage.newBuilder();
		builder.setTextMessage(message);
		builder.setUsername(Main.clientUI.getUserName());
		builder.setIsAudioData(false);
		builder.setIsAudioFormat(false);
		builder.setIsChatMessage(true);
		WebradioMessage textMessage = builder.build();
		// textMessage.writeTo(_outputStrem);
		byte size = (byte) textMessage.getSerializedSize();
		_outputStream.write(size);
		_outputStream.write(textMessage.toByteArray());
	}

	/**
	 * Receive the audio format form the server
	 * 
	 * @throws IOException
	 */
	private void receiveAudioFormat(WebradioMessage audioFormatMessage)
			throws IOException {
		try {
			AudioFormat audioFormat = ((AudioFormatTransport) ByteArray
					.toObject(audioFormatMessage.getData().toByteArray()))
					.getAudioFormat();
			System.out.println("Audio Format: " + audioFormat.toString());
			_audioPlayer = new AudioPlayer(audioFormat);
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

}