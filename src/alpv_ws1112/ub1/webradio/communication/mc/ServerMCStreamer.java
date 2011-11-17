package alpv_ws1112.ub1.webradio.communication.mc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.protobuf.ByteString;

import alpv_ws1112.ub1.webradio.audioplayer.AudioFormatTransport;
import alpv_ws1112.ub1.webradio.audioplayer.AudioPlayer;
import alpv_ws1112.ub1.webradio.communication.ByteArray;
import alpv_ws1112.ub1.webradio.protobuf.Messages.Message;

public class ServerMCStreamer implements Runnable {

	private static final String NETWORK_GROUP = "230.0.0.1";
	private static final int DELAY = 100;

	private AudioInputStream _ais;
	private AudioFormat _audioFormat;
	private String _path;
	private InetAddress _networkGroup;
	private int _networkGroupPort;
	private MulticastSocket _socket;
	private boolean _close = false;

	public ServerMCStreamer(String path, int networkGroupPort)
			throws MalformedURLException, UnsupportedAudioFileException,
			IOException {
		changePath(path);
		_networkGroup = InetAddress.getByName(NETWORK_GROUP);
		_networkGroupPort = networkGroupPort;
		_socket = new MulticastSocket();
	}

	@Override
	public void run() {
		byte[] audioBuffer;
		while (!_close) {
			// Read audio data, if available
			if (_ais != null) {
				audioBuffer = new byte[getAudioBufferSize()];
				try {
					if (_ais.read(audioBuffer) <= 0) {
						_ais = AudioPlayer.getAudioInputStream(_path);
						_ais.read(audioBuffer);
					}
				} catch (IOException e) {
					System.err.println("IO-Error while reading the file.");
					close();
				} catch (UnsupportedAudioFileException e) {
					System.err.println("Unsupported file type.");
					close();
				}
			} else {
				audioBuffer = null;
			}

			// Send data
			if (audioBuffer != null) {
				Message.Builder builder = Message.newBuilder();
				builder.setData(ByteString.copyFrom(audioBuffer));

				try {
					sendPackage(builder);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
			}
		}

		_socket.close();

	}

	public void changePath(String path) throws MalformedURLException,
			UnsupportedAudioFileException, IOException {
		_path = path;
		_ais = AudioPlayer.getAudioInputStream(_path);
		_audioFormat = _ais.getFormat();
		// Not first start
		if (_socket != null) {
			// Transmitte audio format
			AudioFormatTransport aft = new AudioFormatTransport(_audioFormat);
			byte[] format = ByteArray.toBytes(aft);
			Message.Builder builder = Message.newBuilder();
			builder.setIsAudioFormat(true);
			builder.setData(ByteString.copyFrom(format));
			builder.setBufferSize(getBufferSize());

			sendPackage(builder);
		}
	}

	private void sendPackage(Message.Builder builder) throws IOException {
		Message message = builder.build();
		byte[] bytes = message.toByteArray();

		DatagramPacket packet = new DatagramPacket(bytes, bytes.length,
				_networkGroup, _networkGroupPort);

		_socket.send(packet);
	}

	public AudioFormat getAudioFormat() {
		return _audioFormat;
	}

	public String getNetworkGroup() {
		return _networkGroup.getHostName();
	}

	public int getNetworkGroupPort() {
		return _networkGroupPort;
	}

	/**
	 * Returns send buffer size
	 */
	public int getBufferSize() {
		return getAudioBufferSize() + 1000;
	}

	/**
	 * Returns audio buffer sizr
	 */
	private int getAudioBufferSize() {
		if (_audioFormat == null) {
			return 0;
		} else {
			return (int) (_audioFormat.getFrameSize()
					* _audioFormat.getFrameRate() / (1000 / DELAY));
		}
	}

	public void close() {
		_close = true;
	}

}
