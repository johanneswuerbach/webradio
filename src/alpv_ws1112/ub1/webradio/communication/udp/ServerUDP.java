package alpv_ws1112.ub1.webradio.communication.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.protobuf.InvalidProtocolBufferException;

import alpv_ws1112.ub1.webradio.audioplayer.AudioPlayer;
import alpv_ws1112.ub1.webradio.communication.Server;
import alpv_ws1112.ub1.webradio.communication.udp.ServerUDPClient;
import alpv_ws1112.ub1.webradio.communication.udp.ServerUDPClient.Chat;
import alpv_ws1112.ub1.webradio.protobuf.Messages.ClientMessage;

public class ServerUDP implements Server {

	private static final int BUFFER_SIZE = 256;

	private AudioInputStream _ais;
	private AudioFormat _audioFormat;
	private String _path;
	private DatagramSocket _socket;
	private boolean _close = false;
	private List<ServerUDPClient> _clients; // Connected clients

	public ServerUDP(int port) throws IOException {
		// Create socket
		_socket = new DatagramSocket(port);
		_socket.setSoTimeout(1000);

		_clients = new ArrayList<ServerUDPClient>();
		System.out.println("Starting server using port \"" + port + "\".");
	}

	public void run() {
		byte[] audioBuffer;
		while (!_close) {
			// Read audio data, if available
			if (_ais != null) {
				audioBuffer = new byte[BUFFER_SIZE];
				try {
					if (_ais.read(audioBuffer) <= 0) {
						_ais = AudioPlayer.getAudioInputStream(_path);
						_ais.read(audioBuffer);
					}
				} catch (IOException e) {
					System.err.println("IO-Error while reading the file.");
					close();
				} catch (UnsupportedAudioFileException e) {
					System.err.println("Unsupported file type.");
					close();
				}
			} else {
				audioBuffer = null;
				// Fixed heavy CPU load
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
				}

			}
			// Receive data
			try {
				byte[] buffer = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				_socket.receive(packet);

				byte[] bytes = new byte[packet.getLength()];
				for (int i = 0; i < packet.getLength(); i++) {
					bytes[i] = buffer[i];
				}

				System.out.println("New paket (Size: " + bytes.length + ")");

				try {
					ClientMessage message = ClientMessage.parseFrom(bytes);
					ServerUDPClient client = new ServerUDPClient(_socket,
							packet.getAddress(), packet.getPort());
					
					try {
						if (message.hasConnection()) {
							// Connection status change
							if (message.getConnection()) {
								if (!_clients.contains(client)) {
									if (_audioFormat != null) {
										client.sendAudioFormat(_audioFormat);
									}
									_clients.add(client);
									System.out.println("Client connected ("
											+ packet.getAddress().getHostName()
											+ ":" + packet.getPort() + ")");
								}
							} else {
								_clients.remove(client);
								System.out.println("Client disconnected ("
										+ packet.getAddress().getHostName()
										+ ":" + packet.getPort() + ")");
							}
						} else {
							// Chat message
							Chat chat = client.receiveChatMessage(message.getUsername(), message.getText());
							for (ServerUDPClient chatClient : _clients) {
								if (!chat.isSource(chatClient)) {
									chatClient.addChat(chat);
								}
							}
						}
					} catch (IOException e) {
						System.err
								.println("Error while establishing connection.");
					}
				} catch (InvalidProtocolBufferException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (SocketTimeoutException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// Send data
			try {
				for(ServerUDPClient client : _clients) {
					client.sendDataMessage(audioBuffer);
				}
				
			}
			catch(IOException e) {
			}

		}

		_socket.close();
	}

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

		for (ServerUDPClient client : _clients) {
			try {
				client.sendAudioFormat(_audioFormat);
			} catch (IOException e) {
				_clients.remove(client);
			}
		}
	}

}
