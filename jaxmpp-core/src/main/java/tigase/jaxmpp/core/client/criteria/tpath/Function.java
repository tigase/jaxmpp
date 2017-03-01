/*
 * Function.java
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
package tigase.jaxmpp.core.client.criteria.tpath;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public interface Function {

	Object value(Element element) throws XMLException;

	class Attr
			implements Function {

		private final String attName;

		public Attr(String attName) {
			this.attName = attName;
		}

		@Override
		public Object value(Element element) throws XMLException {
			return element.getAttribute(attName);
		}

	}

	final class Value
			implements Function {

		@Override
		public Object value(Element element) throws XMLException {
			return element.getValue();
		}
	}

}