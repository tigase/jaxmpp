package tigase.jaxmpp.core.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public class ResponseManager {

	// TODO add timeout handling

	private static final class Entry {

		private final AsyncCallback callback;

		private final JID jid;

		private final long timeout;

		private final long timestamp;

		public Entry(JID jid, long timestamp, long timeout, AsyncCallback callback) {
			super();
			this.jid = jid;
			this.timestamp = timestamp;
			this.timeout = timeout;
			this.callback = callback;
		}

	}

	private static final long DEFAULT_TIMEOUT = 1000 * 60;

	private final Map<String, Entry> handlers = new HashMap<String, Entry>();

	private final Logger log = Logger.getLogger(this.getClass().getName());

	public void checkTimeouts() throws JaxmppException {
		long now = (new Date()).getTime();
		Iterator<java.util.Map.Entry<String, tigase.jaxmpp.core.client.ResponseManager.Entry>> it = this.handlers.entrySet().iterator();
		while (it.hasNext()) {
			java.util.Map.Entry<String, tigase.jaxmpp.core.client.ResponseManager.Entry> e = it.next();
			if (e.getValue().timestamp + e.getValue().timeout < now) {
				it.remove();
				try {
					e.getValue().callback.onTimeout();
				} catch (XMLException e1) {
				}
			}
		}
	}

	public Runnable getResponseHandler(final Element element, PacketWriter writer, SessionObject sessionObject)
			throws XMLException {
		final String id = element.getAttribute("id");
		if (id == null)
			return null;
		final Entry entry = this.handlers.get(id);
		if (entry == null)
			return null;

		if (!verify(element, entry, sessionObject))
			return null;

		this.handlers.remove(id);

		final Stanza stanza = element instanceof Stanza ? (Stanza) element : Stanza.create(element);
		AbstractStanzaHandler r = new AbstractStanzaHandler(stanza, writer, sessionObject) {

			@Override
			protected void process() throws JaxmppException {
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

	public String registerResponseHandler(final Element stanza, final Long timeout, final AsyncCallback callback)
			throws XMLException {
		if (stanza == null)
			return null;
		String x = stanza.getAttribute("to");
		String id = stanza.getAttribute("id");
		if (id == null) {
			id = UIDGenerator.next();
			stanza.setAttribute("id", id);
		}

		if (callback != null) {
			Entry entry = new Entry(x == null ? null : JID.jidInstance(x), (new Date()).getTime(),
					timeout == null ? DEFAULT_TIMEOUT : timeout, callback);
			this.handlers.put(id, entry);
		}

		return id;
	}

	private boolean verify(final Element response, final Entry entry, final SessionObject sessionObject) throws XMLException {
		String x = response.getAttribute("from");
		final JID jid = x == null ? null : JID.jidInstance(x);

		if (jid != null && entry.jid != null && jid.getBareJid().equals(entry.jid.getBareJid())) {
			return true;
		} else if (entry.jid == null && jid == null) {
			return true;
		} else {
			final JID userJID = sessionObject.getProperty(ResourceBinderModule.BINDED_RESOURCE_JID);
			if (entry.jid == null && userJID != null && jid.getBareJid().equals(userJID.getBareJid())) {
				return true;
			}
		}
		return false;
	}
}
