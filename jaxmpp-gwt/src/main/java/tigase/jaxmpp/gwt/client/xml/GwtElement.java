/*
 * GwtElement.java
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
package tigase.jaxmpp.gwt.client.xml;

import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementComparator;
import tigase.jaxmpp.core.client.xml.XMLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GwtElement
		implements Element {

	private final com.google.gwt.xml.client.Element xmlElement;

	public static GwtElement parse(String data) {
		com.google.gwt.xml.client.Element e = XMLParser.parse(data).getDocumentElement();
		return new GwtElement(e);
	}

	public GwtElement(com.google.gwt.xml.client.Element xmlElement) {
		this.xmlElement = xmlElement;
	}

	@Override
	public Element addChild(Element child) throws XMLException {
		com.google.gwt.xml.client.Element a = XMLParser.parse(child.getAsString()).getDocumentElement();
		this.xmlElement.appendChild(a);
		GwtElement c = new GwtElement(a);
		return c;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof com.google.gwt.xml.client.Element) {
			return this.xmlElement.equals(obj);
		} else if (obj instanceof Element) {
			return ElementComparator.equal((Element) obj, this);
		} else {
			return false;
		}
	}

	@Override
	public Element findChild(String[] elemPath) throws XMLException {
		if (elemPath[0].isEmpty()) {
			final int len = elemPath.length - 1;
			String[] tmp = new String[len];
			System.arraycopy(elemPath, 1, tmp, 1, len);
			elemPath = tmp;
		}
		if (!elemPath[0].equals(getName())) {
			return null;
		}

		Element child = this;

		// we must start with 1 not 0 as 0 is name of parent element
		for (int i = 1; (i < elemPath.length) && (child != null); i++) {
			String str = elemPath[i];

			child = child.getFirstChild(str);
		}

		return child;
	}

	@Override
	public String getAsString() throws XMLException {
		// TODO
		return this.xmlElement.toString();
	}

	@Override
	public String getAttribute(String attName) throws XMLException {
		return this.xmlElement.getAttribute(attName);
	}

	@Override
	public Map<String, String> getAttributes() throws XMLException {
		HashMap<String, String> result = new HashMap<String, String>();
		for (int i = 0; i < this.xmlElement.getAttributes().getLength(); i++) {
			Node a = this.xmlElement.getAttributes().item(i);
			result.put(a.getNodeName(), a.getNodeValue());
		}
		return result;
	}

	@Override
	public void setAttributes(Map<String, String> attrs) throws XMLException {
		for (Entry<String, String> a : attrs.entrySet()) {
			setAttribute(a.getKey(), a.getValue());
		}
	}

	@Override
	public Element getChildAfter(Element child) throws XMLException {
		int index = indexOf(child);

		if (index == -1) {
			throw new XMLException("Element not part of tree");
		}
		Node n = this.xmlElement.getChildNodes().item(index + 1);
		return new GwtElement((com.google.gwt.xml.client.Element) n);
	}

	@Override
	public List<Element> getChildren() throws XMLException {
		NodeList nodes = this.xmlElement.getChildNodes();
		ArrayList<Element> result = new ArrayList<Element>();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof com.google.gwt.xml.client.Element) {
				GwtElement gpi = new GwtElement((com.google.gwt.xml.client.Element) node);
				result.add(gpi);
			}
		}
		return result;
	}

	@Override
	public List<Element> getChildren(String name) throws XMLException {
		final ArrayList<Element> result = new ArrayList<Element>();
		NodeList nodes = this.xmlElement.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof com.google.gwt.xml.client.Element) {
				GwtElement gpi = new GwtElement((com.google.gwt.xml.client.Element) node);
				if (name.equals(gpi.getName())) {
					result.add(gpi);
				}
			}
		}
		return result;
	}

	@Override
	public List<Element> getChildrenNS(String xmlns) throws XMLException {
		final ArrayList<Element> result = new ArrayList<Element>();
		NodeList nodes = this.xmlElement.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof com.google.gwt.xml.client.Element) {
				final String x = ((com.google.gwt.xml.client.Element) node).getAttribute("xmlns");
				GwtElement gpi = new GwtElement((com.google.gwt.xml.client.Element) node);
				if (x != null && xmlns.equals(gpi.getXMLNS())) {
					result.add(gpi);
				}
			}
		}
		return result;
	}

	@Override
	public Element getChildrenNS(String name, String xmlns) throws XMLException {
		NodeList nodes = this.xmlElement.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof com.google.gwt.xml.client.Element) {
				final String x = ((com.google.gwt.xml.client.Element) node).getNodeName();
				GwtElement gpi = new GwtElement((com.google.gwt.xml.client.Element) node);
				if (x != null && x.equals(name) && xmlns.equals(gpi.getXMLNS())) {
					return gpi;
				}
			}
		}
		return null;
	}

	@Override
	public Element getFirstChild() throws XMLException {
		NodeList nodes = this.xmlElement.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof com.google.gwt.xml.client.Element) {
				return new GwtElement((com.google.gwt.xml.client.Element) node);
			}
		}

		return null;
		// first child may not be Element it can be Node only!!
		// com.google.gwt.xml.client.Element c =
		// (com.google.gwt.xml.client.Element) xmlElement.getFirstChild();
		// return c == null ? null : new GwtElement(c);
	}

	@Override
	public Element getFirstChild(String name) throws XMLException {
		NodeList nodes = this.xmlElement.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof com.google.gwt.xml.client.Element && node.getNodeName().equals(name)) {
				return new GwtElement((com.google.gwt.xml.client.Element) node);
			}
		}
		return null;
	}

	@Override
	public String getName() throws XMLException {
		String n = this.xmlElement.getNodeName();
		return n;
	}

	@Override
	public Element getNextSibling() throws XMLException {
		return new GwtElement((com.google.gwt.xml.client.Element) this.xmlElement.getNextSibling());
	}

	@Override
	public Element getParent() throws XMLException {
		return new GwtElement((com.google.gwt.xml.client.Element) this.xmlElement.getParentNode());
	}

	@Override
	public void setParent(Element parent) throws XMLException {
		// throw new XMLException("Unsupported in GwtElement");
	}

	@Override
	public String getValue() throws XMLException {
		Node x = xmlElement.getFirstChild();
		if (x != null) {
			return x.getNodeValue();
		}
		return null;
	}

	@Override
	public void setValue(String value) throws XMLException {
		final NodeList nodes = xmlElement.getChildNodes();
		for (int index = 0; index < nodes.getLength(); index++) {
			final Node child = nodes.item(index);
			if (child.getNodeType() == Node.TEXT_NODE) {
				xmlElement.removeChild(child);
			}
		}
		xmlElement.appendChild(xmlElement.getOwnerDocument().createTextNode(value));
	}

	@Override
	public String getXMLNS() throws XMLException {
		return this.xmlElement.getAttribute("xmlns");
	}

	@Override
	public void setXMLNS(String xmlns) throws XMLException {
		this.xmlElement.setAttribute("xmlns", xmlns);
	}

	@Override
	public int hashCode() {
		return this.xmlElement.toString().hashCode();
	}

	private int indexOf(final Element child) {
		for (int i = 0; i < this.xmlElement.getChildNodes().getLength(); i++) {
			Node cc = this.xmlElement.getChildNodes().item(i);
			if (child instanceof GwtElement) {
				if (((GwtElement) child).xmlElement.equals(cc)) {
					return i;
				}
			} else if (cc instanceof com.google.gwt.xml.client.Element &&
					ElementComparator.equal(new GwtElement((com.google.gwt.xml.client.Element) cc), child)) {
				return i;
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
		// throw new XMLException("Unsupported in GwtElement");
	}

	@Override
	public void setAttribute(String key, String value) throws XMLException {
		this.xmlElement.setAttribute(key, value);
	}
}
