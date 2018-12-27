/*
 * ListMultiField.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
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
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of list-multi field type.
 * <p>
 * <blockquote
 * cite='http://xmpp.org/extensions/xep-0004.html#protocol-formtypes'>The field
 * enables an entity to gather or provide one or more options from among many. A
 * form-submitting entity chooses one or more items from among the options
 * presented by the form-processing entity and MUST NOT insert new options. The
 * form-submitting entity MUST NOT modify the order of items as received from
 * the form-processing entity, since the order of items MAY be
 * significant.</blockquote>
 * </p>
 */
public class ListMultiField
		extends AbstractField<String[]> {

	ListMultiField(Element element) throws XMLException {
		super("list-multi", element);
	}

	/**
	 * Adds value to field.
	 *
	 * @param value value to add
	 */
	public void addFieldValue(String... value) throws XMLException {
		if (value != null) {
			for (String string : value) {
				addChild(ElementFactory.create("value", string, null));
			}
		}
	}

	/**
	 * Adds option to field.
	 *
	 * @param label label of option
	 * @param value value of option
	 */
	public void addOption(String label, String value) throws XMLException {
		Element o = ElementFactory.create("option");
		if (label != null) {
			o.setAttribute("label", label);
		}
		o.addChild(ElementFactory.create("value", value, null));
		addChild(o);
	}

	/**
	 * Removes all options.
	 */
	public void clearOptions() throws XMLException {
		List<Element> lls = getChildren("option");
		if (lls != null) {
			for (Element element : lls) {
				removeChild(element);
			}
		}
	}

	/**
	 * Removes all values.
	 */
	public void clearValues() throws XMLException {
		List<Element> lls = getChildren("value");
		if (lls != null) {
			for (Element element : lls) {
				removeChild(element);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getFieldValue() throws XMLException {
		ArrayList<String> result = new ArrayList<String>();
		List<Element> lls = getChildren("value");
		if (lls != null) {
			for (Element element : lls) {
				result.add(element.getValue());
			}
		}
		return result.toArray(new String[]{});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFieldValue(String[] value) throws XMLException {
		clearValues();
		if (value != null) {
			for (String string : value) {
				addChild(ElementFactory.create("value", string, null));
			}
		}
	}

}