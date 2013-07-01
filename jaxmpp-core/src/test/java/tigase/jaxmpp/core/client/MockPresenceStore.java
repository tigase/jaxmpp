package tigase.jaxmpp.core.client;

import java.util.HashMap;
import java.util.Map;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;

public class MockPresenceStore extends PresenceStore {

	public MockPresenceStore() {
		presencesMapByBareJid = new HashMap<BareJID, Map<String, Presence>>();
		presenceByJid = new HashMap<JID, Presence>();
		bestPresence = new HashMap<BareJID, Presence>();
	}

	@Override
	protected Map<String, Presence> createResourcePresenceMap() {
		return new HashMap<String, Presence>();
	}

}
