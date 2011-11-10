package alpv_ws1112.ub1.webradio.communication.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.sound.sampled.AudioFormat;

import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.communication.ByteArray;
import alpv_ws1112.ub1.webradio.protobuf.Messages.ChatMessage;
import alpv_ws1112.ub1.webradio.protobuf.Messages.WebradioMessage;

import com.google.protobuf.ByteString;

/**
 * A client
 */
public class ServerTCPClient {

	private Socket _socket;
	private InputStream _is;
	private OutputStream _os;
	private Queue<Chat> _chats;

	ServerTCPClient(Socket socket) throws IOException {
		_socket = socket;
		_is = socket.getInputStream();
		_os = socket.getOutputStream();
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
		WebradioMessage.Builder builder = WebradioMessage.newBuilder();
		builder.setIsAudioFormat(true);
		builder.setIsDataMessage(false);
		builder.setData(ByteString.copyFrom(format));
		WebradioMessage message = builder.build();
		message.writeDelimitedTo(_os);

		System.out.println("AudioFormat transmitted.");
	}

	/**
	 * Send data (includs audio and chat)
	 * 
	 * @throws IOException
	 */
	public void sendDataMessage(byte[] buffer) throws IOException {

		WebradioMessage.Builder builder = WebradioMessage.newBuilder();
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
		WebradioMessage message = builder.build();
		assert (message.isInitialized());
		message.writeDelimitedTo(_os);
	}

	/**
	 * Check, whether a new chat message is available
	 * 
	 * @throws IOException
	 */
	public boolean hasChatMessage() throws IOException {
		return _is.available() > 0;
	}

	/**
	 * Receive a chat message
	 * 
	 * @throws IOException
	 */
	public Chat receiveChatMessage() throws IOException {
		ChatMessage message = ChatMessage.parseDelimitedFrom(_is);
		System.out.println("Message from \"" + message.getUsername() + "\"");
		return new Chat(this, message.getUsername(), message.getText());
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

	/**
	 * A chat line
	 */
	protected class Chat {
		private ServerTCPClient _source;
		private String _username, _text;

		Chat(ServerTCPClient source, String username, String text) {
			_source = source;
			_username = username;
			_text = text;
		}

		/**
		 * Is the client to source of this message?
		 */
		public boolean isSource(ServerTCPClient client) {
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
