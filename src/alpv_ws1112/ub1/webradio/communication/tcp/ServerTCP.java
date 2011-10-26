package alpv_ws1112.ub1.webradio.communication.tcp;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.sound.sampled.UnsupportedAudioFileException;

import alpv_ws1112.ub1.webradio.communication.Server;

public class ServerTCP implements Server {

	private ServerSocket server;

	// private Socket[] clients;

	public ServerTCP(int port) throws IOException {
		server = new ServerSocket(port);
		
		System.out.println("Starting server using port \"" + port + "\".");
	}

	@Override
	public void close() {
		try {
			// for (Socket client : clients) {
			// client.close();
			// }
			server.close();

		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

	}

	@Override
	public void playSong(String path) throws MalformedURLException,
			UnsupportedAudioFileException, IOException {
		// TODO Auto-generated method stub

	}

	public void writeHelloWorld(Socket client) throws IOException {

		OutputStreamWriter outputStream = new OutputStreamWriter(
				client.getOutputStream());

		PrintWriter printWriter = new PrintWriter(outputStream);
		printWriter.println("Hello World");
		printWriter.flush();
		outputStream.close();
		printWriter.close();

	}

	@Override
	public void run() {

		System.out.println("Server started.");

		while (true) {
			Socket client = null;
			try {
				client = server.accept();
				this.writeHelloWorld(client);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (client != null)
					try {
						client.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}

	}

}
