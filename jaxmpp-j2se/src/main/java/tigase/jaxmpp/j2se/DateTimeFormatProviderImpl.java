package tigase.jaxmpp.j2se;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat.DateTimeFormatProvider;

public class DateTimeFormatProviderImpl implements DateTimeFormatProvider {

	private final SimpleDateFormat d1;

	public DateTimeFormatProviderImpl() {
		d1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		d1.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	@Override
	public String format(Date date) {
		Date d = new Date(date.getTime());
		return d1.format(d);
	}

	@Override
	public Date parse(String t) {
		try {
			return d1.parse(t);
		} catch (Exception e) {
			return null;
		}
	}

}
