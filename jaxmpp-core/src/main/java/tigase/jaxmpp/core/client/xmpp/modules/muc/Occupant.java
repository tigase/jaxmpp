package tigase.jaxmpp.core.client.xmpp.modules.muc;

import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;

public class Occupant {

	private static long counter = 0;

	private Affiliation cacheAffiliation;

	private Role cacheRole;

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

	public Affiliation getAffiliation() {
		try {
			if (cacheAffiliation == null) {
				final XMucUserElement xUser = XMucUserElement.extract(presence);
				if (xUser != null) {
					cacheAffiliation = xUser.getAffiliation();
				}
			}
			return cacheAffiliation == null ? Affiliation.none : cacheAffiliation;
		} catch (XMLException e) {
			return Affiliation.none;
		}
	}

	public String getNickname() throws XMLException {
		return this.presence.getFrom().getResource();
	}

	public Presence getPresence() {
		return presence;
	}

	public Role getRole() {
		try {
			if (cacheRole == null) {
				final XMucUserElement xUser = XMucUserElement.extract(presence);
				if (xUser != null) {
					cacheRole = xUser.getRole();
				}
			}
			return cacheRole == null ? Role.none : cacheRole;
		} catch (XMLException e) {
			return Role.none;
		}
	}

	@Override
	public int hashCode() {
		return ("occupant" + id).hashCode();
	}

	public void setPresence(Presence presence) {
		cacheAffiliation = null;
		cacheRole = null;
		this.presence = presence;
	}

}
