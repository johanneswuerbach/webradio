package alpv_ws1112.ub1.webradio.communication.tcp;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.sound.sampled.UnsupportedAudioFileException;

public class Server implements alpv_ws1112.ub1.webradio.communication.Server {

	final private int port;
	private ServerSocket serverSocket;
	private Socket clientSocket;

	public Server(int port) throws IOException {
		this.port = port;
		open();
	}

	private void open() throws IOException {
		serverSocket = new ServerSocket(port);
		clientSocket = serverSocket.accept();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void playSong(String path) throws MalformedURLException,
			UnsupportedAudioFileException, IOException {
		// TODO Auto-generated method stub

	}

	public void writeHelloWorld() throws IOException {
		PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(
				clientSocket.getOutputStream()));
		printWriter.print("Hello World");
		printWriter.flush();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
