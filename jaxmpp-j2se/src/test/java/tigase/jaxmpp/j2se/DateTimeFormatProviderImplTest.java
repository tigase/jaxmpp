package tigase.jaxmpp.j2se;

import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;

public class DateTimeFormatProviderImplTest extends TestCase {

	private final DateTimeFormatProviderImpl dtf = new DateTimeFormatProviderImpl();

	public void testFormat() {
		String t = "2002-09-10T23:41:07Z";
		Assert.assertEquals(t, dtf.format(dtf.parse(t)));
	}

	public void testParse() {
		Date d = new Date();
		Assert.assertEquals(d.toString(), dtf.parse(dtf.format(d)).toString());
	}

}
