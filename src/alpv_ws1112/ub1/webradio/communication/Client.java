package alpv_ws1112.ub1.webradio.communication;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * A client that handles incoming and outgoing communication. Different
 * implementations may provide support for different communication-protocols.
 * 
 * @author juauer
 */
public interface Client extends Runnable {
	/**
	 * Connect to the given server.
	 * 
	 * @param serverAddress The {@link java.net.InetSocketAddress} of the
	 *            server.
	 * @throws IOException
	 */
	public void connect(InetSocketAddress serverAddress) throws IOException;

	/**
	 * Close this client and free any resources associated with it.
	 */
	public void close();

	/**
	 * Send a chat message. Adding this clients user-name to the message as well
	 * as packing it may be implementation-dependent.
	 * 
	 * @param message A message to send.
	 * @throws IOException
	 */
	public void sendChatMessage(String message) throws IOException;
}
