package tigase.jaxmpp.j2se;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;

public class J2SEPresenceStore extends PresenceStore {

	public J2SEPresenceStore() {
		presencesMapByBareJid = new ConcurrentHashMap<BareJID, Map<String, Presence>>();
		presenceByJid = new ConcurrentHashMap<JID, Presence>();
		bestPresence = new ConcurrentHashMap<BareJID, Presence>();
	}

}
