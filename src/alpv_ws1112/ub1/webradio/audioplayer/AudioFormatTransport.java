package alpv_ws1112.ub1.webradio.audioplayer;

import java.io.Serializable;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

/**
 * A wrapper for serializing an deserializing an AudioFormat object
 */
public class AudioFormatTransport implements Serializable {

	private int _channels, _frameSize, _sampleSizeInBits;
	private float _frameRate, _sampleRate;
	private boolean _isBigEndian;
	private Map<String, Object> _properties;
	private String _encoding;

	/**
	 * Store an AudioFormat
	 * 
	 * @param audioFormat
	 */
	public AudioFormatTransport(AudioFormat audioFormat) {
		_channels = audioFormat.getChannels();
		_encoding = audioFormat.getEncoding().toString();
		_frameRate = audioFormat.getFrameRate();
		_frameSize = audioFormat.getFrameSize();
		_sampleRate = audioFormat.getSampleRate();
		_sampleSizeInBits = audioFormat.getSampleSizeInBits();
		_isBigEndian = audioFormat.isBigEndian();
		_properties = audioFormat.properties();
	}

	/**
	 * Get stored AudioFormat
	 * 
	 * @return
	 */
	public AudioFormat getAudioFormat() {

		Encoding encoding = new Encoding(_encoding);

		return new AudioFormat(encoding, _sampleRate, _sampleSizeInBits,
				_channels, _frameSize, _frameRate, _isBigEndian, _properties);
	}

	private static final long serialVersionUID = -1999629759312603648L;
}
