package alpv_ws1112.ub1.webradio.communication.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import alpv_ws1112.ub1.webradio.protobuf.Messages.WebradioMessage;

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
				receiveWebradioMessage();
			} catch (IOException e) {
				System.err
						.println("Can't receive message. Closing client socket.");
				close();
			}
		}
		try {
			_inputStream.close();
			_outputStream.close();
			_server.removeClient(this);
		} catch (IOException e) {
			System.err.println("Can't send close client connection.");
		}
	}

	private void receiveWebradioMessage() throws IOException {
		int size = _inputStream.read();
		byte[] bytes = new byte[size];
		_inputStream.read(bytes);
		WebradioMessage message = WebradioMessage.parseFrom(bytes);
		if (message.getIsChatMessage()) {
			System.out.println("Message from " + message.getUsername()
					+ " received: " + message.getTextMessage());
			_server.sendChatMessage(message);
		} else {
			System.err.println("not a text message");
		}
	}

	public void sendMessage(WebradioMessage textMessage) {
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
