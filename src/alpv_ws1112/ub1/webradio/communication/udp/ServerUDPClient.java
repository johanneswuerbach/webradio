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
	private ServerUDP _server;
	private DatagramSocket _socket;
	private Queue<Chat> _chats;

	ServerUDPClient(ServerUDP server, InetAddress host, int port) throws IOException {
		_host = host;
		_port = port;
		_socket = server.getSocket();
		_server = server;
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
		builder.setBufferSize(_server.getBufferSize());
		
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
		boolean hasData = false;

		// Add available audio data
		if (buffer != null) {
			hasData = true;
			builder.setData(ByteString.copyFrom(buffer));
		}

		// Add chat messages
		while (!_chats.isEmpty()) {
			hasData = true;
			Chat chat = _chats.poll();
			builder.addUsername(chat.getUsername());
			builder.addText(chat.getText());
		}

		// Send
		if(hasData) {
			sendPacket(builder);
		}
	}
	
	/**
	 * Receive a chat message
	 * 
	 * @throws IOException
	 */
	public Chat receiveChatMessage(String username, String message) throws IOException {
		System.out.println("Message from \"" + username + "\"");
		return new Chat(this, username, message);
	}
	
	private void sendPacket(ServerMessage.Builder builder) throws IOException {

		ServerMessage serverMessage = builder.build();
		byte[] bytes = serverMessage.toByteArray();

		DatagramPacket packet = new DatagramPacket(bytes, bytes.length, _host,
				_port);
		_socket.send(packet);
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
	
	public boolean equals(Object o) {
		if(o instanceof ServerUDPClient) {
			ServerUDPClient other = (ServerUDPClient) o;
			return this._host.equals(other._host) && (other._port == this._port);
		}
		return false;
	}

}
