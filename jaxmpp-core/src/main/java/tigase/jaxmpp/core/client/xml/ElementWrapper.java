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
package tigase.jaxmpp.core.client.xml;

import java.util.List;
import java.util.Map;

public class ElementWrapper implements Element {

	private final Element element;

	public ElementWrapper(Element element) {
		this.element = element;
	}

	@Override
	public Element addChild(Element child) throws XMLException {
		return element.addChild(child);
	}

	@Override
	public boolean equals(Object obj) {
		return this.element.equals(obj);
	}

	@Override
	public String getAsString() throws XMLException {
		return element.getAsString();
	}

	@Override
	public String getAttribute(String attName) throws XMLException {
		return element.getAttribute(attName);
	}

	@Override
	public Map<String, String> getAttributes() throws XMLException {
		return element.getAttributes();
	}

	@Override
	public Element getChildAfter(Element child) throws XMLException {
		return element.getChildAfter(child);
	}

	protected String getChildElementValue(final String elemName) throws XMLException {
		Element b = getFirstChild(elemName);
		return b == null ? null : b.getValue();
	}

	protected String getChildElementValue(final String elemName, final String xmlns) throws XMLException {
		Element b = getChildrenNS(elemName, xmlns);
		return b == null ? null : b.getValue();
	}

	@Override
	public List<Element> getChildren() throws XMLException {
		return element.getChildren();
	}

	@Override
	public List<Element> getChildren(String name) throws XMLException {
		return element.getChildren(name);
	}

	@Override
	public List<Element> getChildrenNS(String xmlns) throws XMLException {
		return element.getChildrenNS(xmlns);
	}

	@Override
	public Element getChildrenNS(String name, String xmlns) throws XMLException {
		return element.getChildrenNS(name, xmlns);
	}

	@Override
	public Element getFirstChild() throws XMLException {
		return element.getFirstChild();
	}

	public Element getFirstChild(String name) throws XMLException {
		List<Element> l = getChildren(name);
		return l != null && !l.isEmpty() ? l.get(0) : null;
	}

	@Override
	public String getName() throws XMLException {
		return element.getName();
	}

	@Override
	public Element getNextSibling() throws XMLException {
		return element.getNextSibling();
	}

	@Override
	public Element getParent() throws XMLException {
		return element.getParent();
	}

	@Override
	public String getValue() throws XMLException {
		return element.getValue();
	}

	@Override
	public String getXMLNS() throws XMLException {
		return element.getXMLNS();
	}

	@Override
	public int hashCode() {
		return this.element.hashCode();
	}

	@Override
	public void removeAttribute(String key) throws XMLException {
		element.removeAttribute(key);
	}

	@Override
	public void removeChild(Element child) throws XMLException {
		element.removeChild(child);
	}

	@Override
	public void setAttribute(String key, String value) throws XMLException {
		element.setAttribute(key, value);
	}

	@Override
	public void setAttributes(Map<String, String> attrs) throws XMLException {
		element.setAttributes(attrs);
	}

	protected void setChildElementValue(final String elemName, final String value) throws XMLException {
		Element b = getFirstChild(elemName);
		if (value == null && b != null)
			removeChild(b);
		else if (value != null && b == null) {
			b = new DefaultElement(elemName);
			addChild(b);
			b.setValue(value);
		} else if (value != null && b != null) {
			b.setValue(value);
		}
	}

	protected void setChildElementValue(final String elemName, final String xmlns, final String value) throws XMLException {
		Element b = getChildrenNS(elemName, xmlns);
		if (value == null && b != null)
			removeChild(b);
		else if (value != null && b == null) {
			b = new DefaultElement(elemName, null, xmlns);
			addChild(b);
			b.setValue(value);
		} else if (value != null && b != null) {
			b.setValue(value);
		}
	}

	@Override
	public void setParent(Element parent) throws XMLException {
		element.setParent(parent);
	}

	@Override
	public void setValue(String value) throws XMLException {
		element.setValue(value);
	}

	@Override
	public void setXMLNS(String xmlns) throws XMLException {
		element.setXMLNS(xmlns);
	}
}