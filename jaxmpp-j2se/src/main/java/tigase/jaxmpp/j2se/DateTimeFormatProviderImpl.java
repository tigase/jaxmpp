package tigase.jaxmpp.j2se;

import java.text.SimpleDateFormat;
import java.util.Date;

import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat.DateTimeFormatProvider;

public class DateTimeFormatProviderImpl implements DateTimeFormatProvider {

	private final SimpleDateFormat d1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private final SimpleDateFormat d2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	@Override
	public String format(Date date) {
		return d1.format(date);
	}

	@Override
	public Date parse(String t) {
		try {
			return d1.parse(t);
		} catch (Exception e) {
			try {
				return d2.parse(t);
			} catch (Exception e1) {
				return null;
			}
		}
	}

}
