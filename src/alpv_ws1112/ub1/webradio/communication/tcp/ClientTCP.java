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

public class ClientTCP implements Client {

	private static final int BUFFER_SIZE = 1024;

	private Socket _socket;
	private boolean _closed = false;
	private InputStream _inputStream;

	@Override
	public void run() {

		AudioPlayer audioPlayer = new AudioPlayer(
				this.receiveAudioFormat(_inputStream));
		;
		
		Thread AudioPlayerThread = new Thread(audioPlayer);
		AudioPlayerThread.start();
		
		System.out.println("Size: " + audioPlayer.getSourceDataLine().getBufferSize());

		while (!_closed) {

			try {
				byte[] buffer = new byte[BUFFER_SIZE];

				while (_inputStream.read(buffer) > 0) {
					audioPlayer.writeBytes(buffer);
					audioPlayer.start();
				}

			} catch (EOFException e) {
				System.err.println("Can't play audio.");
				this.close();
			} catch (SocketException e) {
				System.err.println("Can't play audio.");
				this.close();
			} catch (IOException e) {
				System.err.println("Can't play audio.");
				this.close();
			}

		}

		try {
			_inputStream.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public void connect(InetSocketAddress serverAddress) throws IOException {
		String host = serverAddress.getHostName();
		int port = serverAddress.getPort();

		System.out.println("Client connecting to \"" + host + ":" + port
				+ "\".");

		_socket = new Socket(serverAddress.getHostName(),
				serverAddress.getPort());

		_inputStream = _socket.getInputStream();
	}

	public void close() {
		_closed = true;
	}

	@Override
	public void sendChatMessage(String message) throws IOException {
		// TODO Auto-generated method stub

	}

	private AudioFormat receiveAudioFormat(InputStream is) {

		AudioFormat audioFormat = null;

		try {
			// first 4 bytes containing the size
			byte[] lengthBuffer = new byte[4];
			is.read(lengthBuffer);

			// byte[] -> int
			int length = 0;
			for (int i = 0; i < 4; ++i) {
				length |= (lengthBuffer[3 - i] & 0xff) << (i << 3);
			}

			System.out.println("Länge: " + length);

			// read format
			byte[] formatBuffer = new byte[length];
			is.read(formatBuffer);

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