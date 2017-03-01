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

import java.util.Date;

public class DateTimeFormatProviderImplTest
		extends TestCase {

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