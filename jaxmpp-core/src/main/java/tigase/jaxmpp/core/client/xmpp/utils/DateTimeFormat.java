/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
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
package tigase.jaxmpp.core.client.xmpp.utils;

import java.util.Date;

/**
 * Date time formatter, implements <a
 * href='http://xmpp.org/extensions/xep-0082.html'>XMPP Date and Time
 * Profiles</a>.
 */
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