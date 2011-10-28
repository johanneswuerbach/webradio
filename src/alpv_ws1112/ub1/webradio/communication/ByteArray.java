package alpv_ws1112.ub1.webradio.communication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Object <-> byte[]
 */
public class ByteArray {

	/**
	 * Converts an object into an array of bytes
	 * 
	 * @param object
	 * @return - byte representation
	 */
	public static byte[] toBytes(Object object) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(object);

		return baos.toByteArray();
	}

	/**
	 * Converts and array of bytes back to an object
	 * 
	 * @param bytes
	 * @return - object
	 */
	public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
		Object object = new ObjectInputStream(
					new ByteArrayInputStream(bytes)).readObject();
		return object;
	}
}
