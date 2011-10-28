package alpv_ws1112.ub1.webradio.communication.tcp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import alpv_ws1112.ub1.webradio.audioplayer.AudioPlayer;
import alpv_ws1112.ub1.webradio.communication.Server;

public class ServerTCP implements Server {

	private ServerSocket _socket;
	private boolean _closed;
	private ArrayList<ServerTCPWorker> _clients;
	private boolean _play = false;
	private AudioFormat _audioFormat;
	
	private static final int BUFFER_SIZE = 1024;

	// private Socket[] clients;

	public ServerTCP(int port) throws IOException {
		_socket = new ServerSocket(port);
		// Wait only 500ms for new connections
		_socket.setSoTimeout(500);

		System.out.println("Starting server using port \"" + port + "\".");
	}

	/**
	 * Set the close flag
	 */
	public void close() {
		_closed = true;
	}

	@Override
	public void playSong(String path) throws MalformedURLException,
			UnsupportedAudioFileException, IOException {

		System.out.println("Play song: " + path);

		try {
			AudioInputStream ais = AudioPlayer.getAudioInputStream(path);
			_audioFormat = (AudioFormat) ais.getFormat();
			byte[] musicBuffer = new byte[BUFFER_SIZE];
			
			_play = true;
			
			for(ServerTCPWorker client : _clients) {
				sendMusic(client);
			}

			while (!_closed) {
				while (ais.read(musicBuffer) > 0) {
					for(ServerTCPWorker client : _clients) {
						client.sendBuffer(musicBuffer);
					}
				}
				ais = AudioPlayer.getAudioInputStream(path);
			}

			ais.close();
		} catch (MalformedURLException e) {
			System.err.println("Can't find audio file.");
		} catch (UnsupportedAudioFileException e) {
			System.err.println("Unsupported audio file.");
		} catch (IOException e) {
			System.err.println("Can't open audio file.");
		}
	}

	public void sendMusic(ServerTCPWorker worker) {
		worker.setAudioFormat(_audioFormat);
		worker.play();
	}

	@Override
	public void run() {

		System.out.println("Server started.");
		_clients = new ArrayList<ServerTCPWorker>();

		while (!_closed) {
			try {
				Socket client = _socket.accept();

				ServerTCPWorker worker = new ServerTCPWorker(
						client.getOutputStream());
				
				
				Thread thread = new Thread(worker);
				thread.start();
				
				if(_play) {
					sendMusic(worker);
				}
				_clients.add(worker);
				
			} catch (SocketTimeoutException e) {

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			
			for (ServerTCPWorker client : _clients) {
				client.close();
			}
			
			_socket.close();

		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

	}
}
