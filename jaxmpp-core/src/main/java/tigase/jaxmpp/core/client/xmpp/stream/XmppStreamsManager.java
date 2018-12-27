/*
 * XmppStreamsManager.java
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

package tigase.jaxmpp.core.client.xmpp.stream;

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.ClearedHandler;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.DefaultXMPPStream;

import java.util.HashMap;
import java.util.Set;

public class XmppStreamsManager {

	public static final String DEFAULT_XMPP_STREAM_KEY = "DEFAULT_XMPP_STREAM_KEY";

	private static final String XMPP_STREAMS_MANAGER_KEY = "STREAMS_MANAGER_KEY";
	private final HashMap<JID, XMPPStream> registeredStreams = new HashMap<JID, XMPPStream>();
	private Context context;
	private DefaultXMPPStream defaultStream;
	private final ClearedHandler clearedHandler = new ClearedHandler() {

		@Override
		public void onCleared(SessionObject sessionObject, Set<Scope> scopes) throws JaxmppException {
			if (scopes.contains(Scope.stream)) {
				defaultStream.setFeatures(null);
				registeredStreams.clear();
			}
		}
	};

	public static XmppStreamsManager getStreamsManager(SessionObject sessionObject) {
		return sessionObject.getProperty(XMPP_STREAMS_MANAGER_KEY);
	}

	public static void setStreamsManager(SessionObject sessionObject, XmppStreamsManager streamsManager) {
		sessionObject.setProperty(Scope.user, XMPP_STREAMS_MANAGER_KEY, streamsManager);
	}

	public DefaultXMPPStream getDefaultStream() {
		return defaultStream;
	}

	public XMPPStream getXmppStream(JID jid) {
		return this.registeredStreams.get(jid);
	}

	public void registerXmppStream(JID jid, XMPPStream xmppStream) {
		this.registeredStreams.put(jid, xmppStream);
	}

	public void setContext(Context context) {
		if (this.context != null) {
			this.context.getEventBus().remove(ClearedHandler.ClearedEvent.class, clearedHandler);
		}
		this.context = context;
		if (this.context != null) {
			this.context.getEventBus().addHandler(ClearedHandler.ClearedEvent.class, clearedHandler);
			this.defaultStream = context.getSessionObject().getProperty(DEFAULT_XMPP_STREAM_KEY);
		}
	}

	public void unregisterXmppStream(JID jid) {
		this.registeredStreams.remove(jid);
	}

	public void writeToStream(Element stanza) throws JaxmppException {
		String to = stanza.getAttribute("to");
		XMPPStream outputStream = null;
		if (to != null) {
			outputStream = this.registeredStreams.get(JID.jidInstance(to));
		}
		if (outputStream == null) {
			outputStream = defaultStream;
		}
		outputStream.write(stanza);
	}

}
