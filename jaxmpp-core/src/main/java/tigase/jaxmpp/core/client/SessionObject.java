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

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;

public interface SessionObject extends UserProperties {

	public static final String NICKNAME = "nickname";

	public static final String PASSWORD = "password";

	public static final String RESOURCE = "resource";

	public static final String SERVER_NAME = "serverName";

	public static final String USER_BARE_JID = "userBareJid";

	public void checkHandlersTimeout() throws JaxmppException;

	public void clear() throws JaxmppException;

	public PresenceStore getPresence();

	public <T> T getProperty(String key);

	public Runnable getResponseHandler(final Element element, PacketWriter writer, SessionObject sessionObject)
			throws XMLException;

	public RosterStore getRoster();

	public Element getStreamFeatures();

	public BareJID getUserBareJid();

	public String registerResponseHandler(Element stanza, Long timeout, AsyncCallback callback) throws XMLException;

	public void setProperty(String key, Object value);

	public void setStreamFeatures(Element element);

}