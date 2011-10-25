package alpv_ws1112.ub1.webradio.protobuf;

import alpv_ws1112.ub1.webradio.protobuf.PacketProtos.TestMessage;

import com.google.protobuf.ByteString;

/**
 * Simple example to illustrate the usage of {@link com.google.protobuf}
 * 
 * @author juauer
 */
public class ProtoBufExample {
	/**
	 * Build a message with protobuf. The resulting message can be transformed
	 * to an array of bytes or written directly to an output stream. It than can
	 * easily be parsed by the remote host.
	 * 
	 * @param id
	 * @param data
	 * @param message
	 * @return
	 */
	public static TestMessage buildTestMessage(int id, byte[] data, String message) {
		TestMessage.Builder builder = TestMessage.newBuilder();
		builder.setId(id);
		builder.setData(ByteString.copyFrom(data));

		if(message != null)
			builder.setMessage(message);

		TestMessage testMessage = builder.build();
		assert (testMessage.isInitialized());
		return testMessage;
	}
}
