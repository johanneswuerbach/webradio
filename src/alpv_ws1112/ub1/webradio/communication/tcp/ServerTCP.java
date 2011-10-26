package alpv_ws1112.ub1.webradio.communication.tcp;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.sound.sampled.UnsupportedAudioFileException;

public class ServerTCP implements alpv_ws1112.ub1.webradio.communication.Server {

	final private int port;
	private ServerSocket serverSocket;
	private Socket clientSocket;

	public ServerTCP(int port) throws IOException {
		this.port = port;
		open();
	}

	private void open() throws IOException {
		serverSocket = new ServerSocket(port);
		clientSocket = serverSocket.accept();
	}

	@Override
	public void close() {
		try {
			clientSocket.close();
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

	public void writeHelloWorld() throws IOException {
		OutputStreamWriter outputStream = new OutputStreamWriter(
				clientSocket.getOutputStream());
		PrintWriter printWriter = new PrintWriter(outputStream);
		printWriter.print("Hello World");
		printWriter.flush();
		outputStream.close();
		printWriter.close();

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
