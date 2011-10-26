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

	private ServerSocket serverSocket;

	// private Socket[] clients;

	public ServerTCP(int port) throws IOException {
		serverSocket = new ServerSocket(port);
	}

	@Override
	public void close() {
		try {
			// for (Socket client : clients) {
			// client.close();
			// }
			serverSocket.close();

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

		while (true) {
			try {
				Socket client = serverSocket.accept();
				this.writeHelloWorld(client);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
