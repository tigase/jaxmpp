/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
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
package tigase.jaxmpp.core.client.xmpp.modules.adhoc;

import java.util.Date;
import java.util.Map;

import tigase.jaxmpp.core.client.xmpp.forms.JabberDataElement;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;

public class AdHocRequest {

	private final Action action;

	private JabberDataElement form;

	private final IQ iq;

	private final String node;

	private Session session;

	private String sessionId;

	private final Map<String, Session> sessions;

	public AdHocRequest(Action action, String node, String sessionId, IQ iq, Map<String, Session> sessions) {
		super();
		this.action = action;
		this.node = node;
		this.sessionId = sessionId;
		this.iq = iq;
		this.sessions = sessions;
	}

	public Action getAction() {
		return action;
	}

	public JabberDataElement getForm() {
		return form;
	}

	public IQ getIq() {
		return iq;
	}

	public String getNode() {
		return node;
	}

	public Session getSession() {
		return getSession(true);
	}

	public Session getSession(boolean createNew) {
		if (session == null && sessionId != null) {
			session = this.sessions.get(sessionId);
		}
		if (session == null && createNew) {
			sessionId = AdHocCommansModule.generateSessionId();
			session = new Session(sessionId);
			session.setLastRequest(new Date());
			this.sessions.put(sessionId, session);
		}
		return session;
	}

	public String getSessionId() {
		return sessionId;
	}

	void setForm(JabberDataElement form) {
		this.form = form;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}