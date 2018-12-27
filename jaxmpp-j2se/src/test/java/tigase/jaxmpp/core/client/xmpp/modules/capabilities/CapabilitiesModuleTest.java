/*
 * CapabilitiesModuleTest.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.core.client.xmpp.modules.capabilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CapabilitiesModuleTest {

	@Test
	public void testGenerateVerificationString() {
		CapabilitiesModule cm = new CapabilitiesModule();
		assertEquals("QgayPKawpkPSDYmwT/WM94uAlu0=",
					 cm.generateVerificationString(new String[]{"client/pc//Exodus 0.9.1"},
												   new String[]{"http://jabber.org/protocol/caps",
																"http://jabber.org/protocol/disco#info",
																"http://jabber.org/protocol/disco#items",
																"http://jabber.org/protocol/muc"}));
	}

}