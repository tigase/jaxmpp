package tigase.jaxmpp.core.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class ResponseManager {

	private static final class Entry {
		private final ResponseHandler handler;

		private final long timestamp;

		public Entry(long timestamp, ResponseHandler handler) {
			super();
			this.timestamp = timestamp;
			this.handler = handler;
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
	}

	private final UIDGenerator generator = new UIDGenerator();

	private final Map<Key, Entry> handlers = new HashMap<Key, Entry>();

	public Runnable getResponseHandler(Element element, PacketWriter writer, SessionObject sessionObject) throws XMLException {
		String x = element.getAttribute("from");
		String i = element.getAttribute("id");
		Key key = new Key(i == null ? null : i, x == null ? null : BareJID.bareJIDInstance(x));
		final Entry entry = this.handlers.get(key);
		if (entry != null) {
			// XXX
		}
		return null;
	};

	public String registerResponseHandler(Element stanza, Runnable runnable) throws XMLException {
		String x = stanza.getAttribute("to");
		String i = generator.nextUID();
		Key key = new Key(i == null ? null : i, x == null ? null : BareJID.bareJIDInstance(x));

		Entry entry = new Entry((new Date()).getTime(), null);

		this.handlers.put(key, entry);

		return i;
	}
}
