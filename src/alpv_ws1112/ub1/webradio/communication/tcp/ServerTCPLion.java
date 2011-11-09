package alpv_ws1112.ub1.webradio.communication.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.protobuf.ByteString;

import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.audioplayer.AudioPlayer;
import alpv_ws1112.ub1.webradio.communication.ByteArray;
import alpv_ws1112.ub1.webradio.protobuf.Messages.WebradioMessage;

/**
 * AudioFile handling
 */
public class ServerTCPLion implements Runnable {

	private static final int BUFFER_SIZE = 64;

	private AudioInputStream _ais;
	private AudioFormat _audioFormat;
	private ServerTCP _server;
	private String _path;
	private List<Client> _clients; // Connected clients
	private Queue<ChatMessage> _messageQueue;
	private boolean _close = false;

	public ServerTCPLion(ServerTCP server) {
		_server = server;
		_clients = new CopyOnWriteArrayList<Client>();
		_messageQueue = new ArrayBlockingQueue<ChatMessage>(100);
	}

	/**
	 * Read the next list of bytes from the audio file
	 */
	public void run() {
		while (!_close) {
			byte[] buffer = null;
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
			}

			ChatMessage chatMessage = null;
			if (!_messageQueue.isEmpty()) {
				chatMessage = _messageQueue.poll();
			}

			for (Client client : _clients) {
				try {
					if (client.hasChatMessage()) {
						try {
							ChatMessage message = client.receiveChatMessage();
							if (message != null) {
								_messageQueue.add(message);
							}
						} catch (IOException e) {
							System.err
									.println("Error while receiving chat message.");
						}
					}

					if (chatMessage != null && !chatMessage.isSource(client)) {
						client.sendMessage(chatMessage.getMessage());
					}

					if (buffer != null) {
						client.sendAudioData(buffer);
					}

				} catch (IOException e) {
					client.close();
					_clients.remove(client);
				}
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}

		for (Client client : _clients) {
			client.close();
		}

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

	public boolean isPlaying() {
		return _audioFormat != null;
	}

	public void addClient(Socket socket) {
		try {

			Client client = new Client(socket);
			if (isPlaying()) {
				client.sendAudioFormat(_audioFormat);
			}
			_clients.add(client);

			System.out.println("New Client added.");

		} catch (IOException e) {
			System.err.println("Client not added. IO-Exception.");
		}
	}

	private class Client {

		private Socket _socket;
		private InputStream _is;
		private OutputStream _os;

		Client(Socket socket) throws IOException {
			_socket = socket;
			_is = socket.getInputStream();
			_os = socket.getOutputStream();
		}

		public void sendMessage(WebradioMessage message) throws IOException {
			message.writeDelimitedTo(_os);
		}

		public void sendAudioFormat(AudioFormat audioFormat) throws IOException {
			System.out.println("Start: sendAudioFormat");
			AudioFormatTransport aft = new AudioFormatTransport(audioFormat);
			byte[] format = ByteArray.toBytes(aft);
			WebradioMessage.Builder builder = WebradioMessage.newBuilder();
			builder.setData(ByteString.copyFrom(format));
			builder.setIsAudioData(false);
			builder.setIsAudioFormat(true);
			builder.setIsChatMessage(false);
			WebradioMessage message = builder.build();
			sendMessage(message);
			System.out.println("End: sendAudioFormat");
		}

		/**
		 * Send audioData
		 * 
		 * @throws IOException
		 */
		public void sendAudioData(byte[] buffer) throws IOException {

			WebradioMessage.Builder builder = WebradioMessage.newBuilder();
			builder.setData(ByteString.copyFrom(buffer));
			builder.setIsAudioData(true);
			builder.setIsAudioFormat(false);
			builder.setIsChatMessage(false);
			WebradioMessage message = builder.build();
			assert (message.isInitialized());
			sendMessage(message);
		}

		public boolean hasChatMessage() throws IOException {
			return _is.available() > 0;
		}

		/**
		 * Receive a chat message
		 */
		public ChatMessage receiveChatMessage() throws IOException {

			System.out.println("Start: receiveWebradioMessage");

			ChatMessage chatMessage = null;

			int size = _is.read();
			byte[] bytes = new byte[size];
			_is.read(bytes);
			WebradioMessage message = WebradioMessage.parseFrom(bytes);
			if (message.getIsChatMessage()) {
				System.out.println("Message from " + message.getUsername()
						+ " received: " + message.getTextMessage());

				chatMessage = new ChatMessage(this, message);
			} else {
				System.err.println("not a text message");
			}

			System.out.println("End: receiveWebradioMessage");

			return chatMessage;
		}

		public void close() {
			try {
				_socket.close();
			} catch (IOException e) {
				System.err.println("Can't close client connection.");
			}
		}

	}

	private class ChatMessage {
		private Client _source;
		private WebradioMessage _message;

		ChatMessage(Client source, WebradioMessage message) {
			_source = source;
			_message = message;
		}

		public boolean isSource(Client client) {
			return _source.equals(client);
		}

		public WebradioMessage getMessage() {
			return _message;
		}
	}
}