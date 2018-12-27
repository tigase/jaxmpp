/*
 * PresenceModuleTest.java
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

package tigase.jaxmpp.core.client.xmpp.modules.presence;

import junit.framework.Assert;
import org.junit.Test;
import tigase.jaxmpp.core.client.*;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.ContactUnavailableHandler;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import java.util.HashSet;
import java.util.Set;

public class PresenceModuleTest
		extends AbstractJaxmppTest {

	private PresenceStore getPresence() {
		return PresenceModule.getPresenceStore(context.getSessionObject());
	}

	@Test
	public void test() {

		PresenceModule presenceModule = new PresenceModule();
		presenceModule.setContext(context);
		PresenceModule.setPresenceStore(context.getSessionObject(), new MockPresenceStore());

		final Set<JID> unavailableContacts = new HashSet<JID>();

		context.getEventBus()
				.addHandler(PresenceModule.ContactUnavailableHandler.ContactUnavailableEvent.class,
							new ContactUnavailableHandler() {

								@Override
								public void onContactUnavailable(SessionObject sessionObject, Presence stanza, JID jid,
																 String status) {
									unavailableContacts.add(jid);
								}
							});

		try {
			Presence p1 = Presence.create();
			p1.setFrom(JID.jidInstance("a@b.c/d"));
			presenceModule.process(p1);

			Assert.assertTrue("Contant should be available",
							  getPresence().isAvailable(BareJID.bareJIDInstance("a@b.c")));

			p1 = Presence.create();
			p1.setFrom(JID.jidInstance("a@b.c/d_second"));
			presenceModule.process(p1);

			Assert.assertTrue("Contant should be available",
							  getPresence().isAvailable(BareJID.bareJIDInstance("a@b.c")));

			p1 = Presence.create();
			p1.setFrom(JID.jidInstance("a@b.c/d"));
			p1.setType(StanzaType.unavailable);
			presenceModule.process(p1);

			Assert.assertTrue("Contant should be available",
							  getPresence().isAvailable(BareJID.bareJIDInstance("a@b.c")));

			System.out.println(unavailableContacts);

			p1 = Presence.create();
			p1.setFrom(JID.jidInstance("a@b.c/d_second"));
			p1.setType(StanzaType.unavailable);
			presenceModule.process(p1);

			System.out.println(unavailableContacts);

			Assert.assertFalse("Contant shouldn't be available anymore",
							   getPresence().isAvailable(BareJID.bareJIDInstance("a@b.c")));
		} catch (Throwable e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

	}

	@Test
	public void testPresenceStanza() throws Exception {
		Presence p = Presence.create();
		org.junit.Assert.assertEquals(Presence.Show.online, p.getShow());

		p = Presence.create();
		p.setType(StanzaType.unavailable);
		org.junit.Assert.assertEquals(Presence.Show.offline, p.getShow());
	}

}
