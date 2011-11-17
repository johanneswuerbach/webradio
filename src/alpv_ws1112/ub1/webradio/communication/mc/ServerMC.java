package alpv_ws1112.ub1.webradio.communication.mc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;

import alpv_ws1112.ub1.webradio.communication.Server;

public class ServerMC implements Server {

	private ServerSocket _socket;
	private boolean _close = false;
	private ArrayList<ServerMCClient> _clients;
	private ServerMCStreamer _streamer;
	private int _port;

	public ServerMC(int port) throws IOException {
		// Create socket
		_port = port;
		_socket = new ServerSocket(_port);
		_socket.setSoTimeout(500); // Only 500ms timeout
		_clients = new ArrayList<ServerMCClient>();
	}

	public void run() {

		System.out.println("Server started.");

		while (!_close) {
			try {
				//
				Socket socket = _socket.accept();

				if (_streamer == null) {
					// Store clients until music starts
					_clients.add(new ServerMCClient(socket));
				} else {
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

	public void close() {
		_close = true;
	}

	public void playSong(String path) throws MalformedURLException,
			UnsupportedAudioFileException, IOException {

		if (_streamer == null) {
			// First song
			_streamer = new ServerMCStreamer(path, _port);
			for (ServerMCClient client : _clients) {
				client.sendInformation(_streamer.getAudioFormat(),
						_streamer.getBufferSize(), _streamer.getNetworkGroup(),
						_streamer.getNetworkGroupPort());
				client.close();
			}
			_clients.clear();
			(new Thread(_streamer)).run();
		}
		else {
			// Next songs
			_streamer.changePath(path);
		}

	}

}
