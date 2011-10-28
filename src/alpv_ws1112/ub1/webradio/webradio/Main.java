package alpv_ws1112.ub1.webradio.webradio;

import java.io.IOException;
import java.net.InetSocketAddress;

import alpv_ws1112.ub1.webradio.communication.Client;
import alpv_ws1112.ub1.webradio.communication.Server;
import alpv_ws1112.ub1.webradio.communication.tcp.ClientTCP;
import alpv_ws1112.ub1.webradio.communication.tcp.ServerTCP;
import alpv_ws1112.ub1.webradio.ui.ClientUI;
import alpv_ws1112.ub1.webradio.ui.ServerUI;
import alpv_ws1112.ub1.webradio.ui.cmd.ClientCMD;
import alpv_ws1112.ub1.webradio.ui.cmd.ServerCMD;

public class Main {
	private static final String USAGE = String
			.format("usage: java -jar UB%%X_%%NAMEN [-options] server tcp|udp|mc PORT%n"
					+ "         (to start a server)%n"
					+ "or:    java -jar UB%%X_%%NAMEN [-options] client tcp|udp|mc SERVERIPADDRESS SERVERPORT USERNAME%n"
					+ "         (to start a client)");

	/**
	 * Starts a server/client according to the given arguments, using a GUI or
	 * just the command-line according to the given arguments.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			boolean useGUI = false;
			int i = -1;

			// Parse options. Add additional options here if you have to. Do not
			// forget to mention their usage in the help-string!
			while (args[++i].startsWith("-")) {
				if (args[i].equals("-help")) {
					System.out.println(USAGE
							+ String.format("%n%nwhere options include:"));
					System.out.println("  -help      Show this text.");
					System.out
							.println("  -gui       Show a graphical user interface.");
					System.exit(0);
				} else if (args[i].equals("-gui")) {
					useGUI = true;
				}
			}

			Server server = null;

			if (args[i].equals("server")) {
				String protocol = args[i + 1];
				int port = Integer.parseInt(args[i + 2]);
				if (protocol.equals("tcp")) {
					server = new ServerTCP(port);
				} else if (protocol.equals("udp")) {
					System.err.println("udp not supported.");
				} else if (protocol.equals("mc")) {
					System.err.println("mc not supported.");
				} else {
					System.err.println("protcol " + protocol
							+ " is not supported.");
				}

				// Start server
				Thread serverThread = new Thread(server);
				serverThread.start();

				// Run UI
				ServerUI serverUI = null;
				if (useGUI) {
					// TBD
				} else {
					serverUI = new ServerCMD(server);
				}
				Thread serverUIThread = new Thread(serverUI);
				serverUIThread.start();
				while (serverUIThread.isAlive())
					;

				// Shutdown server
				server.close();
				while (serverThread.isAlive())
					;

			} else if (args[i].equals("client")) {
				String protocol = args[i + 1];

				Client client = null;
				Thread clientThread = null;

				if (protocol.equals("tcp")) {
					client = new ClientTCP();
					String host = args[i + 2];
					int port = Integer.parseInt(args[i + 3]);
					client.connect(InetSocketAddress.createUnresolved(host,
							port));

				} else if (protocol.equals("udp")) {
					System.err.println("udp not supported.");
				} else if (protocol.equals("mc")) {
					System.err.println("mc not supported.");
				} else {
					System.err.println("protcol " + protocol
							+ " is not supported.");
				}

				// Create connection
				clientThread = new Thread(client);
				clientThread.start();

				// Run UI
				ClientUI clientUI = null;
				if (useGUI) {
					// TBD
				} else {
					clientUI = new ClientCMD(client, args[i + 3]);
				}
				Thread clientUIThread = new Thread(clientUI);
				clientUIThread.start();
				while (clientUIThread.isAlive())
					;

				// Shutdown client
				client.close();
				while (clientThread.isAlive())
					;

			} else {
				throw new IllegalArgumentException();
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println(USAGE);
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.err.println(USAGE);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.err.println(USAGE);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		System.out.println("Bye.");
	}
}
