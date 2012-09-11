/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
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

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
import tigase.jaxmpp.core.client.xml.XMLException;

public abstract class AbstractField<T> extends ElementWrapper implements Field<T> {

	AbstractField(String elementType, Element element) throws XMLException {
		super(element);
		if (elementType != null)
			setAttribute("type", elementType);
	}

	@Override
	public String getDesc() throws XMLException {
		return getChildElementValue("desc");
	}

	@Override
	public String getLabel() throws XMLException {
		return getAttribute("label");
	}

	@Override
	public String getType() throws XMLException {
		return getAttribute("type");
	}

	@Override
	public String getVar() throws XMLException {
		return getAttribute("var");
	}

	@Override
	public boolean isRequired() throws XMLException {
		return getFirstChild("required") != null;
	}

	@Override
	public void setDesc(String desc) throws XMLException {
		setChildElementValue("desc", desc);
	}

	@Override
	public void setLabel(String label) throws XMLException {
		setAttribute("label", label);
	}

	@Override
	public void setRequired(boolean value) throws XMLException {
		Element b = getFirstChild("required");
		if (value == false && b != null)
			removeChild(b);
		else if (value == true && b == null) {
			b = new DefaultElement("required");
			addChild(b);
		}
	}

	@Override
	public void setVar(String var) throws XMLException {
		setAttribute("var", var);
	}

}