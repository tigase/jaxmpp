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
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Abstract class to implement fields.
 * 
 * @param <T>
 *            type of field
 */
public abstract class AbstractField<T> extends ElementWrapper implements Field<T> {

	AbstractField(String elementType, Element element) throws XMLException {
		super(element);
		if (elementType != null)
			setAttribute("type", elementType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDesc() throws XMLException {
		return getChildElementValue("desc");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLabel() throws XMLException {
		return getAttribute("label");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getType() throws XMLException {
		return getAttribute("type");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getVar() throws XMLException {
		return getAttribute("var");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRequired() throws XMLException {
		return getFirstChild("required") != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDesc(String desc) throws XMLException {
		setChildElementValue("desc", desc);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLabel(String label) throws XMLException {
		setAttribute("label", label);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRequired(boolean value) throws XMLException {
		Element b = getFirstChild("required");
		if (!value && b != null)
			removeChild(b);
		else if (value && b == null) {
			b = ElementFactory.create("required");
			addChild(b);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVar(String var) throws XMLException {
		setAttribute("var", var);
	}

}