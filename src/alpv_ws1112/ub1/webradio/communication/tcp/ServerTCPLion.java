package alpv_ws1112.ub1.webradio.communication.tcp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import alpv_ws1112.ub1.webradio.audioplayer.AudioPlayer;
import alpv_ws1112.ub1.webradio.communication.tcp.ServerTCPClient.Chat;

/**
 * AudioFile handling
 */
public class ServerTCPLion implements Runnable {

	private static final int BUFFER_SIZE = 256;

	private AudioInputStream _ais;
	private AudioFormat _audioFormat;
	private ServerTCP _server;
	private String _path;
	private List<ServerTCPClient> _clients; // Connected clients
	private boolean _close = false;

	public ServerTCPLion(ServerTCP server) {
		_server = server;
		_clients = new CopyOnWriteArrayList<ServerTCPClient>();
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
			for (ServerTCPClient client : _clients) {
				try {
					if (client.hasChatMessage()) {
						try {
							Chat chat = client.receiveChatMessage();
							for (ServerTCPClient chatClient : _clients) {
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
		for (ServerTCPClient client : _clients) {
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

		for (ServerTCPClient client : _clients) {
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

			ServerTCPClient client = new ServerTCPClient(socket);
			if (_audioFormat != null) {
				client.sendAudioFormat(_audioFormat);
			}
			_clients.add(client);

			System.out.println("New Client added.");

		} catch (IOException e) {
			System.err.println("Client not added. IO-Exception.");
		}
	}
}