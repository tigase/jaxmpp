/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EscapeUtilsTest {

	@Test
	public void testEscape() {
		assertEquals("&lt;a&gt;&quot;&amp;&lt;a&gt;", EscapeUtils.escape("<a>\"&<a>"));
		assertEquals("&lt;a&gt;", EscapeUtils.escape("<a>"));
		assertEquals("&lt;a b=&quot;x'x&quot;&gt;", EscapeUtils.escape("<a b=\"x'x\">"));
	}

	@Test
	public void testUnescape() {
		assertEquals("<a>", EscapeUtils.unescape("&lt;a&gt;"));
		assertEquals("<a b=\"x'x\">", EscapeUtils.unescape("&lt;a b=&quot;x'x&quot;&gt;"));
		assertEquals("<a>\"&<a>", EscapeUtils.unescape("&lt;a&gt;&quot;&amp;&lt;a&gt;"));
	}

}