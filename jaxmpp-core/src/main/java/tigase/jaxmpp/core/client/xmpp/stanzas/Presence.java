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
package tigase.jaxmpp.core.client.xmpp.stanzas;

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class Presence extends Stanza {

	public static enum Show {
		away(3),
		chat(5),
		dnd(1),
		online(4),
		xa(2);

		private final int weight;

		private Show(int weight) {
			this.weight = weight;
		}

		public int getWeight() {
			return weight;
		}

	}

	public static Presence create() throws XMLException {
		return new Presence(new DefaultElement("presence"));
	}

	private String cacheNickname;

	private boolean cacheNicknameSet = false;

	private Integer cachePriority;

	private Show cacheShow;

	private String cacheStatus;

	private boolean cacheStatusSet = false;

	public Presence(Element element) throws XMLException {
		super(element);
		if (!"presence".equals(element.getName()))
			throw new RuntimeException("Wrong element name: " + element.getName());
	}

	public String getNickname() throws XMLException {
		if (cacheNicknameSet)
			return cacheNickname;

		cacheNickname = getChildElementValue("nick", "http://jabber.org/protocol/nick");
		cacheNicknameSet = true;

		return cacheNickname;
	}

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

	public String getStatus() throws XMLException {
		if (cacheStatusSet)
			return cacheStatus;
		cacheStatus = getChildElementValue("status");
		cacheStatusSet = true;

		return cacheStatus;
	}

	public void setNickname(String value) throws XMLException {
		setChildElementValue("nick", "http://jabber.org/protocol/nick", value == null ? null : value.toString());
	}

	public void setPriority(Integer value) throws XMLException {
		setChildElementValue("priority", value == null ? null : value.toString());
	}

	public void setShow(Show value) throws XMLException {
		if (value == null || value == Show.online)
			setChildElementValue("show", null);
		else
			setChildElementValue("show", value.name());
	}

	public void setStatus(String value) throws XMLException {
		setChildElementValue("status", value);
	}

}