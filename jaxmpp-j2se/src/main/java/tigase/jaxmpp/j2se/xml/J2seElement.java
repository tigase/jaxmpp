/*
 * J2seElement.java
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
package tigase.jaxmpp.j2se.xml;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementComparator;
import tigase.jaxmpp.core.client.xml.XMLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class J2seElement
		implements Element {

	private final tigase.xml.Element xmlElement;
	private J2seElement parent;

	public J2seElement(tigase.xml.Element xmlElement) {
		this(xmlElement, null);
	}

	private J2seElement(tigase.xml.Element xmlElement, J2seElement parent) {
		this.xmlElement = xmlElement;
		this.parent = parent;
	}

	@Override
	public Element addChild(Element child) throws XMLException {
		throw new RuntimeException("Not implemented in J2seElement");
	}

	@Override
	public Element findChild(String[] elemPath) throws XMLException {
		final tigase.xml.Element child = this.xmlElement.findChild(elemPath);
		if (child != null) {
			return new J2seElement(child);
		} else {
			return null;
		}
	}

	@Override
	public String getAsString() throws XMLException {
		return this.xmlElement.toString();
	}

	@Override
	public String getAttribute(String attName) throws XMLException {
		return this.xmlElement.getAttribute(attName);
	}

	@Override
	public Map<String, String> getAttributes() throws XMLException {
		return this.xmlElement.getAttributes();
	}

	@Override
	public void setAttributes(Map<String, String> attrs) throws XMLException {
		this.setAttributes(attrs);
	}

	@Override
	public Element getChildAfter(Element child) throws XMLException {
		int index = indexOf(child);
		if (index == -1) {
			throw new XMLException("Element not part of tree");
		}
		tigase.xml.Element n = this.xmlElement.getChildren().get(index + 1);
		return new J2seElement(n, this);
	}

	@Override
	public List<Element> getChildren() throws XMLException {
		ArrayList<Element> result = new ArrayList<Element>();
		List<tigase.xml.Element> x = this.xmlElement.getChildren();
		if (x != null) {
			for (tigase.xml.Element e : x) {
				result.add(new J2seElement(e, this));
			}
		}
		return result;
	}

	@Override
	public List<Element> getChildren(String name) throws XMLException {
		ArrayList<Element> result = new ArrayList<Element>();
		List<tigase.xml.Element> x = this.xmlElement.getChildren();
		if (x != null) {
			for (tigase.xml.Element e : x) {
				if (e != null && name.equals(e.getName())) {
					result.add(new J2seElement(e, this));
				}
			}
		}
		return result;
	}

	@Override
	public List<Element> getChildrenNS(String xmlns) throws XMLException {
		ArrayList<Element> result = new ArrayList<Element>();
		List<tigase.xml.Element> children = this.xmlElement.getChildren();
		if (children != null) {
			for (tigase.xml.Element e : children) {
				String x = e.getXMLNS();
				if (x != null && x.equals(xmlns)) {
					result.add(new J2seElement(e, this));
				}
			}
		}
		return result;
	}

	@Override
	public Element getChildrenNS(String name, String xmlns) throws XMLException {
		tigase.xml.Element e = this.xmlElement.getChild(name, xmlns);
		if (e != null) {
			return new J2seElement(e, this);
		}
		return null;
	}

	@Override
	public Element getFirstChild() throws XMLException {
		List<tigase.xml.Element> children = this.xmlElement.getChildren();
		if (children != null && children.size() > 0) {
			return new J2seElement(children.get(0), this);
		}
		return null;
	}

	@Override
	public Element getFirstChild(String name) throws XMLException {
		tigase.xml.Element child = this.xmlElement.getChild(name);
		return (child != null) ? new J2seElement(child, this) : null;
	}

	@Override
	public String getName() throws XMLException {
		return this.xmlElement.getName();
	}

	@Override
	public Element getNextSibling() throws XMLException {
		if (this.parent == null) {
			return null;
		}
		return this.parent.getChildAfter(this);
	}

	@Override
	public Element getParent() throws XMLException {
		return this.parent == null
			   ? null
			   : new J2seElement(this.parent.xmlElement, (J2seElement) this.parent.getParent());
	}

	@Override
	public void setParent(Element parent) throws XMLException {
		throw new RuntimeException("Not implemented in J2seElement");
	}

	@Override
	public String getValue() throws XMLException {
		return this.xmlElement.getCData();
	}

	@Override
	public void setValue(String value) throws XMLException {
		this.xmlElement.setCData(value);
	}

	@Override
	public String getXMLNS() throws XMLException {
		return getAttribute("xmlns");
	}

	@Override
	public void setXMLNS(String xmlns) throws XMLException {
		setAttribute("xmlns", xmlns);
	}

	private int indexOf(final Element child) {
		List<tigase.xml.Element> children = this.xmlElement.getChildren();
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				tigase.xml.Element cc = children.get(i);
				if (child instanceof J2seElement) {
					if (((J2seElement) child).xmlElement.equals(cc)) {
						return i;
					}
				} else if (cc instanceof tigase.xml.Element &&
						ElementComparator.equal(new J2seElement(cc, null), child)) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public void removeAttribute(String key) throws XMLException {
		this.xmlElement.removeAttribute(key);
	}

	@Override
	public void removeChild(Element child) throws XMLException {
		int index = indexOf(child);
		if (index != -1) {
			tigase.xml.Element z = this.xmlElement.getChildren().get(index);
			this.xmlElement.removeChild(z);
		}

	}

	@Override
	public void setAttribute(String key, String value) throws XMLException {
		this.xmlElement.setAttribute(key, value);
	}

}