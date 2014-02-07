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
 * Implementation of Boolean field type.
 * <p>
 * <blockquote
 * cite='http://xmpp.org/extensions/xep-0004.html#protocol-formtypes'>The field
 * enables an entity to gather or provide an either-or choice between two
 * options. The default value is "false".</blockquote>
 * </p>
 */
public class BooleanField extends AbstractField<Boolean> {

	public static boolean parse(String value) {
		if (value == null)
			return Boolean.FALSE;
		else if (value.equals("1") || value.equals("true"))
			return Boolean.TRUE;
		else
			return Boolean.FALSE;
	}

	BooleanField(Element element) throws XMLException {
		super("boolean", element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean getFieldValue() throws XMLException {
		final String t = getChildElementValue("value");
		return parse(t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFieldValue(Boolean value) throws XMLException {
		if (value == null)
			setChildElementValue("value", "0");
		else if (value.booleanValue())
			setChildElementValue("value", "1");
		else
			setChildElementValue("value", "0");
	}
}