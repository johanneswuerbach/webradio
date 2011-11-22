package alpv_ws1112.ub1.webradio.communication.mc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.audioplayer.AudioPlayer;
import alpv_ws1112.ub1.webradio.communication.ByteArray;
import alpv_ws1112.ub1.webradio.communication.Client;
import alpv_ws1112.ub1.webradio.protobuf.Messages.Message;
import alpv_ws1112.ub1.webradio.protobuf.Messages.StartMessage;
import alpv_ws1112.ub1.webradio.ui.ClientUI;
import alpv_ws1112.ub1.webradio.webradio.Main;

public class ClientMC implements Client {

	private boolean _close = false;
	private Socket _socket;
	private MulticastSocket _mcSocket;
	private InetAddress _networkGroup;
	private int _bufferSize;
	private AudioPlayer _audioPlayer;

	public ClientMC(String host, int port) {
		try {
			connect(InetSocketAddress.createUnresolved(host, port));
		} catch (IOException e) {
			System.err.println("Can't connect to host " + host + " on port "
					+ port);
		}
	}

	/**
	 * Establish connection
	 */
	public void run() {

		// First TCP to establish connection
		while (!_close && _mcSocket == null) {
			try {
				StartMessage message = StartMessage.parseDelimitedFrom(_socket
						.getInputStream());

				_mcSocket = new MulticastSocket(message.getNetworkGroupPort());
				_networkGroup = InetAddress
						.getByName(message.getNetworkGroup());
				_mcSocket.joinGroup(_networkGroup);
				_bufferSize = message.getBufferSize();
				receiveAudioFormat(message.getData());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Now multicast
		System.out.println("Now multicast");
		while (!_close) {
			try {
				byte[] buffer = new byte[_bufferSize];
				DatagramPacket packet = new DatagramPacket(buffer,
						buffer.length);

				_mcSocket.receive(packet);

				// Trim package
				byte[] bytes = new byte[packet.getLength()];
				for (int i = 0; i < packet.getLength(); i++) {
					bytes[i] = buffer[i];
				}

				Message message = Message.parseFrom(bytes);
				if (message.hasData()) {
					// Play audio
					if (message.getIsAudioFormat()) {
						// New audio format
						receiveAudioFormat(message.getData());
						_bufferSize = message.getBufferSize();
					} else {
						// Music
						_audioPlayer.start();
						_audioPlayer
								.writeBytes(message.getData().toByteArray());
					}
				} else if (message.hasText() && message.hasUsername()) {
					// Show chat message
					displayMessage(message.getUsername(), message.getText());
				}

			} catch (InvalidProtocolBufferException e) {
				System.err.print("Can't receive message.");
				System.exit(0);
			} catch (IOException e) {
				System.err.print("Can't receive message.");
				System.exit(0);
			} catch (ClassNotFoundException e) {
				System.err.print("Can't receive message.");
				System.exit(0);
			}
		}

		try {
			_mcSocket.leaveGroup(_networkGroup);
			_mcSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Connect to a host via TCP
	 */
	public void connect(InetSocketAddress serverAddress) throws IOException {
		String host = serverAddress.getHostName();
		int port = serverAddress.getPort();

		System.out.println("Client connecting to \"" + host + ":" + port
				+ "\".");

		_socket = new Socket(host, port);
	}

	public void close() {
		_close = true;
	}

	/**
	 * Displays a chat message in the current UI
	 */
	private void displayMessage(String username, String message) {
		clientUI().pushChatMessage(username + ": " + message);
	}
	
	/**
	 * Send chat message to multicast group
	 */
	public void sendChatMessage(String text) throws IOException {
		
		// Server not playing -> no multicast group
		if (_mcSocket == null) {
			displayMessage("Error", "Server not ready");
			return;
		}

		System.out.println("Sending chat message to server.");
		Message.Builder builder = Message.newBuilder();
		builder.setIsAudioFormat(false);
		builder.setText(text);
		builder.setUsername(clientUI().getUserName());

		Message message = builder.build();
		byte[] bytes = message.toByteArray();

		DatagramPacket packet = new DatagramPacket(bytes, bytes.length,
				_networkGroup, _mcSocket.getLocalPort());
		try {
			_mcSocket.send(packet);
		} catch (IOException e) {
			System.err.print("Can't send chat message.");
		}
	}

	private ClientUI clientUI() {
		return Main.clientUI;
	}

	/**
	 * Parse AudioFormat from a byte array and set audioPlayer
	 * @param data
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void receiveAudioFormat(ByteString data) throws IOException,
			ClassNotFoundException {
		AudioFormat audioFormat = ((AudioFormatTransport) ByteArray
				.toObject(data.toByteArray())).getAudioFormat();
		System.out.println("Audio Format: " + audioFormat.toString());
		_audioPlayer = new AudioPlayer(audioFormat);
	}

}
