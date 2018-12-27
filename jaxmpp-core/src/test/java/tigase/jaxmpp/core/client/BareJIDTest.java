/*
 * BareJIDTest.java
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
package tigase.jaxmpp.core.client;

import junit.framework.TestCase;

public class BareJIDTest
		extends TestCase {

	public void testBareJIDInstance() {
		assertEquals(BareJID.bareJIDInstance("a@b"), BareJID.bareJIDInstance("a@b"));
		assertEquals(BareJID.bareJIDInstance("a@b"), BareJID.bareJIDInstance("a", "b"));
		assertEquals(BareJID.bareJIDInstance("a", "b"), BareJID.bareJIDInstance("a", "b"));
		assertEquals(BareJID.bareJIDInstance("a@b"), BareJID.bareJIDInstance("a@b/c"));
	}

	public void testGetDomain() {
		BareJID jid = BareJID.bareJIDInstance("a@b");
		assertEquals("b", jid.getDomain());
	}

	public void testGetLocalpart() {
		BareJID jid = BareJID.bareJIDInstance("a@b");
		assertEquals("a", jid.getLocalpart());
	}

	public void testPercentJids() {
		BareJID jid = BareJID.bareJIDInstance("-101100311719181%chat.facebook.com@domain.com");

		assertEquals("domain.com", jid.getDomain());
		assertEquals("-101100311719181%chat.facebook.com", jid.getLocalpart());
		assertEquals("-101100311719181%chat.facebook.com@domain.com", jid.toString());
	}

	public void testToString() {
		BareJID jid = BareJID.bareJIDInstance("a@b");
		assertEquals("a@b", jid.toString());

		jid = BareJID.bareJIDInstance("a@b/c");
		assertEquals("a@b", jid.toString());
	}

}