/*
 * JaxmppTest.java
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

package tigase.jaxmpp.gwt.client;

import com.google.gwt.junit.client.GWTTestCase;

public class JaxmppTest
		extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "tigase.jaxmpp.gwt.JaxmppGWTJUnit";
	}

	public void test() {
		try {
			Jaxmpp j = new Jaxmpp();
		} catch (Throwable e) {
			//Assert.fail(e.getMessage());
		}

	}

}
