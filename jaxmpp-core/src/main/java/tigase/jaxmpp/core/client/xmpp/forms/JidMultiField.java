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

import java.util.ArrayList;
import java.util.List;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Implementation of jid-multi field type.
 * <p>
 * <blockquote
 * cite='http://xmpp.org/extensions/xep-0004.html#protocol-formtypes'> The field
 * enables an entity to gather or provide multiple Jabber IDs. Each provided JID
 * SHOULD be unique (as determined by comparison that includes application of
 * the Nodeprep, Nameprep, and Resourceprep profiles of Stringprep as specified
 * in XMPP Core), and duplicate JIDs MUST be ignored. </blockquote>
 * </p>
 */
public class JidMultiField extends AbstractField<JID[]> {

	JidMultiField(Element element) throws XMLException {
		super("jid-multi", element);
	}

	/**
	 * Add value to field.
	 * 
	 * @param value
	 *            JID to add
	 */
	public void addFieldValue(JID... value) throws XMLException {
		if (value != null)
			for (JID string : value) {
				addChild(ElementFactory.create("value", string == null ? null : string.toString(), null));
			}
	}

	/**
	 * Removes all values.
	 */
	public void clearValues() throws XMLException {
		List<Element> lls = getChildren("value");
		if (lls != null)
			for (Element element : lls) {
				removeChild(element);
			}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JID[] getFieldValue() throws XMLException {
		ArrayList<JID> result = new ArrayList<JID>();
		List<Element> lls = getChildren("value");
		if (lls != null)
			for (Element element : lls) {
				String x = element.getValue();
				result.add(x == null ? null : JID.jidInstance(x));
			}
		return result.toArray(new JID[] {});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFieldValue(JID[] value) throws XMLException {
		clearValues();
		if (value != null) {
			for (JID string : value) {
				addChild(ElementFactory.create("value", string == null ? null : string.toString(), null));
			}
		}
	}

}