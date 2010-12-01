package tigase.jaxmpp.core.client.xmpp.modules.roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.xml.XMLException;

public class RosterStore {

	static interface Handler {

		void add(BareJID jid, String name, Collection<String> groups) throws XMLException;

		void remove(BareJID jid) throws XMLException;

		void update(RosterItem item) throws XMLException;
	}

	private Handler handler;

	protected final Map<BareJID, RosterItem> roster = new HashMap<BareJID, RosterItem>();

	public void add(BareJID jid, String name) throws XMLException {
		add(jid, name, new ArrayList<String>());
	}

	public void add(BareJID jid, String name, Collection<String> groups) throws XMLException {
		if (this.handler != null)
			this.handler.add(jid, name, groups);
	}

	public void add(BareJID jid, String name, String[] groups) throws XMLException {
		ArrayList<String> x = new ArrayList<String>();
		if (groups != null)
			for (String string : groups) {
				x.add(string);
			}
		add(jid, name, x);
	}

	void addItem(RosterItem item) {
		this.roster.put(item.getJid(), item);
	}

	public RosterItem get(BareJID jid) {
		return this.roster.get(jid);
	}

	public void remove(BareJID jid) throws XMLException {
		if (handler != null)
			this.handler.remove(jid);
	}

	void removeItem(BareJID jid) {
		this.roster.remove(jid);
	}

	void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void update(RosterItem item) throws XMLException {
		if (this.handler != null)
			this.handler.update(item);
	}

}
