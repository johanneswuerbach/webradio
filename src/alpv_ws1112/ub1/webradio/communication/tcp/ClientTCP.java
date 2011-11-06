package alpv_ws1112.ub1.webradio.communication.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import javax.sound.sampled.AudioFormat;

import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.audioplayer.AudioPlayer;
import alpv_ws1112.ub1.webradio.communication.ByteArray;
import alpv_ws1112.ub1.webradio.communication.Client;
import alpv_ws1112.ub1.webradio.ui.ClientUI;
import alpv_ws1112.ub1.webradio.ui.cmd.ClientCMD;
import alpv_ws1112.ub1.webradio.ui.swing.ClientSwing;

/**
 * A TCP client for the webradio
 */
public class ClientTCP implements Client {

	private static final int BUFFER_SIZE = 64;

	private Socket _socket;
	private boolean _close = false;
	private InputStream _inputStream;
	private boolean _useGUI;
	private String _username;

	public ClientTCP(String host, int port, String username, boolean useGUI) {
		_useGUI = useGUI;
		_username = username;
		try {
			connect(InetSocketAddress.createUnresolved(host, port));
		} catch (IOException e) {
			System.err.println("Can't connect to host " + host + " on port "
					+ port);
			return;
		}
	}

	/**
	 * Play the music
	 */
	public void run() {
		startClientUI();
		// Start player and receive audio format
		AudioPlayer audioPlayer = new AudioPlayer(receiveAudioFormat());

		// Start receiving bytes and playing music
		byte[] buffer = new byte[BUFFER_SIZE];
		boolean first = true;
		try {
			while (!_close && _inputStream.read(buffer) > 0) {
				if (first) {
					System.out.print("Start playing.");
					first = false;
				}
				audioPlayer.start();
				audioPlayer.writeBytes(buffer);
			}
		} catch (EOFException e) {
			System.err.println("Can't play audio.");
			close();
		} catch (SocketException e) {
			System.err.println("Can't play audio.");
			close();
		} catch (IOException e) {
			System.err.println("Can't play audio.");
			close();
		}

		// Close the stream
		try {
			_inputStream.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	private void startClientUI() {
		ClientUI clientUI = null;
		// Run UI
		if (_useGUI) {
			clientUI = new ClientSwing(this, _username);
		} else {
			clientUI = new ClientCMD(this, _username);
		}
		Thread clientUIThread = new Thread(clientUI);
		clientUIThread.start();
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
	}

	/**
	 * Closes the client
	 */
	public void close() {
		_close = true;
	}

	@Override
	public void sendChatMessage(String message) throws IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * Receive the audio format form the server
	 * 
	 * @return
	 */
	private AudioFormat receiveAudioFormat() {

		AudioFormat audioFormat = null;

		try {
			// first 4 bytes containing the size
			byte[] lengthBuffer = new byte[4];
			_inputStream.read(lengthBuffer);

			// byte[] -> int
			int length = 0;
			for (int i = 0; i < 4; ++i) {
				length |= (lengthBuffer[3 - i] & 0xff) << (i << 3);
			}

			System.out.println("Länge: " + length);

			// read format
			byte[] formatBuffer = new byte[length];
			_inputStream.read(formatBuffer);

			AudioFormatTransport aft = (AudioFormatTransport) ByteArray
					.toObject(formatBuffer);
			audioFormat = aft.getAudioFormat();

			System.out.println("AudioFormat received.");
			System.out.println("Format: " + audioFormat.toString());

		} catch (ClassNotFoundException e) {
			System.err.println("Can't receive audio format.");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Can't receive audio format.");
			System.exit(1);
		}

		return audioFormat;
	}
}