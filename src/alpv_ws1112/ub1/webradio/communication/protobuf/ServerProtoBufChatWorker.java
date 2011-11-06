package alpv_ws1112.ub1.webradio.communication.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import alpv_ws1112.ub1.webradio.protobuf.Messages.TextMessage;

/**
 * Handeling all clients and receive their chat messages.
 */
public class ServerProtoBufChatWorker implements Runnable {
	ServerProtoBuf _server;
	Socket _client;
	private boolean _close = false;
	private InputStream _inputStream;
	private OutputStream _outputStream;

	public ServerProtoBufChatWorker(ServerProtoBuf server, Socket client)
			throws IOException {
		_client = client;
		_inputStream = _client.getInputStream();
		_outputStream = _client.getOutputStream();
		_server = server;
	}

	/**
	 * Set the close flag
	 */
	public void close() {
		_close = true;
	}

	/**
	 * Runs the chat server
	 */
	public void run() {
		System.out.println("Chat Worker started.");
		while (!_close) {
			try {
				receiveMessages();
			} catch (IOException e) {
				System.err
						.println("Can't receive message. Closing client socket.");
				close();
			}
		}
		try {
			_inputStream.close();
		} catch (IOException e) {
			System.err.println("Can't send close client connection.");
		}
	}

	private void receiveMessages() throws IOException {
		int size = _inputStream.read();
		byte[] bytes = new byte[size];
		_inputStream.read(bytes);
		TextMessage message = TextMessage.parseFrom(bytes);
		System.out.println("Message from " + message.getUsername()
				+ " received: " + message.getTextMessage());
		_server.sendChatMessage(message);
	}

	public void sendMessage(TextMessage textMessage) {
		System.out.println("sending chat message to client.");
		byte size = (byte) textMessage.getSerializedSize();
		try {
			_outputStream.write(size);
			_outputStream.write(textMessage.toByteArray());
		} catch (IOException e) {
			System.err.println("Can't send message to client.");
		}
	}
}
