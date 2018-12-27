/*
 * ResponseManager.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

import java.util.*;
import java.util.logging.Logger;

/**
 * Class for manage responses for IQ {@code type='get'} stanzas.
 */
public class ResponseManager {

	public static final String RESPONSE_MANAGER_KEY = "ResponseManager#RESPONSE_MANAGER";
	protected static final long DEFAULT_TIMEOUT = 1000 * 60;
	protected final Logger log = Logger.getLogger(this.getClass().getName());
	private final Map<String, Entry> handlers = new HashMap<String, Entry>();

	public static Runnable getResponseHandler(Context context, Element element) throws JaxmppException {
		return getResponseManager(context.getSessionObject()).getResponseHandler(element, context);
	}

	public static final ResponseManager getResponseManager(SessionObject sessionObject) {
		return sessionObject.getProperty(RESPONSE_MANAGER_KEY);
	}

	public static String registerResponseHandler(SessionObject sessionObject, Element stanza, Long timeout,
												 AsyncCallback callback) throws XMLException {
		return getResponseManager(sessionObject).registerResponseHandler(stanza, timeout, callback);
	}

	public static final void setResponseManager(SessionObject sessionObject, ResponseManager responseManager) {
		sessionObject.setProperty(Scope.user, RESPONSE_MANAGER_KEY, responseManager);
	}

	/**
	 * Checks if any requested IQ stanza waits for answer longer than declared
	 * timeout.
	 */
	public void checkTimeouts() throws JaxmppException {
		long now = (new Date()).getTime();
		Iterator<java.util.Map.Entry<String, tigase.jaxmpp.core.client.ResponseManager.Entry>> it = this.getHandlers()
				.entrySet()
				.iterator();
		while (it.hasNext()) {
			java.util.Map.Entry<String, tigase.jaxmpp.core.client.ResponseManager.Entry> e = it.next();
			if (e.getValue().timestamp + e.getValue().timeout < now) {
				tigase.jaxmpp.core.client.ResponseManager.Entry entry = e.getValue();
				it.remove();
				try {
					log.fine("Request id=" + entry.stanzaId + "; Timeout.");
					entry.callback.onTimeout();
				} catch (XMLException e1) {
				}
			}
		}
	}

	protected Map<String, Entry> getHandlers() {
		return handlers;
	}

	/**
	 * Returns handler for response of sent <code><iq/></code> stanza.
	 *
	 * @param element reponse <code><iq/></code> stanza.
	 *
	 * @return Runnable object with handler
	 *
	 * @throws XMLException
	 */
	public Runnable getResponseHandler(final Element element, Context context) throws JaxmppException {
		if (!Stanza.canBeConverted(element)) {
			return null;
		}

		final String id = element.getAttribute("id");
		if (id == null) {
			return null;
		}
		final Entry entry = this.getHandlers().get(id);
		if (entry == null) {
			return null;
		}

		if (!verify(element, entry, context.getSessionObject())) {
			return null;
		}

		this.getHandlers().remove(id);

		AbstractStanzaHandler r = new AbstractStanzaHandler(element, context) {

			@Override
			protected void process() throws JaxmppException {
				final String type = this.element.getAttribute("type");

				if (type != null && type.equals("result")) {
					log.fine("Request id=" + entry.stanzaId + "; Result received.");
					entry.callback.onSuccess(Stanza.create(this.element));
				} else if (type != null && type.equals("error")) {
					log.fine("Request id=" + entry.stanzaId + "; Error received.");
					List<Element> es = this.element.getChildren("error");
					final Element error;
					if (es != null && es.size() > 0) {
						error = es.get(0);
					} else {
						error = null;
					}

					ErrorCondition errorCondition = null;
					if (error != null) {
						List<Element> conds = error.getChildrenNS(XMPPException.XMLNS);
						if (conds != null && conds.size() > 0) {
							errorCondition = ErrorCondition.getByElementName(conds.get(0).getName());
						}
					}
					entry.callback.onError(Stanza.create(this.element), errorCondition);
				}
			}
		};
		return r;
	}

	/**
	 * Register callback for response of sent <code><iq/></code> stanza.
	 *
	 * @param stanza sent <code><iq/></code> stanza.
	 * @param timeout timeout. After it method {@linkplain AsyncCallback#onTimeout() onTimeout()} will be called.
	 * @param callback callback
	 *
	 * @return id of stanza
	 *
	 * @throws XMLException
	 */
	public String registerResponseHandler(final Element stanza, final Long timeout, final AsyncCallback callback)
			throws XMLException {
		if (stanza == null) {
			return null;
		}
		String x = stanza.getAttribute("to");
		String id = stanza.getAttribute("id");
		if (id == null) {
			id = UIDGenerator.next();
			stanza.setAttribute("id", id);
		}

		if (callback != null) {
			Entry entry = new Entry(x == null ? null : JID.jidInstance(x), id, (new Date()).getTime(),
									timeout == null ? DEFAULT_TIMEOUT : timeout, callback);
			this.getHandlers().put(id, entry);
		}

		return id;
	}

	private boolean verify(final Element response, final Entry entry, final SessionObject sessionObject)
			throws XMLException {
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

	protected static final class Entry {

		private final AsyncCallback callback;

		private final JID jid;
		private final String stanzaId;
		private final long timeout;
		private final long timestamp;

		public Entry(JID jid, String stanzaId, long timestamp, long timeout, AsyncCallback callback) {
			super();
			this.jid = jid;
			this.timestamp = timestamp;
			this.timeout = timeout;
			this.callback = callback;
			this.stanzaId = stanzaId;
		}

	}
}