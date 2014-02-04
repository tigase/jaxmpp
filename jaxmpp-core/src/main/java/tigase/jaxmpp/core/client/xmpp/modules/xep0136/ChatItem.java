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

public class ChatItem {

	public static enum Type {

		FROM,
		TO
	}

	private String body;
	private Date date;
	private final Type type;

	public ChatItem(Type type) {
		this.type = type;
	}

	public ChatItem(final Type type, final Date date, final String body) {
		this.type = type;
		this.date = date;
		this.body = body;
	}

	public String getBody() {
		return body;
	}

	public Date getDate() {
		return date;
	}

	public Type getType() {
		return type;
	}
}
