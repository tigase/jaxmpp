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
package tigase.jaxmpp.core.client.xmpp.forms;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Implementation of text-private field type.
 * <p>
 * <blockquote
 * cite='http://xmpp.org/extensions/xep-0004.html#protocol-formtypes'>The field
 * enables an entity to gather or provide a single line or word of text, which
 * shall be obscured in an interface (e.g., with multiple instances of the
 * asterisk character).</blockquote>
 * </p>
 */
public class TextPrivateField extends AbstractField<String> {

	TextPrivateField(Element element) throws XMLException {
		super("text-private", element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFieldValue() throws XMLException {
		return getChildElementValue("value");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFieldValue(String value) throws XMLException {
		setChildElementValue("value", value);
	}
}