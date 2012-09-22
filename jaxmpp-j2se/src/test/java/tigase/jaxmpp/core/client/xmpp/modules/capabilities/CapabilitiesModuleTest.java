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
package tigase.jaxmpp.core.client.xmpp.modules.capabilities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CapabilitiesModuleTest {

	@Test
	public void testGenerateVerificationString() {
		assertEquals(
				"QgayPKawpkPSDYmwT/WM94uAlu0=",
				CapabilitiesModule.generateVerificationString(new String[] { "client/pc//Exodus 0.9.1" }, new String[] {
						"http://jabber.org/protocol/caps", "http://jabber.org/protocol/disco#info",
						"http://jabber.org/protocol/disco#items", "http://jabber.org/protocol/muc" }));
	}

}