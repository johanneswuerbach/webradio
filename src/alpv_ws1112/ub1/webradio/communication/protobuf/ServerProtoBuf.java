package alpv_ws1112.ub1.webradio.communication.protobuf;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import alpv_ws1112.ub1.webradio.communication.Server;
import alpv_ws1112.ub1.webradio.protobuf.Messages.WebradioMessage;

public class ServerProtoBuf implements Server {

	private ServerSocket _socket;
	private boolean _close = false;
	private List<ServerProtoBufWorker> _clients, _pendingClients; // Number of
																	// connected
																	// and
																	// pending
																	// clients

	private CyclicBarrier _barrier; // Buffer sending barrier
	private ServerProtoBufStreamer _streamer;
	private AtomicBoolean _currentlyResetingBarrier;
	private AtomicBoolean _currentlyMergingClients;
	private AudioFormat _audioFormat;

	public ServerProtoBuf(int port) throws IOException {
		// Create socket
		_socket = new ServerSocket(port);
		_socket.setSoTimeout(500); // Only 500ms timeout
		// Create streamer
		_streamer = new ServerProtoBufStreamer(this);
		// Create connection queues
		_clients = new ArrayList<ServerProtoBufWorker>();
		_pendingClients = new ArrayList<ServerProtoBufWorker>();
		_currentlyMergingClients = new AtomicBoolean();
		_currentlyResetingBarrier = new AtomicBoolean();

		System.out.println("Starting server using port \"" + port + "\".");
	}

	/**
	 * Set the close flag
	 */
	public void close() {
		_close = true;
	}

	/**
	 * Play a specific song
	 */
	public void playSong(String path) throws MalformedURLException,
			UnsupportedAudioFileException, IOException {

		System.out.println("Play song: " + path);

		try {
			// Initialize the stream
			_streamer.playSong(path);
		} catch (MalformedURLException e) {
			System.err.println("Can't find audio file.");
		} catch (UnsupportedAudioFileException e) {
			System.err.println("Unsupported audio file.");
		} catch (IOException e) {
			System.err.println("Can't open audio file.");
		}
	}

	/**
	 * Start client
	 */
	public void startClient(ServerProtoBufWorker client) {
		// Send audioFormat, if music is playing
		if(_audioFormat != null) {
			client.setAudioFormat(_audioFormat);
			client.play();
		}
		Thread thread = new Thread(client);
		thread.start();
	}

	/**
	 * Handles client server socket connections
	 */
	public void run() {

		System.out.println("Server started.");
		
		while (!_close) {
			try {
				Socket client = _socket.accept();
				ServerProtoBufWorker worker = new ServerProtoBufWorker(this,
						client);
				addClient(worker);
			} catch (SocketTimeoutException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {

			// Close connected clients
			for (ServerProtoBufWorker client : _clients) {
				client.close();
			}

			// Close pending clients
			for (ServerProtoBufWorker pendingClient : _pendingClients) {
				pendingClient.close();
			}

			// Close streamer
			if (_streamer != null) {
				_streamer.close();
			}

			// Close socket
			_socket.close();

		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		System.out.println("Server closed.");
	}

	/**
	 * Add a new client to the pending queue
	 * 
	 * @param worker
	 */
	private void addClient(ServerProtoBufWorker worker) {
		while (_currentlyMergingClients.get())
			;

		_pendingClients.add(worker);
		System.out.println("New pending client.");

		// Music started and no clients connected -> start playing immediately
		if (_clients.size() == 0) {
			resetBarrier();
		}
	}

	/**
	 * Returns the current audio format
	 */
	public AudioFormat getAudioFormat() {
		return _streamer.getAudioFormat();
	}

	/**
	 * Returns the current music buffer
	 * 
	 * @return
	 */
	public byte[] getBuffer() {
		return _streamer.getBuffer();
	}

	/**
	 * Refresh the current barrier and merge pending and connected clients
	 */
	public void resetBarrier() {
		// Set locks (reset barrier and merge clients)
		_currentlyResetingBarrier.set(true);
		_currentlyMergingClients.set(true);
		// Add pending workers to connected worker queue and start them if radio
		// is already playing
		for (ServerProtoBufWorker pendingClient : _pendingClients) {
			_clients.add(pendingClient);
			startClient(pendingClient);
			System.out.println("Client added.");
		}
		// Remove added workers from pending queue
		_pendingClients.clear();
		// Remove merging lock
		_currentlyMergingClients.set(false);
		// Create a new barrier
		int size = _clients.size();
		if (size > 0) {
			_barrier = new CyclicBarrier(size, _streamer);
		}
		// Remove reseting lock
		_currentlyResetingBarrier.set(false);
	}

	/**
	 * Wait at the current barrier
	 */
	public void awaitBarrier() {
		if (_barrier != null) {
			// Loop during reset
			while (_currentlyResetingBarrier.get())
				;
			try {
				_barrier.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Remove disconnected clients
	 * 
	 * @param worker
	 */
	public void removeClient(ServerProtoBufWorker worker) {
		// Remove from both queues
		_pendingClients.remove(worker);
		_clients.remove(worker);
	}

	/**
	 * Send chat message to all connected clients
	 * 
	 * @param message
	 * @param source
	 */
	public void sendChatMessage(WebradioMessage message,
			ServerProtoBufWorker source) {
		for (ServerProtoBufWorker client : _clients) {
			if (!client.equals(source)) {
				client.queueMessage(message);
			}
		}
	}

	/**
	 * Publish new audio format to all clients
	 */
	public void newAudioFormat(AudioFormat audioFormat) {
		System.out.println("Broadcast new audio format.");
		_audioFormat = audioFormat;
		for (ServerProtoBufWorker client : _clients) {
			client.setAudioFormat(audioFormat);
			client.play();
		}
	}
}