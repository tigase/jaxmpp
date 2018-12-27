/*
 * AdHocRequest.java
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
package tigase.jaxmpp.core.client.xmpp.modules.adhoc;

import tigase.jaxmpp.core.client.xmpp.forms.JabberDataElement;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;

import java.util.Date;
import java.util.Map;

/**
 * Class to provide request data to ad-hoc command.
 */
public class AdHocRequest {

	private final Action action;
	private final IQ iq;
	private final String node;
	private final Map<String, Session> sessions;
	private JabberDataElement form;
	private Session session;
	private String sessionId;

	public AdHocRequest(Action action, String node, String sessionId, IQ iq, Map<String, Session> sessions) {
		super();
		this.action = action;
		this.node = node;
		this.sessionId = sessionId;
		this.iq = iq;
		this.sessions = sessions;
	}

	/**
	 * Returns Action selected by client.
	 *
	 * @return {@linkplain Action Action}
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * Return Data Form sent by client in request.
	 *
	 * @return {@linkplain JabberDataElement Data Form}
	 */
	public JabberDataElement getForm() {
		return form;
	}

	void setForm(JabberDataElement form) {
		this.form = form;
	}

	/**
	 * Returns IQ stanza constains command request.
	 *
	 * @return IQ stanza
	 */
	public IQ getIq() {
		return iq;
	}

	/**
	 * Returns called node of ad-hoc command.
	 *
	 * @return node name
	 */
	public String getNode() {
		return node;
	}

	/**
	 * Returns current {@linkplain Session Session}. If there is no session
	 * related, creates one.
	 *
	 * @return {@linkplain Session Session}
	 */
	public Session getSession() {
		return getSession(true);
	}

	/**
	 * Returns current {@linkplain Session Session}. If there is no session
	 * related, creates one if <code>createNew</code> is <code>true</code>.
	 *
	 * @param createNew <code>true</code> to create new session.
	 *
	 * @return {@linkplain Session Session} or <code>null</code> if <code>createNew</code> is <code>false</code> and
	 * request hasn't Session.
	 */
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

	/**
	 * Return session ID, or <code>null</code> if request hasn't
	 * {@linkplain Session Session}.
	 *
	 * @return session ID if present.
	 */
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}