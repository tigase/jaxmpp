package tigase.jaxmpp.core.client.xmpp.modules.muc;

import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;

public class Occupant {

	private static long counter = 0;

	private long id;

	private Presence presence;

	public Occupant() {
		id = ++counter;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Occupant))
			return false;
		return ((Occupant) obj).id == id;
	}

	public String getNickname() throws XMLException {
		return this.presence.getFrom().getResource();
	}

	public Presence getPresence() {
		return presence;
	}

	@Override
	public int hashCode() {
		return ("occupant" + id).hashCode();
	}

	public void setPresence(Presence presence) {
		this.presence = presence;
	}

}
