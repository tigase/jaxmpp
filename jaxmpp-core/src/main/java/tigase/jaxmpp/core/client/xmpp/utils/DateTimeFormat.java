package tigase.jaxmpp.core.client.xmpp.utils;

import java.util.Date;

public class DateTimeFormat {

	public static interface DateTimeFormatProvider {
		String format(Date date);

		Date parse(String s);
	}

	private static DateTimeFormatProvider provider;

	public static void setProvider(DateTimeFormatProvider provider) {
		DateTimeFormat.provider = provider;
	}

	public String format(Date date) {
		return provider.format(date);
	}

	public Date parse(String s) {
		return provider.parse(s);
	}
}
