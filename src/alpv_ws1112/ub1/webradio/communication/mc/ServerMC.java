package alpv_ws1112.ub1.webradio.communication.mc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;

import alpv_ws1112.ub1.webradio.communication.Server;

/**
 * Server for multicast - Includes Streamer (audio -> multicast group) - List of
 * TCP clients
 */
public class ServerMC implements Server {

	private ServerSocket _socket;
	private boolean _close = false;
	private ArrayList<ServerMCClient> _clients; // Contains clients, if music is
												// not playing
	private ServerMCStreamer _streamer;
	private int _port;

	public ServerMC(int port) throws IOException {
		// Create socket
		_port = port;
		_socket = new ServerSocket(_port);
		_socket.setSoTimeout(500); // Only 500ms timeout
		_clients = new ArrayList<ServerMCClient>();
	}

	/**
	 * Client handling
	 */
	public void run() {

		System.out.println("Server started.");

		while (!_close) {
			try {
				Socket socket = _socket.accept();
				if (_streamer == null) {
					// Store clients until music starts
					_clients.add(new ServerMCClient(socket));
				} else {
					// Accept clients
					ServerMCClient client = new ServerMCClient(socket);
					client.sendInformation(_streamer.getAudioFormat(),
							_streamer.getBufferSize(),
							_streamer.getNetworkGroup(),
							_streamer.getNetworkGroupPort());
					client.close();
				}

			} catch (SocketTimeoutException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Close server
	 */
	public void close() {
		_close = true;
	}

	/**
	 * Play an audio file
	 */
	public void playSong(String path) throws MalformedURLException,
			UnsupportedAudioFileException, IOException {

		if (_streamer == null) {
			// First song, publish format to all pending clients
			_streamer = new ServerMCStreamer(path, _port);
			for (ServerMCClient client : _clients) {
				client.sendInformation(_streamer.getAudioFormat(),
						_streamer.getBufferSize(), _streamer.getNetworkGroup(),
						_streamer.getNetworkGroupPort());
				client.close();
			}
			_clients.clear();
			(new Thread(_streamer)).run();
		} else {
			// Next songs
			_streamer.changePath(path);
		}
	}

}
