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
package tigase.jaxmpp.core.client;

import junit.framework.TestCase;

public class JIDTest extends TestCase {

	public void testGetBareJid() {
		JID jid = JID.jidInstance("a@b/c");
		assertEquals(BareJID.bareJIDInstance("a", "b"), jid.getBareJid());
	}

	public void testGetDomain() {
		JID jid = JID.jidInstance("a@b/c");
		assertEquals("b", jid.getDomain());
	}

	public void testGetLocalpart() {
		JID jid = JID.jidInstance("a@b/c");
		assertEquals("a", jid.getLocalpart());
	}

	public void testGetResource() {
		JID jid = JID.jidInstance("a@b/c");
		assertEquals("c", jid.getResource());
	}

	public void testJIDInstance() {
		assertEquals(JID.jidInstance("a@b"), JID.jidInstance("a@b"));
		assertEquals(JID.jidInstance("a@b"), JID.jidInstance("a", "b"));
		assertEquals(JID.jidInstance("a", "b"), JID.jidInstance("a", "b"));
		assertEquals(JID.jidInstance("a@b/c"), JID.jidInstance("a@b/c"));
		assertEquals(JID.jidInstance("a", "b", "c"), JID.jidInstance("a@b/c"));

		assertEquals(JID.jidInstance("a@b/C"), JID.jidInstance("A@B/C"));
		assertTrue(JID.jidInstance("A@b/c").equals(JID.jidInstance("a@b/c")));
		assertFalse(JID.jidInstance("a@b/C").equals(JID.jidInstance("a@b/c")));
		assertEquals(JID.jidInstance("a@b/C").hashCode(), JID.jidInstance("A@B/C").hashCode());

		assertFalse(JID.jidInstance("a@b").equals(JID.jidInstance("a@b/c")));
		assertFalse(JID.jidInstance("a@b/C").hashCode() == JID.jidInstance("a@b/c").hashCode());
	}

	public void testPercentJids() {
		JID jid = JID.jidInstance("-101100311719181%chat.facebook.com@domain.com");

		assertEquals("domain.com", jid.getDomain());
		assertEquals("-101100311719181%chat.facebook.com", jid.getLocalpart());
		assertNull(jid.getResource());
		assertEquals("-101100311719181%chat.facebook.com@domain.com", jid.toString());
	}

	public void testToString() {
		JID jid = JID.jidInstance("a@b");
		assertEquals("a@b", jid.toString());

		jid = JID.jidInstance("a@b/c");
		assertEquals("a@b/c", jid.toString());

		jid = JID.jidInstance("A@B/C");
		assertEquals("A@b/C", jid.toString());
	}

}