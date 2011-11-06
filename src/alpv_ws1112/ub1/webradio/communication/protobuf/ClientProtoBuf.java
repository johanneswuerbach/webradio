package alpv_ws1112.ub1.webradio.communication.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;

import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.communication.ByteArray;
import alpv_ws1112.ub1.webradio.communication.Client;
import alpv_ws1112.ub1.webradio.protobuf.Messages.AudioFormatData;
import alpv_ws1112.ub1.webradio.protobuf.Messages.TextMessage;
import alpv_ws1112.ub1.webradio.ui.ClientUI;
import alpv_ws1112.ub1.webradio.ui.cmd.ClientCMD;
import alpv_ws1112.ub1.webradio.ui.swing.ClientSwing;

/**
 * A TCP client for the webradio
 */
public class ClientProtoBuf implements Client {

	private static final int BUFFER_SIZE = 64;
	private Socket _socket;
	private boolean _close = false;
	private InputStream _inputStream;
	private OutputStream _outputStream;
	private String _username;
	private boolean _useGUI;
	private ClientUI _clientUI;

	public ClientProtoBuf(String host, int port, String username, boolean useGUI) {
		_useGUI = useGUI;
		_username = username;
		try {
			connect(InetSocketAddress.createUnresolved(host, port));
		} catch (IOException e) {
			System.err.println("Can't connect to host " + host + " on port "
					+ port);
		}

	}

	@Override
	public void run() {
		startClientUi();
		while (!_close) {
			int size;
			try {
				size = _inputStream.read();
				byte[] bytes = new byte[size];
				_inputStream.read(bytes);
				TextMessage message = TextMessage.parseFrom(bytes);
				System.out.println("Message received: "
						+ message.getTextMessage());
				_clientUI.pushChatMessage(message.getUsername() + ": "
						+ message.getTextMessage());
			} catch (IOException e) {
				System.err.println("Can't reveice message from server.");
				e.printStackTrace();
			}
		}
	}

	private void startClientUi() {
		if (_useGUI) {
			_clientUI = new ClientSwing(this, _username);
		} else {
			_clientUI = new ClientCMD(this, _username);
		}
		Thread clientUIThread = new Thread(_clientUI);
		clientUIThread.start();
	}

	//	/**
	//	 * Play the music
	//	 */
	//	public void run() {
	//		// Start player and receive audio format
	//		AudioPlayer audioPlayer;
	//		try {
	//			audioPlayer = new AudioPlayer(receiveAudioFormat());
	//		} catch (IOException e) {
	//			System.err.println("Can't receive audio format.");
	//			e.printStackTrace();
	//			System.exit(1);
	//			return;
	//		}
	//
	//		// Start receiving bytes and playing music
	//		byte[] buffer = new byte[BUFFER_SIZE];
	//		boolean first = true;
	//		try {
	//			while (!_close && _inputStream.read(buffer) > 0) {
	//				if (first) {
	//					System.out.print("Start playing.");
	//					first = false;
	//				}
	//				audioPlayer.start();
	//				audioPlayer.writeBytes(buffer);
	//			}
	//		} catch (EOFException e) {
	//			System.err.println("Can't play audio.");
	//			close();
	//		} catch (SocketException e) {
	//			System.err.println("Can't play audio.");
	//			close();
	//		} catch (IOException e) {
	//			System.err.println("Can't play audio.");
	//			close();
	//		}
	//
	//		// Close the stream
	//		try {
	//			_inputStream.close();
	//		} catch (IOException e) {
	//			System.err.println(e.getMessage());
	//		}
	//	}

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
		System.out.println("sending chat message to server.");
		TextMessage.Builder builder = TextMessage.newBuilder();
		builder.setTextMessage(message);
		builder.setUsername(_username);
		TextMessage textMessage = builder.build();
		//		textMessage.writeTo(_outputStrem);
		byte size = (byte) textMessage.getSerializedSize();
		_outputStream.write(size);
		_outputStream.write(textMessage.toByteArray());
	}

	/**
	 * Receive the audio format form the server
	 * 
	 * @return
	 * @throws IOException
	 */
	private AudioFormat receiveAudioFormat() throws IOException {
		try {
			AudioFormatData.Builder builder = AudioFormatData.newBuilder();
			builder.mergeFrom(_inputStream);
			AudioFormatData audioFormatMessage = builder.build();
			AudioFormat audioFormat = ((AudioFormatTransport) ByteArray
					.toObject(audioFormatMessage.getData().toByteArray()))
					.getAudioFormat();
			System.out.println("Audio Format: " + audioFormat.toString());
			return audioFormat;
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

}