package tigase.jaxmpp.core.client;

public class Hex {

	public static byte[] decode(final String hex) {
		int l = hex.length();
		byte[] data = new byte[l / 2];
		for (int i = 0; i < l; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
		}
		return data;
	}

	public static String encode(final byte[] buffer, final int offset) {
		final StringBuilder sb = new StringBuilder();
		for (int i = offset; i < buffer.length; i++) {
			sb.append(Integer.toString((buffer[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	public static String encode(final byte[] buffer) {
		return encode(buffer, 0);
	}

	public static String format(String hex, final int groupSize) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < hex.length(); ) {
			int d = i + groupSize;
			if (d >= hex.length()) {
				d = hex.length();
			}
			sb.append(hex.substring(i, d));
			i += groupSize;
			if (i < hex.length()) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}

}
