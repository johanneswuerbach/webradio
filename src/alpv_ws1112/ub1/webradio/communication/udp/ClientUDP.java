package alpv_ws1112.ub1.webradio.communication.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import javax.sound.sampled.AudioFormat;

import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.audioplayer.AudioPlayer;
import alpv_ws1112.ub1.webradio.communication.ByteArray;
import alpv_ws1112.ub1.webradio.communication.Client;
import alpv_ws1112.ub1.webradio.protobuf.Messages.ClientMessage;
import alpv_ws1112.ub1.webradio.protobuf.Messages.ServerMessage;
import alpv_ws1112.ub1.webradio.ui.ClientUI;
import alpv_ws1112.ub1.webradio.webradio.Main;

public class ClientUDP implements Client {

	private boolean _close = false;
	private DatagramSocket _socket;
	private InetAddress _host;
	private int _port;
	private AudioPlayer _audioPlayer;

	public ClientUDP(String host, int port) {
		try {
			connect(InetSocketAddress.createUnresolved(host, port));
			_socket = new DatagramSocket();
		} catch (IOException e) {
			System.err.println("Can't connect to host " + host + " on port "
					+ port);
			System.exit(1);
		}
	}

	public void run() {
		
		while (!_close) {
			try {
				
				byte[] bytes = new byte[2000];
				
				DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
				
				_socket.receive(packet);
				
				// Receive audioFormat
				// If not, reconnect

				// Receive audio and chat data
				
				ServerMessage message = ServerMessage.parseFrom(bytes);
				if (message.getIsAudioFormat()) {
					receiveAudioFormat(message);
				} else if (_audioPlayer == null) {
					disconnect();
					connect();
				} else if(message.getIsDataMessage()) {
					receiveDataMessage(message);
				}
			} catch (Exception e) {
				System.err.println("Can't receive message from server.");
				System.exit(0);
			}
		}
		
		disconnect();
		_socket.close();

	}
	
	/**
	 * Receive the audio format form the server
	 * 
	 * @throws IOException
	 */
	private void receiveAudioFormat(ServerMessage audioFormatMessage)
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
	
	
	
	
	public void connect(InetSocketAddress serverAddress) throws IOException {

		_host = InetAddress.getByName(serverAddress.getHostName());
		_port = serverAddress.getPort();
		
		connect();
	}
	
	private void connect() throws IOException {
		
		_socket.connect(_host, _port);

		System.out
				.println("Client sending to \"" + _host + ":" + _port + "\".");
		
		// Send connect message
		ClientMessage.Builder builder = ClientMessage.newBuilder();
		builder.setConnect(true);
		
		sendPacket(builder);
	}
	
	private void disconnect() {
		// Send disconnect message
		ClientMessage.Builder builder = ClientMessage.newBuilder();
		builder.setDisconnect(true);
		
		try {
			sendPacket(builder);
		} catch (IOException e) {
			System.err.println("Can't disconnect.");
		}
	}

	public void close() {
		_close = true;
	}

	public void sendChatMessage(String message) throws IOException {
		// Display in GUI
		displayMessage(clientUI().getUserName(), message);

		// Send message
		ClientMessage.Builder builder = ClientMessage.newBuilder();
		builder.setText("Test");
		builder.setUsername(clientUI().getUserName());
		
		sendPacket(builder);
	}

	private void sendPacket(ClientMessage.Builder builder) throws IOException {

		ClientMessage clientMessage = builder.build();
		byte[] bytes = clientMessage.toByteArray();

		DatagramPacket packet = new DatagramPacket(bytes, bytes.length, _host,
				_port);
		_socket.send(packet);

		System.out.println("ClientMessage send. (Size: " + bytes.length + ")");
	}

	/**
	 * Displays a chat message in the current UI
	 */
	private void displayMessage(String username, String message) {
		clientUI().pushChatMessage(username + ": " + message);
	}
	
	/**
	 * Receive and handle a data message
	 */
	private void receiveDataMessage(ServerMessage message) {
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

	private ClientUI clientUI() {
		return Main.clientUI;
	}

}
