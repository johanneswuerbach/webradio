package alpv_ws1112.ub1.webradio.communication.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import javax.sound.sampled.UnsupportedAudioFileException;

import alpv_ws1112.ub1.webradio.communication.Server;

public class ServerUDP implements Server {

	private DatagramSocket _socket;
	private boolean _close = false;

	public ServerUDP(int port) throws IOException {
		// Create socket
		_socket = new DatagramSocket(port);
		_socket.setSoTimeout(20);
		System.out.println("Starting server using port \"" + port + "\".");
	}

	public void run() {
		while (!_close) {
			
			// Accept clients
			// Send data to all connected clients
			
			DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
			try {
				_socket.receive(packet);
			} catch (SocketTimeoutException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Empfänger auslesen
			InetAddress address = packet.getAddress();
			int port = packet.getPort();
			int len = packet.getLength();
			byte[] data = packet.getData();
			System.out.println("Anfrage von " + address + " vom Port " + port
					+ " Länge " + len + "\n" + new String(data, 0, len));
		}

		_socket.close();
	}

	public void close() {
		_close = true;
	}

	@Override
	public void playSong(String path) throws MalformedURLException,
			UnsupportedAudioFileException, IOException {
		// TODO Auto-generated method stub

	}

}
