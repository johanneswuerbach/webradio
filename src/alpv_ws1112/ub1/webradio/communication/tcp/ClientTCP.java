package alpv_ws1112.ub1.webradio.communication.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import alpv_ws1112.ub1.webradio.communication.Client;

public class ClientTCP implements Client {

	private Socket _socket;

	@Override
	public void run() {
		while (true) {

			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(_socket.getInputStream()));

				System.out.println("Client: " + br.readLine());

				br.close();
			} catch (SocketException e) {
				this.close();
			} catch (IOException e) {
				System.out.println("Can't read input stream.");
				e.printStackTrace();
			}

		}

	}

	public void connect(InetSocketAddress serverAddress) throws IOException {
		String host = serverAddress.getHostName();
		int port = serverAddress.getPort();

		System.out.println("Client connecting to \"" + host + ":" + port
				+ "\".");
		_socket = new Socket(serverAddress.getHostName(),
				serverAddress.getPort());
	}

	public void close() {
		try {
			_socket.close();
		} catch (IOException e) {
			System.out.println("Can't close client socket.");
			e.printStackTrace();
		}
	}

	@Override
	public void sendChatMessage(String message) throws IOException {
		// TODO Auto-generated method stub

	}

}