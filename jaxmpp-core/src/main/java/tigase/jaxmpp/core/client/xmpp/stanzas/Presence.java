/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc.
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
package tigase.jaxmpp.core.client.xmpp.stanzas;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Representation of Presence stanza.
 * 
 */
public class Presence extends Stanza {

	/**
	 * Availability sub-state
	 * 
	 */
	public static enum Show {
		/**
		 * The entity or resource is temporarily away.
		 */
		away(3),
		/**
		 * The entity or resource is actively interested in chatting.
		 */
		chat(5),
		/**
		 * The entity or resource is busy (dnd = "Do Not Disturb").
		 */
		dnd(1),
		/**
		 * The entity or resource is online and available.
		 */
		online(4),
		/**
		 * The entity or resource is away for an extended period (xa =
		 * "eXtended Away").
		 */
		xa(2);

		private final int weight;

		private Show(int weight) {
			this.weight = weight;
		}

		public int getWeight() {
			return weight;
		}

	}

	/**
	 * Creates new instance of stanza.
	 * 
	 * @return {@linkplain Presence}
	 */
	public static Presence create() throws JaxmppException {
		return createPresence();
	}

	private String cacheNickname;

	private boolean cacheNicknameSet = false;

	private Integer cachePriority;

	private Show cacheShow;

	private String cacheStatus;

	private boolean cacheStatusSet = false;

	Presence(Element element) throws XMLException {
		super(element);
		if (!"presence".equals(element.getName()))
			throw new RuntimeException("Wrong element name: " + element.getName());
	}

	/**
	 * Returns nickname. Nickname is defined in <a
	 * href='http://xmpp.org/extensions/xep-0172.html'>XEP-0172: User
	 * Nickname</a>.
	 * 
	 * @return nickname or <code>null</code> if not present.
	 */
	public String getNickname() throws XMLException {
		if (cacheNicknameSet)
			return cacheNickname;

		cacheNickname = getChildElementValue("nick", "http://jabber.org/protocol/nick");
		cacheNicknameSet = true;

		return cacheNickname;
	}

	/**
	 * Returns priority level of resource.
	 * 
	 * @return priority level.
	 */
	public Integer getPriority() throws XMLException {
		if (cachePriority != null)
			return cachePriority;

		String x = getChildElementValue("priority");
		final Integer p;
		if (x == null)
			p = 0;
		else
			p = Integer.valueOf(x);

		cachePriority = p;

		return p;
	}

	/**
	 * Return avaiability substate.
	 * 
	 * @return {@linkplain Show}
	 */
	public Show getShow() throws XMLException {
		if (cacheShow != null)
			return cacheShow;

		String x = getChildElementValue("show");
		final Show show;
		if (x == null)
			show = Show.online;
		else
			show = Show.valueOf(x);
		cacheShow = show;
		return show;
	}

	/**
	 * Returns natural-language description of an entity's availability.
	 * 
	 * @return status
	 */
	public String getStatus() throws XMLException {
		if (cacheStatusSet)
			return cacheStatus;
		cacheStatus = getChildElementValue("status");
		cacheStatusSet = true;

		return cacheStatus;
	}

	/**
	 * Sets nickname. Nickname is defined in <a
	 * href='http://xmpp.org/extensions/xep-0172.html'>XEP-0172: User
	 * Nickname</a>.
	 * 
	 * @param nickname
	 *            nickname
	 */
	public void setNickname(String nickname) throws XMLException {
		setChildElementValue("nick", "http://jabber.org/protocol/nick", nickname == null ? null : nickname.toString());
	}

	/**
	 * Sets priority level of resource.
	 * 
	 * @param value
	 * @throws XMLException
	 */
	public void setPriority(Integer value) throws XMLException {
		setChildElementValue("priority", value == null ? null : value.toString());
	}

	/**
	 * Sets avaiability sub-state.
	 * 
	 * @param show
	 *            {@linkplain Show}
	 * @throws XMLException
	 */
	public void setShow(Show show) throws XMLException {
		if (show == null || show == Show.online)
			setChildElementValue("show", null);
		else
			setChildElementValue("show", show.name());
	}

	/**
	 * Sets natural-language description of an entity's availability.
	 * 
	 * @param status
	 *            description
	 * @throws XMLException
	 */
	public void setStatus(String status) throws XMLException {
		setChildElementValue("status", status);
	}

}