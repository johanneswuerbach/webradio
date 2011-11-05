package alpv_ws1112.ub1.webradio.communication.protobuf;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import javax.sound.sampled.AudioFormat;

import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.audioplayer.AudioPlayer;
import alpv_ws1112.ub1.webradio.communication.ByteArray;
import alpv_ws1112.ub1.webradio.communication.Client;
import alpv_ws1112.ub1.webradio.protobuf.Messages.AudioFormatData;
import alpv_ws1112.ub1.webradio.protobuf.Messages.TextMessage;

/**
 * A TCP client for the webradio
 */
public class ClientProtoBuf implements Client {

	private static final int BUFFER_SIZE = 64;

	private Socket _socket;
	private boolean _close = false;
	private InputStream _inputStream;
	private OutputStream _outputStream;

	/**
	 * Play the music
	 */
	public void run() {
		// Start player and receive audio format
		AudioPlayer audioPlayer;
		try {
			audioPlayer = new AudioPlayer(receiveAudioFormat());
		} catch (IOException e) {
			System.err.println("Can't receive audio format.");
			e.printStackTrace();
			System.exit(1);
			return;
		}

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