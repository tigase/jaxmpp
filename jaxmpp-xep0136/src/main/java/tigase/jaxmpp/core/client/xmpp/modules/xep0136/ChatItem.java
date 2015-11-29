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
package tigase.jaxmpp.core.client.xmpp.modules.xep0136;

import java.util.Date;
import tigase.jaxmpp.core.client.xml.Element;

public class ChatItem {

	@Override
	public String toString() {
		return "ChatItem{" + "body=" + body + ", date=" + date + ", type=" + type + '}';
	}

	public static enum Type {
		FROM,
		TO
	}

	private final String body;
	private final Date date;
	private final Type type;
	private final Element item;

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
	
}
