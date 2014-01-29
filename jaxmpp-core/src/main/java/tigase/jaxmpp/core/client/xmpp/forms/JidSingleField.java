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

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Implementation of jid-single field type.
 * <p>
 * <blockquote
 * cite='http://xmpp.org/extensions/xep-0004.html#protocol-formtypes'>The field
 * enables an entity to gather or provide a single Jabber ID.</blockquote>
 * </p>
 */
public class JidSingleField extends AbstractField<JID> {

	JidSingleField(Element element) throws XMLException {
		super("jid-single", element);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JID getFieldValue() throws XMLException {
		String x = getChildElementValue("value");
		return x == null ? null : JID.jidInstance(x);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFieldValue(JID value) throws XMLException {
		setChildElementValue("value", value == null ? null : value.toString());
	}

}