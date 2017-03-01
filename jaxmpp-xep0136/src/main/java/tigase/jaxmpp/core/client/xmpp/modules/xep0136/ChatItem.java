/*
 * ChatItem.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.core.client.xmpp.modules.xep0136;

import tigase.jaxmpp.core.client.xml.Element;

import java.util.Date;

public class ChatItem {

	public static enum Type {
		FROM,
		TO
	}

	private final String body;
	private final Date date;
	private final Element item;
	private final Type type;

	public ChatItem(final Type type, final Date date, final String body, final Element item) {
		this.type = type;
		this.date = date;
		this.body = body;
		this.item = item;
	}

	public String getBody() {
		return body;
	}

	public Date getDate() {
		return date;
	}

	public Element getItem() {
		return item;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "ChatItem{" + "body=" + body + ", date=" + date + ", type=" + type + '}';
	}

}
