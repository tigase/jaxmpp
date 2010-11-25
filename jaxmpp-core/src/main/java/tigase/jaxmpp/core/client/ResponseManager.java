package tigase.jaxmpp.core.client;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public class ResponseManager {

	// TODO add timeout handling

	private static final class Entry {
		private final AsyncCallback callback;

		private final long timestamp;

		public Entry(long timestamp, AsyncCallback callback) {
			super();
			this.timestamp = timestamp;
			this.callback = callback;
		}

	}

	public static final class Key {

		final String $toString;

		final String id;

		final BareJID jid;

		public Key(String id, BareJID jid) {
			super();
			if (id == null)
				throw new RuntimeException("ID can't be null");
			this.id = id;
			this.jid = jid;
			this.$toString = ("k:" + (id == null ? "" : id) + ":" + (jid == null ? "" : jid)).intern();
		}

		@Override
		public boolean equals(Object arg0) {
			if (arg0 == this)
				return true;
			if (!(arg0 instanceof Key))
				return false;

			return $toString.equals(((Key) arg0).$toString);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public String toString() {
			return $toString;
		}
	}

	private final Map<Key, Entry> handlers = new HashMap<Key, Entry>();

	public Runnable getResponseHandler(final Element element, PacketWriter writer, SessionObject sessionObject)
			throws XMLException {
		String x = element.getAttribute("from");
		String i = element.getAttribute("id");
		if (i == null)
			return null;
		Key key = new Key(i, x == null ? null : BareJID.bareJIDInstance(x));
		final Entry entry = this.handlers.get(key);
		final Stanza stanza = element instanceof Stanza ? (Stanza) element : Stanza.create(element);
		if (entry != null) {
			AbstractStanzaHandler r = new AbstractStanzaHandler(stanza, writer, sessionObject) {

				@Override
				protected void process() throws XMLException, XMPPException {
					final String type = this.stanza.getAttribute("type");

					if (type != null && type.equals("result")) {
						entry.callback.onSuccess(this.stanza);
					} else if (type != null && type.equals("error")) {
						List<Element> es = this.stanza.getChildren("error");
						final Element error;
						if (es != null && es.size() > 0)
							error = es.get(0);
						else
							error = null;

						ErrorCondition errorCondition = null;
						if (error != null) {
							List<Element> conds = error.getChildrenNS(XMPPException.XMLNS);
							if (conds != null && conds.size() > 0) {
								errorCondition = ErrorCondition.getByElementName(conds.get(0).getName());
							}
						}
						entry.callback.onError(this.stanza, errorCondition);
					}
				}
			};
			return r;
		}
		return null;
	};

	public String registerResponseHandler(Element stanza, AsyncCallback callback) throws XMLException {
		String x = stanza.getAttribute("to");
		String i = stanza.getAttribute("id");
		if (i == null) {
			i = UIDGenerator.next();
			stanza.setAttribute("id", i);
		}
		Key key = new Key(i == null ? null : i, x == null ? null : BareJID.bareJIDInstance(x));

		Entry entry = new Entry((new Date()).getTime(), callback);

		this.handlers.put(key, entry);

		return i;
	}
}
