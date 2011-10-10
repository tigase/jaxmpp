package tigase.jaxmpp.core.client.xmpp.modules.presence;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;

public class PresenceStore {

	static interface Handler {

		public void onOffline(Presence i) throws JaxmppException;

		public void setPresence(Show show, String status, Integer priority) throws XMLException, JaxmppException;

	}

	private Map<BareJID, Presence> bestPresence = new HashMap<BareJID, Presence>();

	private Handler handler;

	private Map<JID, Presence> presenceByJid = new HashMap<JID, Presence>();

	private Map<BareJID, Map<String, Presence>> presencesMapByBareJid = new HashMap<BareJID, Map<String, Presence>>();

	public void clear() throws JaxmppException {
		clear(true);
	}

	public void clear(boolean notify) throws JaxmppException {
		presenceByJid.clear();

		if (notify) {
			Iterator<Presence> it = bestPresence.values().iterator();
			while (it.hasNext()) {
				Presence i = it.next();
				handler.onOffline(i);
				it.remove();
			}
		} else
			bestPresence.clear();

		presencesMapByBareJid.clear();
	}

	public Presence getBestPresence(final BareJID jid) throws XMLException {
		return this.bestPresence.get(jid);
	}

	public Presence getPresence(final JID jid) {
		return this.presenceByJid.get(jid);
	}

	public Map<String, Presence> getPresences(BareJID jid) {
		return this.presencesMapByBareJid.get(jid);
	}

	private Presence intGetBestPresence(final BareJID jid) throws XMLException {
		Map<String, Presence> resourcesPresence = this.presencesMapByBareJid.get(jid);
		Presence result = null;
		if (resourcesPresence != null) {
			Iterator<Presence> it = resourcesPresence.values().iterator();
			while (it.hasNext()) {
				Presence x = it.next();
				Integer p = x.getPriority();
				if (result == null || p >= result.getPriority() && x.getType() == null) {
					result = x;
				}
			}
		}
		return result;
	}

	public boolean isAvailable(BareJID jid) throws XMLException {
		Map<String, Presence> resourcesPresence = this.presencesMapByBareJid.get(jid);
		boolean result = false;
		if (resourcesPresence != null) {
			Iterator<Presence> it = resourcesPresence.values().iterator();
			while (it.hasNext() && !result) {
				Presence x = it.next();
				result = result | x.getType() == null;
			}
		}
		return result;

	}

	void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void setPresence(Show show, String status, Integer priority) throws XMLException, JaxmppException {
		this.handler.setPresence(show, status, priority);
	}

	public void update(final Presence presence) throws XMLException {
		final JID from = presence.getFrom();
		if (from == null)
			return;
		final BareJID bareFrom = from.getBareJid();
		final String resource = from.getResource() == null ? "" : from.getResource();

		this.presenceByJid.put(from, presence);
		Map<String, Presence> m = this.presencesMapByBareJid.get(bareFrom);
		if (m == null) {
			m = new HashMap<String, Presence>();
			this.presencesMapByBareJid.put(bareFrom, m);
		}
		m.put(resource, presence);
		updateBestPresence(presence);
	}

	private void updateBestPresence(final Presence presence) throws XMLException {
		final BareJID bareFrom = presence.getFrom().getBareJid();
		this.bestPresence.put(bareFrom, intGetBestPresence(bareFrom));

		// Presence x = this.bestPresence.get(bareFrom);
		// if (x == null) {
		// this.bestPresence.put(bareFrom, intGetBestPresence(bareFrom));
		// } else {
		// if (presence.getPriority() > x.getPriority() && presence.getType() ==
		// null) {
		// this.bestPresence.put(bareFrom, x);
		// } else {
		// this.bestPresence.put(bareFrom, intGetBestPresence(bareFrom));
		// }
		// }
	}
}
