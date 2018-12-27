/*
 * ConnectionSession.java
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
package tigase.jaxmpp.core.client.xmpp.modules.connection;

import tigase.jaxmpp.core.client.DataHolder;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;

/**
 * @author andrzej
 */
public class ConnectionSession
		extends DataHolder {

	private final JID peer;
	private final SessionObject sessionObject;
	private final String sid;
	private boolean incoming = false;

	protected ConnectionSession(SessionObject sessionObject, JID peer, String sid, boolean tcp) {
		this.sessionObject = sessionObject;
		this.peer = peer;
		this.sid = sid;
	}

	public JID getPeer() {
		return peer;
	}

	public SessionObject getSessionObject() {
		return sessionObject;
	}

	public String getSid() {
		return sid;
	}

	public boolean isIncoming() {
		return incoming;
	}

	public void setIncoming(boolean incoming) {
		this.incoming = incoming;
	}

}
