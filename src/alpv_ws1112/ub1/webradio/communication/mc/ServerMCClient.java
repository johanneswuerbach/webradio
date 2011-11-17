package alpv_ws1112.ub1.webradio.communication.mc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;

import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.communication.ByteArray;
import alpv_ws1112.ub1.webradio.protobuf.Messages.StartMessage;

import com.google.protobuf.ByteString;

/**
 * A client
 */
public class ServerMCClient {

	private Socket _socket;
	private OutputStream _os;

	ServerMCClient(Socket socket) throws IOException {
		_socket = socket;
		_os = socket.getOutputStream();
	}

	/**
	 * Send audio format
	 * 
	 * @throws IOException
	 */
	public void sendInformation(AudioFormat audioFormat, int bufferSize, String networkGroup, int networkGroupPort) throws IOException {
		AudioFormatTransport aft = new AudioFormatTransport(audioFormat);
		byte[] format = ByteArray.toBytes(aft);
		StartMessage.Builder builder = StartMessage.newBuilder();
		builder.setNetworkGroup(networkGroup);
		builder.setNetworkGroupPort(networkGroupPort);
		builder.setBufferSize(bufferSize);
		builder.setData(ByteString.copyFrom(format));
		StartMessage message = builder.build();
		message.writeDelimitedTo(_os);

		System.out.println("InitialInformation transmitted.");
	}

	/**
	 * Close client socket
	 */
	public void close() {
		try {
			_socket.close();
		} catch (IOException e) {
			System.err.println("Can't close client connection.");
		}
	}
}
