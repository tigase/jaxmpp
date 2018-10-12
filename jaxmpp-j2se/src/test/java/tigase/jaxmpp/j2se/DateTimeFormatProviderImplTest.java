/*
 * DateTimeFormatProviderImplTest.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.jaxmpp.j2se;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeFormatProviderImplTest
		extends TestCase {

	private final DateTimeFormatProviderImpl dtf = new DateTimeFormatProviderImpl();

	public void testFormat() {
		String t = "2002-09-10T23:41:07.000Z";
		final Date d = dtf.parse(t);
		Assert.assertEquals(t, dtf.format(d));

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.setTime(d);

		Assert.assertEquals(2002, c.get(Calendar.YEAR));
		Assert.assertEquals(8, c.get(Calendar.MONTH));
		Assert.assertEquals(10, c.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(41, c.get(Calendar.MINUTE));
		Assert.assertEquals(7, c.get(Calendar.SECOND));
		Assert.assertEquals(0, c.get(Calendar.MILLISECOND));

	}

	public void testFormatWithMilis() {
		String t = "2017-12-27T07:56:26.453Z";
		final Date d = dtf.parse(t);
		Assert.assertEquals(t, dtf.format(d));
	}

	public void testParse() {
		Date d = new Date();
		Assert.assertEquals(d.toString(), dtf.parse(dtf.format(d)).toString());

		d = dtf.parse("2017-12-27T07:56:26.453+01:00");
		Assert.assertNotNull("It should not be null", d);

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.setTime(d);

		Assert.assertEquals(2017, c.get(Calendar.YEAR));
		Assert.assertEquals(11, c.get(Calendar.MONTH));
		Assert.assertEquals(27, c.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(6, c.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(56, c.get(Calendar.MINUTE));
		Assert.assertEquals(26, c.get(Calendar.SECOND));
		Assert.assertEquals(453, c.get(Calendar.MILLISECOND));
	}

	public void testParseWithMillisOnFourPositions() {
		Date d = new Date();
		Assert.assertEquals(d.toString(), dtf.parse(dtf.format(d)).toString());

		d = dtf.parse("2017-12-27T07:56:26.4531+01:00");
		Assert.assertNotNull("It should not be null", d);

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.setTime(d);

		Assert.assertEquals(2017, c.get(Calendar.YEAR));
		Assert.assertEquals(11, c.get(Calendar.MONTH));
		Assert.assertEquals(27, c.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(6, c.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(56, c.get(Calendar.MINUTE));
		Assert.assertEquals(26, c.get(Calendar.SECOND));
		Assert.assertEquals(453, c.get(Calendar.MILLISECOND));
	}

	public void testParseWithMillisOnOnePosition() {
		Date d = new Date();
		Assert.assertEquals(d.toString(), dtf.parse(dtf.format(d)).toString());

		d = dtf.parse("2017-12-27T07:56:26.4+01:00");
		Assert.assertNotNull("It should not be null", d);

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.setTime(d);

		Assert.assertEquals(2017, c.get(Calendar.YEAR));
		Assert.assertEquals(11, c.get(Calendar.MONTH));
		Assert.assertEquals(27, c.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(6, c.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(56, c.get(Calendar.MINUTE));
		Assert.assertEquals(26, c.get(Calendar.SECOND));
		Assert.assertEquals(400, c.get(Calendar.MILLISECOND));
	}

	public void testParseWithMillisOnTwoPositions() {
		Date d = new Date();
		Assert.assertEquals(d.toString(), dtf.parse(dtf.format(d)).toString());

		d = dtf.parse("2017-12-27T07:56:26.45+01:00");
		Assert.assertNotNull("It should not be null", d);

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.setTime(d);

		Assert.assertEquals(2017, c.get(Calendar.YEAR));
		Assert.assertEquals(11, c.get(Calendar.MONTH));
		Assert.assertEquals(27, c.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(6, c.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(56, c.get(Calendar.MINUTE));
		Assert.assertEquals(26, c.get(Calendar.SECOND));
		Assert.assertEquals(450, c.get(Calendar.MILLISECOND));
	}

	public void testParseWithoutMillis() {
		Date d = new Date();
		Assert.assertEquals(d.toString(), dtf.parse(dtf.format(d)).toString());

		d = dtf.parse("2017-12-27T07:56:26+01:00");
		Assert.assertNotNull("It should not be null", d);

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.setTime(d);

		Assert.assertEquals(2017, c.get(Calendar.YEAR));
		Assert.assertEquals(11, c.get(Calendar.MONTH));
		Assert.assertEquals(27, c.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(6, c.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(56, c.get(Calendar.MINUTE));
		Assert.assertEquals(26, c.get(Calendar.SECOND));
	}

}
