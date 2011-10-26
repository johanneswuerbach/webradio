package alpv_ws1112.ub1.webradio.communication.tcp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.sound.sampled.UnsupportedAudioFileException;

public class Server implements alpv_ws1112.ub1.webradio.communication.Server {

	final private int port;

	public Server(int port) throws IOException {
		this.port = port;
		open();
	}

	private void open() throws IOException {
		ServerSocket serverSocket = new ServerSocket(port);
		Socket clientSocket = serverSocket.accept();
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

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
