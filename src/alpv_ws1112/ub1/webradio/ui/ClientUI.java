package alpv_ws1112.ub1.webradio.ui;

/**
 * UI to input commands into and displaying incoming messages.
 * 
 * @author juauer
 */
public interface ClientUI extends Runnable {
	/**
	 * A call by an {@link alpv_ws1112.ub1.webradio.communication.Client} to
	 * this should return the current user-name associated with this client. The
	 * user-name can be merged into an outgoing message, then.
	 * 
	 * @return The user-name associated with this client.
	 */
	public String getUserName();

	/**
	 * Display a recently received chat-message.
	 * 
	 * @param message A chat-message.
	 */
	public void pushChatMessage(String message);
}
