package alpv_ws1112.ub1.webradio.communication.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import alpv_ws1112.ub1.webradio.communication.Client;
import alpv_ws1112.ub1.webradio.protobuf.Messages.ChatMessage;

public class ClientUDP implements Client {

	private boolean _close = false;
	private DatagramSocket _socket;
	private InetAddress _host;
	private int _port;

	public ClientUDP(String host, int port) {
		try {
			connect(InetSocketAddress.createUnresolved(host, port));
			_socket = new DatagramSocket();
		} catch (IOException e) {
			System.err.println("Can't connect to host " + host + " on port "
					+ port);
			System.exit(1);
		}
	}

	public void run() {
		while (!_close) {
			ChatMessage.Builder builder = ChatMessage.newBuilder();
			builder.setText("Test");
			builder.setUsername("Peter");

			ChatMessage chatMessage = builder.build();
			byte[] bytes = chatMessage.toByteArray();

			DatagramPacket packet;
			try {
				packet = new DatagramPacket(bytes, bytes.length, _host, _port);
				_socket.send(packet);
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Weg is' es");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		_socket.close();

	}

	@Override
	public void connect(InetSocketAddress serverAddress) throws IOException {
		
		_host = InetAddress.getByName(serverAddress.getHostName());
		_port = serverAddress.getPort();

		System.out.println("Client sending to \"" + _host + ":" + _port
				+ "\".");
	}

	public void close() {
		_close = true;
	}

	@Override
	public void sendChatMessage(String message) throws IOException {
		// TODO Auto-generated method stub

	}

}
