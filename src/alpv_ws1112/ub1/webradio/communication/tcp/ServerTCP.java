package alpv_ws1112.ub1.webradio.communication.tcp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.sound.sampled.UnsupportedAudioFileException;

import alpv_ws1112.ub1.webradio.communication.Server;

public class ServerTCP implements Server {

	private ServerSocket _socket;
	private boolean _close = false;
	private ServerTCPLion _lion;
	
	public ServerTCP(int port) throws IOException {
		// Create socket
		_socket = new ServerSocket(port);
		_socket.setSoTimeout(500); // Only 500ms timeout
		// Create lion
		_lion = new ServerTCPLion(this);
		(new Thread(_lion)).start();

		System.out.println("Starting server using port \"" + port + "\".");
	}

	/**
	 * Set the close flag
	 */
	public void close() {
		_close = true;
	}

	/**
	 * Play a specific song
	 */
	public void playSong(String path) throws MalformedURLException,
			UnsupportedAudioFileException, IOException {

		System.out.println("Play song: " + path);

		try {
			// Initialize the stream
			_lion.playSong(path);
		} catch (MalformedURLException e) {
			System.err.println("Can't find audio file.");
		} catch (UnsupportedAudioFileException e) {
			System.err.println("Unsupported audio file.");
		} catch (IOException e) {
			System.err.println("Can't open audio file.");
		}
	}

	/**
	 * Handles client server socket connections
	 */
	public void run() {

		System.out.println("Server started.");

		while (!_close) {
			try {
				Socket client = _socket.accept();
				_lion.addClient(client);
			} catch (SocketTimeoutException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			// Close lion
			if (_lion != null) {
				_lion.close();
			}
			// Close socket
			_socket.close();

		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		System.out.println("Server closed.");
	}


}