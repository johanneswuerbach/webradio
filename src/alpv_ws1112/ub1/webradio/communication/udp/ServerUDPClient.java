package alpv_ws1112.ub1.webradio.communication.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.sound.sampled.AudioFormat;

import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.communication.ByteArray;
import alpv_ws1112.ub1.webradio.protobuf.Messages.ServerMessage;

import com.google.protobuf.ByteString;

/**
 * A client
 */
public class ServerUDPClient {

	private InetAddress _host;
	private int _port;
	private DatagramSocket _socket;
	private Queue<Chat> _chats;

	ServerUDPClient(DatagramSocket socket, InetAddress host, int port) throws IOException {
		_host = host;
		_port = port;
		_socket = socket;
		_chats = new ArrayDeque<Chat>();
	}

	/**
	 * Add chat message to local queue
	 */
	public void addChat(Chat chat) {
		_chats.add(chat);
	}

	/**
	 * Send audio format
	 * 
	 * @throws IOException
	 */
	public void sendAudioFormat(AudioFormat audioFormat) throws IOException {
		AudioFormatTransport aft = new AudioFormatTransport(audioFormat);
		byte[] format = ByteArray.toBytes(aft);
		ServerMessage.Builder builder = ServerMessage.newBuilder();
		builder.setIsAudioFormat(true);
		builder.setIsDataMessage(false);
		builder.setData(ByteString.copyFrom(format));
		
		sendPacket(builder);

		System.out.println("AudioFormat transmitted.");
	}

	/**
	 * Send data (includs audio and chat)
	 * 
	 * @throws IOException
	 */
	public void sendDataMessage(byte[] buffer) throws IOException {

		ServerMessage.Builder builder = ServerMessage.newBuilder();
		builder.setIsDataMessage(true);
		builder.setIsAudioFormat(false);

		// Add available audio data
		if (buffer != null) {
			builder.setData(ByteString.copyFrom(buffer));
		}

		// Add chat messages
		while (!_chats.isEmpty()) {
			Chat chat = _chats.poll();
			builder.addUsername(chat.getUsername());
			builder.addText(chat.getText());
		}

		// Send
		sendPacket(builder);
	}
	
	private void sendPacket(ServerMessage.Builder builder) throws IOException {

		ServerMessage serverMessage = builder.build();
		byte[] bytes = serverMessage.toByteArray();

		DatagramPacket packet = new DatagramPacket(bytes, bytes.length, _host,
				_port);
		_socket.send(packet);

		System.out.println("ServerMessage send. (Size: " + bytes.length + ")");
	}

	/**
	 * A chat line
	 */
	protected class Chat {
		private ServerUDPClient _source;
		private String _username, _text;

		Chat(ServerUDPClient source, String username, String text) {
			_source = source;
			_username = username;
			_text = text;
		}

		/**
		 * Is the client to source of this message?
		 */
		public boolean isSource(ServerUDPClient client) {
			return _source.equals(client);
		}

		/**
		 * Text of this message
		 */
		public String getText() {
			return _text;
		}

		/**
		 * User of this message
		 */
		public String getUsername() {
			return _username;
		}
	}

}
