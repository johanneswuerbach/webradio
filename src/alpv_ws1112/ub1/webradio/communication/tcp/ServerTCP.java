package alpv_ws1112.ub1.webradio.communication.tcp;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.sound.sampled.UnsupportedAudioFileException;

import alpv_ws1112.ub1.webradio.communication.Server;

public class ServerTCP implements Server {

	private ServerSocket _socket;
	private boolean _closed;

	// private Socket[] clients;

	public ServerTCP(int port) throws IOException {
		_socket = new ServerSocket(port);
		// Wait only 500ms for new connections
		_socket.setSoTimeout(500);
		
		System.out.println("Starting server using port \"" + port + "\".");
	}

	/**
	 * Set the close flag
	 */
	public void close() {
		_closed = true;
	}

	@Override
	public void playSong(String path) throws MalformedURLException,
			UnsupportedAudioFileException, IOException {
		// TODO Auto-generated method stub

	}

	public void writeHelloWorld(Socket client) throws IOException {

		OutputStreamWriter outputStream = new OutputStreamWriter(
				client.getOutputStream());

		PrintWriter printWriter = new PrintWriter(outputStream);
		printWriter.println("Hello World");
		printWriter.flush();
		outputStream.close();
		printWriter.close();

	}

	@Override
	public void run() {

		System.out.println("Server started.");

		while (!_closed) {
			Socket client = null;
			try {
				client = _socket.accept();
				this.writeHelloWorld(client);
			} catch (SocketTimeoutException e) {
				
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (client != null)
					try {
						client.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		
		try {
			// for (Socket client : clients) {
			// client.close();
			// }
			_socket.close();

		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

	}

}
