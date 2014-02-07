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
 * Implementation of Fixed field type.
 * <p>
 * <blockquote
 * cite='http://xmpp.org/extensions/xep-0004.html#protocol-formtypes'>The field
 * is intended for data description (e.g., human-readable text such as "section"
 * headers) rather than data gathering or provision. The &lt;value/&gt; child
 * SHOULD NOT contain newlines (the \n and \r characters); instead an
 * application SHOULD generate multiple fixed fields, each with one
 * &lt;value/&gt; child.</blockquote>
 * </p>
 */
public class FixedField extends AbstractField<String> {

	FixedField(Element element) throws XMLException {
		super("fixed", element);
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