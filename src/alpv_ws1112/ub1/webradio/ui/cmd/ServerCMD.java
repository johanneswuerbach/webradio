package alpv_ws1112.ub1.webradio.ui.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.sound.sampled.UnsupportedAudioFileException;

import alpv_ws1112.ub1.webradio.communication.Server;
import alpv_ws1112.ub1.webradio.ui.ServerUI;

/**
 * Command-line interface for controlling the server
 */
public class ServerCMD implements ServerUI {

	private Server _server;
	private Thread _serverThread;

	public ServerCMD(Server server, Thread serverThread) {
		_server = server;
		_serverThread = serverThread;
	}

	/**
	 * Run the ui
	 */
	public void run() {
		System.out.print("Welcome to webradio server.\n"
				+ "Possible commands:\n" + "M: <path> - to play a music file\n"
				+ "QUIT - shutdown the server\n");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		try {
			while (_serverThread.isAlive()) {
				String line = br.readLine();
				if (line.equals("QUIT")) {
					break;
				} else if (line.startsWith("M: ")) {
					String path = line.substring(3);
					try {
						_server.playSong(path);
					} catch (UnsupportedAudioFileException e) {
						e.printStackTrace();
					}
				}
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Can't read cmd input.");
		}
		_server.close();
		System.out.println("Shutdown the server.");
		System.out.println("Bye.");
	}
}
