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
package tigase.jaxmpp.core.client;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;

/**
 * Default representation of {@linkplain SessionObject}
 * 
 */
public class DefaultSessionObject implements SessionObject {

	private final Logger log = Logger.getLogger(this.getClass().getName());

	protected final PresenceStore presence = new PresenceStore();

	protected final Map<String, Object> properties = new HashMap<String, Object>();

	protected final ResponseManager responseManager = new ResponseManager();

	protected final RosterStore roster = new RosterStore();

	protected Element streamFeatures;

	protected final Map<String, Object> userProperties = new HashMap<String, Object>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void checkHandlersTimeout() throws JaxmppException {
		this.responseManager.checkTimeouts();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() throws JaxmppException {
		log.fine("Clearing properties!");
		this.properties.clear();
		roster.clear();
		presence.clear(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PresenceStore getPresence() {
		return presence;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String key) {
		T t = (T) this.properties.get(key);
		if (t == null)
			t = (T) this.userProperties.get(key);
		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	public Runnable getResponseHandler(Element element, PacketWriter writer) throws JaxmppException {
		return responseManager.getResponseHandler(element, writer, this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RosterStore getRoster() {
		return roster;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Element getStreamFeatures() {
		return this.streamFeatures;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BareJID getUserBareJid() {
		return this.getProperty(USER_BARE_JID);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getUserProperty(String key) {
		return (T) this.userProperties.get(key);
	}

	public String registerResponseHandler(Element stanza, Long timeout, AsyncCallback callback) throws XMLException {
		return responseManager.registerResponseHandler(stanza, timeout, callback);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperty(String key, Object value) {
		this.properties.put(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStreamFeatures(Element element) {
		this.streamFeatures = element;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setUserProperty(String key, Object value) {
		if (value == null)
			this.userProperties.remove(key);
		this.userProperties.put(key, value);
	}

}