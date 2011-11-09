package alpv_ws1112.ub1.webradio.ui.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import alpv_ws1112.ub1.webradio.communication.Client;
import alpv_ws1112.ub1.webradio.ui.ClientUI;

/**
 * Command-line interface for controlling the client
 */
public class ClientCMD implements ClientUI {

	private String _username;
	private Client _client;
	private Thread _clientThread;

	public ClientCMD(Client client, Thread clientThread, String username) {
		_username = username;
		_client = client;
		_clientThread = clientThread;
	}

	/**
	 * Run the ui
	 */
	public void run() {

		System.out.print("Welcome to webradio client.\n"
				+ "Possible commands:\n"
				+ "N: <new_name> - to change your nickname\n"
				+ "QUIT - quite the client\n");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			while (_clientThread.isAlive()) {

				String line = br.readLine();

				// Change nickname
				if (line.startsWith("N: ")) {
					_username = line.substring(3);
				} else if (line.startsWith("M: ")) {
					_client.sendChatMessage(line.substring(3));
				} else if (line.equals("QUIT")) {
					break;
				}
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		_client.close();
		System.out.println("Shutdown the client.");
		System.out.println("Bye.");
	}

	public String getUserName() {
		return _username;
	}

	@Override
	public void pushChatMessage(String message) {
		System.out.println(message);
	}

}
