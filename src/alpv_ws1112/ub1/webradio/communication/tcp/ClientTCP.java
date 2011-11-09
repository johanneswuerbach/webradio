package alpv_ws1112.ub1.webradio.communication.tcp;

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
import alpv_ws1112.ub1.webradio.protobuf.Messages.WebradioMessage;
import alpv_ws1112.ub1.webradio.protobuf.Messages.ChatMessage;
import alpv_ws1112.ub1.webradio.ui.ClientUI;
import alpv_ws1112.ub1.webradio.webradio.Main;

/**
 * A TCP client for the webradio
 */
public class ClientTCP implements Client {

	private Socket _socket;
	private boolean _close = false;
	private InputStream _inputStream;
	private OutputStream _outputStream;
	private AudioPlayer _audioPlayer;

	public ClientTCP(String host, int port) {
		try {
			connect(InetSocketAddress.createUnresolved(host, port));
		} catch (IOException e) {
			System.err.println("Can't connect to host " + host + " on port "
					+ port);
		}
	}

	/**
	 * Start receiving messages
	 */
	public void run() {
		while (!_close) {
			try {
				WebradioMessage message = WebradioMessage
						.parseDelimitedFrom(_inputStream);
				if (message.getIsAudioFormat()) {
					receiveAudioFormat(message);
				} else if (message.getIsDataMessage()) {
					receiveDataMessage(message);
				}
			} catch (Exception e) {
				System.err.println("Can't receive message from server.");
				System.exit(0);
			}
		}
		
		try {
			_socket.close();
		} catch (IOException e) {}
	}

	/**
	 * Receive and handle a data message
	 */
	private void receiveDataMessage(WebradioMessage message) {
		// Receive chat messages
		int numberOfMessages = message.getUsernameCount();
		for (int i = 0; i < numberOfMessages; i++) {
			displayMessage(message.getUsername(i), message.getText(i));
		}

		// Play audio
		if(message.hasData() && _audioPlayer != null) {
			_audioPlayer.start();
			_audioPlayer.writeBytes(message.getData().toByteArray());
		}
	}

	/**
	 * Displays a chat message in the current UI
	 */
	private void displayMessage(String username, String message) {
		clientUI().pushChatMessage(username + ": " + message);
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

	/**
	 * Send chat messages to server
	 */
	public void sendChatMessage(String message) throws IOException {

		displayMessage(clientUI().getUserName(), message);

		System.out.println("Sending chat message to server.");
		ChatMessage.Builder builder = ChatMessage.newBuilder();
		builder.setText(message);
		builder.setUsername(clientUI().getUserName());

		ChatMessage chatMessage = builder.build();
		chatMessage.writeDelimitedTo(_outputStream);

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

	private ClientUI clientUI() {
		return Main.clientUI;
	}

}