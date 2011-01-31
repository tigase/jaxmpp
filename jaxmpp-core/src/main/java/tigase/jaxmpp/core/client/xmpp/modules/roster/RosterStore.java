package tigase.jaxmpp.core.client.xmpp.modules.roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;

public class RosterStore {

	static interface Handler {

		void add(BareJID jid, String name, Collection<String> groups) throws XMLException, JaxmppException;

		void remove(BareJID jid) throws XMLException, JaxmppException;

		void update(RosterItem item) throws XMLException, JaxmppException;
	}

	private Handler handler;

	protected final Map<BareJID, RosterItem> roster = new HashMap<BareJID, RosterItem>();

	public void add(BareJID jid, String name) throws XMLException, JaxmppException {
		add(jid, name, new ArrayList<String>());
	}

	public void add(BareJID jid, String name, Collection<String> groups) throws XMLException, JaxmppException {
		if (this.handler != null)
			this.handler.add(jid, name, groups);
	}

	public void add(BareJID jid, String name, String[] groups) throws XMLException, JaxmppException {
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

	public List<RosterItem> getAll() {
		ArrayList<RosterItem> result = new ArrayList<RosterItem>();
		result.addAll(this.roster.values());
		return result;
	}

	public void remove(BareJID jid) throws XMLException, JaxmppException {
		if (handler != null)
			this.handler.remove(jid);
	}

	void removeItem(BareJID jid) {
		this.roster.remove(jid);
	}

	void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void update(RosterItem item) throws XMLException, JaxmppException {
		if (this.handler != null)
			this.handler.update(item);
	}

}
