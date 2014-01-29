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
package tigase.jaxmpp.core.client.xml;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import tigase.jaxmpp.core.client.xmpp.utils.EscapeUtils;

/**
 * Default implementation of XML Element object. This class should be used every
 * time when new XML Element is created.
 */
public class DefaultElement implements Element {

	public final static Element create(final Element src) throws XMLException {
		return create(src, -1);
	}

	public final static DefaultElement create(final Element src, int deep) throws XMLException {
		final DefaultElement result = new DefaultElement(src.getName(), src.getValue(), src.getXMLNS());
		result.setAttributes(src.getAttributes());
		if (deep != 0)
			for (Element e : src.getChildren()) {
				DefaultElement c = create(e, deep - 1);
				c.parent = result;
				result.children.add(c);
			}

		return result;
	}

	private Map<String, String> attributes;

	private LinkedList<Element> children;

	private String name;

	private Element parent;

	private String value;

	private String xmlns;

	public DefaultElement(String name) {
		this(name, null, null);
	}

	public DefaultElement(String name, String value, String xmlns) {
		this.name = name;
		this.value = value;
		this.xmlns = xmlns;

		parent = null;
		children = new LinkedList<Element>();
		attributes = new HashMap<String, String>();
	}

	@Override
	public Element addChild(Element child) throws XMLException {
		child.setParent(this);
		synchronized (children) {
			children.add(child);
		}
		return child;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Element))
			return false;
		if (obj instanceof Element)
			return ElementComparator.equal((Element) obj, this);
		else
			return false;
	}

	@Override
	public String getAsString() throws XMLException {
		StringBuilder builder = new StringBuilder();
		builder.append('<');
		builder.append(name);
		if (xmlns != null && (parent == null || parent.getXMLNS() == null || !parent.getXMLNS().equals(xmlns))) {
			builder.append(' ');
			builder.append("xmlns=\"");
			builder.append(EscapeUtils.escape(xmlns));
			builder.append('"');
		}

		synchronized (attributes) {
			for (Map.Entry<String, String> attr : attributes.entrySet()) {
				builder.append(' ');
				builder.append(attr.getKey());
				builder.append("=\"");
				builder.append(EscapeUtils.escape(attr.getValue()));
				builder.append('"');
			}
		}
		synchronized (children) {
			if (children.isEmpty() && value == null) {
				builder.append('/');
			}
			builder.append('>');
			for (Element element : children) {
				builder.append(element.getAsString());
			}
			if (value != null)
				builder.append(EscapeUtils.escape(value));
			if (!(children.isEmpty() && value == null)) {
				builder.append("</");
				builder.append(name);
				builder.append('>');
			}
		}
		return builder.toString();
	}

	@Override
	public String getAttribute(String attName) throws XMLException {
		if (attName.equals("xmlns"))
			return getXMLNS();
		else
			synchronized (attributes) {
				return attributes.get(attName);
			}
	}

	@Override
	public Map<String, String> getAttributes() throws XMLException {
		return attributes;
	}

	@Override
	public Element getChildAfter(Element child) throws XMLException {
		synchronized (children) {
			int index = children.indexOf(child);

			if (index == -1) {
				throw new XMLException("Element not part of tree");
			}

			return children.get(index + 1);
		}
	}

	@Override
	public List<Element> getChildren() throws XMLException {
		return children;
	}

	@Override
	public List<Element> getChildren(String name) throws XMLException {
		List<Element> retval = new LinkedList<Element>();

		synchronized (children) {
			for (Element element : children) {
				if (element.getName().equals(name)) {
					retval.add(element);
				}
			}
		}

		return retval;
	}

	@Override
	public List<Element> getChildrenNS(String xmlns) throws XMLException {
		List<Element> retval = new LinkedList<Element>();

		synchronized (children) {
			for (Element element : children) {
				String x = element.getXMLNS();
				if (x != null && x.equals(xmlns)) {
					retval.add(element);
				}
			}
		}

		return retval;
	}

	@Override
	public Element getChildrenNS(String name, String xmlns) throws XMLException {
		synchronized (children) {
			for (Element element : children) {
				if (element.getName().equals(name) && element.getXMLNS().equals(xmlns)) {
					return element;
				}
			}
		}
		return null;
	}

	@Override
	public Element getFirstChild() throws XMLException {
		synchronized (children) {
			if (!children.isEmpty())
				return children.getFirst();
			else
				return null;
		}
	}

	@Override
	public String getName() throws XMLException {
		return name;
	}

	@Override
	public Element getNextSibling() throws XMLException {
		return parent.getChildAfter(this);
	}

	@Override
	public Element getParent() throws XMLException {
		return parent;
	}

	@Override
	public String getValue() throws XMLException {
		return value;
	}

	@Override
	public String getXMLNS() throws XMLException {
		if (xmlns == null && parent != null) {
			xmlns = parent.getXMLNS();
		}
		return xmlns;
	}

	@Override
	public int hashCode() {
		try {
			return getAsString().hashCode();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void removeAttribute(String key) throws XMLException {
		synchronized (attributes) {
			attributes.remove(key);
		}
	}

	@Override
	public void removeChild(Element child) throws XMLException {
		synchronized (children) {
			children.remove(child);
		}
	}

	@Override
	public void setAttribute(String key, String value) throws XMLException {
		if (key == null || value == null)
			return;
		synchronized (attributes) {
			attributes.put(key, value);
		}
	}

	@Override
	public void setAttributes(Map<String, String> attrs) throws XMLException {
		if (attrs == null)
			return;
		synchronized (attributes) {
			attributes.putAll(attrs);
		}
	}

	@Override
	public void setParent(Element parent) throws XMLException {
		// TODO This is specified in std. Should we support it?
		if (this.parent != null) {
			throw new XMLException("Illegal action, moving child from another tree");
		}

		this.parent = parent;
	}

	@Override
	public void setValue(String value) throws XMLException {
		synchronized (children) {
			if (!children.isEmpty()) {
				throw new XMLException("Unsupported mixed Element with children and value");
			}
		}
		this.value = value;
	}

	@Override
	public void setXMLNS(String xmlns) throws XMLException {
		this.xmlns = xmlns;
	}

}