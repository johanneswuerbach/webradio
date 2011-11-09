package alpv_ws1112.ub1.webradio.communication.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.protobuf.ByteString;

import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.audioplayer.AudioPlayer;
import alpv_ws1112.ub1.webradio.communication.ByteArray;
import alpv_ws1112.ub1.webradio.protobuf.Messages.ChatMessage;
import alpv_ws1112.ub1.webradio.protobuf.Messages.WebradioMessage;

/**
 * AudioFile handling
 */
public class ServerTCPLion implements Runnable {

	private static final int BUFFER_SIZE = 256;

	private AudioInputStream _ais;
	private AudioFormat _audioFormat;
	private ServerTCP _server;
	private String _path;
	private List<Client> _clients; // Connected clients
	private boolean _close = false;

	public ServerTCPLion(ServerTCP server) {
		_server = server;
		_clients = new CopyOnWriteArrayList<Client>();
	}

	/**
	 * Read the next list of bytes from the audio file
	 */
	public void run() {
		byte[] buffer;
		while (!_close) {

			// Read audio data, if available
			if (_ais != null) {
				buffer = new byte[BUFFER_SIZE];
				try {
					if (_ais.read(buffer) <= 0) {
						_ais = AudioPlayer.getAudioInputStream(_path);
						_ais.read(buffer);
					}
				} catch (IOException e) {
					System.err.println("IO-Error while reading the file.");
					_server.close();
				} catch (UnsupportedAudioFileException e) {
					System.err.println("Unsupported file type.");
					_server.close();
				}
			} else {
				buffer = null;
				// Fixed heavy CPU load
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
				}

			}

			// Send audio data to all clients and check for new chat messages
			for (Client client : _clients) {
				try {
					if (client.hasChatMessage()) {
						try {
							Chat chat = client.receiveChatMessage();
							for (Client chatClient : _clients) {
								if (!chat.isSource(chatClient)) {
									chatClient.addChat(chat);
								}
							}
						} catch (IOException e) {
							System.err
									.println("Error while receiving chat message.");
						}
					}

					client.sendDataMessage(buffer);

				} catch (IOException e) {
					client.close();
					_clients.remove(client);
				}
			}

		}

		// Close all connections
		for (Client client : _clients) {
			client.close();
		}

		// Close stream
		if (_ais != null) {
			try {
				_ais.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sets the close flag
	 */
	public void close() {
		_close = true;
	}

	/**
	 * Play a song
	 * 
	 * @param path
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 * @throws MalformedURLException
	 */
	public void playSong(String path) throws MalformedURLException,
			UnsupportedAudioFileException, IOException {
		_path = path;
		_ais = AudioPlayer.getAudioInputStream(_path);
		_audioFormat = _ais.getFormat();

		for (Client client : _clients) {
			try {
				client.sendAudioFormat(_audioFormat);
			} catch (IOException e) {
				client.close();
				_clients.remove(client);
			}
		}
	}

	/**
	 * Add a new client to client pool
	 */
	public void addClient(Socket socket) {
		try {

			Client client = new Client(socket);
			if (_audioFormat != null) {
				client.sendAudioFormat(_audioFormat);
			}
			_clients.add(client);

			System.out.println("New Client added.");

		} catch (IOException e) {
			System.err.println("Client not added. IO-Exception.");
		}
	}

	/**
	 * A client
	 */
	private class Client {

		private Socket _socket;
		private InputStream _is;
		private OutputStream _os;
		private Queue<Chat> _chats;

		Client(Socket socket) throws IOException {
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
			System.out
					.println("Message from \"" + message.getUsername() + "\"");
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

	}

	/**
	 * A chat line
	 */
	private class Chat {
		private Client _source;
		private String _username, _text;

		Chat(Client source, String username, String text) {
			_source = source;
			_username = username;
			_text = text;
		}

		/**
		 * Is the client to source of this message?
		 */
		public boolean isSource(Client client) {
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