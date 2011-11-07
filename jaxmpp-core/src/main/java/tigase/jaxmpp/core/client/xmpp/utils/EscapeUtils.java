package tigase.jaxmpp.core.client.xmpp.utils;

public class EscapeUtils {

	private static final String[][] ENTITIES = { { "&", "&amp;" }, { "<", "&lt;" }, { ">", "&gt;" }, { "\"", "&quot;" }, };

	public static String escape(String str) {
		if (str == null)
			return null;
		if (str.length() == 0)
			return str;
		for (int i = 0; i < ENTITIES.length; i++) {
			str = str.replace(ENTITIES[i][0], ENTITIES[i][1]);
		}
		return str;
	}

	public static String unescape(String str) {
		if (str == null)
			return null;
		if (str.length() == 0)
			return str;
		for (int i = ENTITIES.length - 1; i >= 0; i--) {
			str = str.replace(ENTITIES[i][1], ENTITIES[i][0]);
		}
		return str;
	}

	private EscapeUtils() {
	}
}
