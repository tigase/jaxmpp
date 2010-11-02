package tigase.jaxmpp.core.client.xml;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DefaultElement implements Element {

	public final static Element create(final Element src) throws XMLException {
		return create(src, -1);
	}

	public final static Element create(final Element src, int deep) throws XMLException {
		final DefaultElement result = new DefaultElement(src.getName(), src.getValue(), src.getXMLNS());
		result.setAttributes(src.getAttributes());
		result.parent = src.getParent();
		if (deep != 0)
			for (Element e : src.getChildren()) {
				result.children.add(create(e, deep - 1));
			}

		return result;
	}

	private Map<String, String> attributes;

	private LinkedList<Element> children;

	private String name;

	private Element parent;

	private String value;

	private String xmlns;

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
		children.add(child);

		return child;
	}

	@Override
	public boolean equals(Object obj) {
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
			builder.append(xmlns);
			builder.append('"');
		}

		for (Map.Entry<String, String> attr : attributes.entrySet()) {
			builder.append(' ');
			builder.append(attr.getKey());
			builder.append("=\"");
			builder.append(attr.getValue());
			builder.append('"');
		}

		if (children.isEmpty() && value == null) {
			builder.append('/');
		}
		builder.append('>');
		for (Element element : children) {
			builder.append(element.getAsString());
		}
		if (value != null)
			builder.append(value);
		if (!(children.isEmpty() && value == null)) {
			builder.append("</");
			builder.append(name);
			builder.append('>');
		}

		return builder.toString();
	}

	@Override
	public String getAttribute(String attName) throws XMLException {
		return attributes.get(attName);
	}

	@Override
	public Map<String, String> getAttributes() throws XMLException {
		return attributes;
	}

	@Override
	public Element getChildAfter(Element child) throws XMLException {
		int index = children.indexOf(child);

		if (index == -1) {
			throw new XMLException("Element not part of tree");
		}

		return children.get(index + 1);
	}

	@Override
	public List<Element> getChildren() throws XMLException {
		return children;
	}

	@Override
	public List<Element> getChildren(String name) throws XMLException {
		List<Element> retval = new LinkedList<Element>();

		for (Element element : children) {
			if (element.getName().equals(name)) {
				retval.add(element);
			}
		}

		return retval;
	}

	@Override
	public List<Element> getChildrenNS(String xmlns) throws XMLException {
		List<Element> retval = new LinkedList<Element>();

		for (Element element : children) {
			String x = element.getXMLNS();
			if (x != null && x.equals(xmlns)) {
				retval.add(element);
			}
		}

		return retval;
	}

	@Override
	public List<Element> getChildrenNS(String name, String xmlns) throws XMLException {
		List<Element> retval = new LinkedList<Element>();

		for (Element element : children) {
			if (element.getName().equals(name) && element.getXMLNS().equals(xmlns)) {
				retval.add(element);
			}
		}

		return retval;
	}

	@Override
	public Element getFirstChild() throws XMLException {
		return children.getFirst();
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
		attributes.remove(key);
	}

	@Override
	public void removeChild(Element child) throws XMLException {
		children.remove(child);
	}

	@Override
	public void setAttribute(String key, String value) throws XMLException {
		attributes.put(key, value);
	}

	@Override
	public void setAttributes(Map<String, String> attrs) throws XMLException {
		attributes.putAll(attrs);
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
		if (!children.isEmpty()) {
			throw new XMLException("Unsupported mixed Element with children and value");
		}
		this.value = value;
	}

	@Override
	public void setXMLNS(String xmlns) throws XMLException {
		this.xmlns = xmlns;
	}
}
