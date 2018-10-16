/*
 * TextSingleField.java
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
package tigase.jaxmpp.core.client.xmpp.forms;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Implementation of text-single field type.
 * <p>
 * <blockquote
 * cite='http://xmpp.org/extensions/xep-0004.html#protocol-formtypes'>The field
 * enables an entity to gather or provide a single line or word of text, which
 * may be shown in an interface. This field type is the default and MUST be
 * assumed if a form-submitting entity receives a field type it does not
 * understand.</blockquote>
 * </p>
 */
public class TextSingleField
		extends AbstractField<String> {

	TextSingleField(Element element) throws XMLException {
		super("text-single", element);
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