package alpv_ws1112.ub1.webradio.communication.protobuf;

import java.io.IOException;
import java.io.InputStream;

import alpv_ws1112.ub1.webradio.protobuf.Messages.TextMessage;

/**
 * Handeling all clients and receive their chat messages.
 */
public class ServerProtoBufChatWorker implements Runnable {

	private boolean _close = false;
	private InputStream _inputStream;

	public ServerProtoBufChatWorker(ServerProtoBuf server,
			InputStream inputStream) throws IOException {
		_inputStream = inputStream;
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
				System.err.println("Can't receive message.");
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
		System.out.println("Message received: " + message.getTextMessage());
	}
}
