package tigase.jaxmpp.core.client.xmpp.modules.presence;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.MockWriter;
import tigase.jaxmpp.core.client.MockSessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.DefaultObservable;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.PresenceEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class PresenceModuleTest {

	@Test
	public void test() {
		final DefaultObservable observable = new DefaultObservable();

		final MockSessionObject sessionObject = new MockSessionObject();
		final MockWriter writer = new MockWriter(sessionObject);

		PresenceModule presenceModule = new PresenceModule(observable, sessionObject, writer);

		final Set<JID> unavailableContacts = new HashSet<JID>();

		presenceModule.addListener(PresenceModule.ContactUnavailable, new Listener<PresenceEvent>() {

			@Override
			public void handleEvent(PresenceEvent be) throws JaxmppException {
				unavailableContacts.add(be.getJid());
			}
		});

		try {
			Presence p1 = Presence.create();
			p1.setFrom(JID.jidInstance("a@b.c/d"));
			presenceModule.process(p1);

			Assert.assertTrue("Contant should be available",
					sessionObject.getPresence().isAvailable(BareJID.bareJIDInstance("a@b.c")));

			p1 = Presence.create();
			p1.setFrom(JID.jidInstance("a@b.c/d_second"));
			presenceModule.process(p1);

			Assert.assertTrue("Contant should be available",
					sessionObject.getPresence().isAvailable(BareJID.bareJIDInstance("a@b.c")));

			p1 = Presence.create();
			p1.setFrom(JID.jidInstance("a@b.c/d"));
			p1.setType(StanzaType.unavailable);
			presenceModule.process(p1);

			Assert.assertTrue("Contant should be available",
					sessionObject.getPresence().isAvailable(BareJID.bareJIDInstance("a@b.c")));

			System.out.println(unavailableContacts);

			p1 = Presence.create();
			p1.setFrom(JID.jidInstance("a@b.c/d_second"));
			p1.setType(StanzaType.unavailable);
			presenceModule.process(p1);

			System.out.println(unavailableContacts);

			Assert.assertFalse("Contant shouldn't be available anymore",
					sessionObject.getPresence().isAvailable(BareJID.bareJIDInstance("a@b.c")));
		} catch (Throwable e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

	}

}
