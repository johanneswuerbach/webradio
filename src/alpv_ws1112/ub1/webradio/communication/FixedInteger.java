package alpv_ws1112.ub1.webradio.communication;

public class FixedInteger {

	/**
	 * Convert four bytes to an integer
	 * 
	 * @param bytes
	 * @return
	 */
	public static int toInt(byte[] bytes) {
		int number = 0;
		for (int i = 0; i < 4; ++i) {
			number |= (bytes[3 - i] & 0xff) << (i << 3);
		}
		return number;
	}

	/**
	 * Convert an integer to four bytes
	 * 
	 * @param number
	 * @return
	 */
	public static byte[] toBytes(int number) {
		byte[] bytes = new byte[4];
		for (int i = 0; i < 4; ++i) {
			int shift = i << 3; // i * 8
			bytes[3 - i] = (byte) ((number & (0xff << shift)) >>> shift);
		}
		return bytes;
	}
}
