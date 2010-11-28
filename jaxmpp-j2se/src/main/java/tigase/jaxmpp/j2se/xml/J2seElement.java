package tigase.jaxmpp.j2se.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementComparator;
import tigase.jaxmpp.core.client.xml.XMLException;

public class J2seElement implements Element {

	private J2seElement parent;

	private final tigase.xml.Element xmlElement;

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
		for (tigase.xml.Element e : this.xmlElement.getChildren()) {
			result.add(new J2seElement(e, this));
		}
		return result;
	}

	@Override
	public List<Element> getChildren(String name) throws XMLException {
		ArrayList<Element> result = new ArrayList<Element>();
		List<tigase.xml.Element> x = this.xmlElement.getChildren();
		if (x != null)
			for (tigase.xml.Element e : x) {
				if (e != null && name.equals(e.getName()))
					result.add(new J2seElement(e, this));
			}
		return result;
	}

	@Override
	public List<Element> getChildrenNS(String xmlns) throws XMLException {
		ArrayList<Element> result = new ArrayList<Element>();
		for (tigase.xml.Element e : this.xmlElement.getChildren()) {
			String x = e.getXMLNS();
			if (x != null && x.equals(xmlns))
				result.add(new J2seElement(e, this));
		}
		return result;
	}

	@Override
	public List<Element> getChildrenNS(String name, String xmlns) throws XMLException {
		ArrayList<Element> result = new ArrayList<Element>();
		tigase.xml.Element e = this.xmlElement.getChild(name, xmlns);
		if (e != null)
			result.add(new J2seElement(e, this));
		return result;
	}

	@Override
	public Element getFirstChild() throws XMLException {
		List<tigase.xml.Element> children = this.xmlElement.getChildren();
		if (children != null && children.size() > 0)
			return new J2seElement(children.get(0), this);
		return null;
	}

	@Override
	public String getName() throws XMLException {
		return this.xmlElement.getName();
	}

	@Override
	public Element getNextSibling() throws XMLException {
		if (this.parent == null)
			return null;
		return this.parent.getChildAfter(this);
	}

	@Override
	public Element getParent() throws XMLException {
		return this.parent == null ? null : new J2seElement(this.parent.xmlElement, (J2seElement) this.parent.getParent());
	}

	@Override
	public String getValue() throws XMLException {
		return this.xmlElement.getCData();
	}

	@Override
	public String getXMLNS() throws XMLException {
		return getAttribute("xmlns");
	}

	private int indexOf(final Element child) {
		List<tigase.xml.Element> children = this.xmlElement.getChildren();
		if (children != null)
			for (int i = 0; i < children.size(); i++) {
				tigase.xml.Element cc = children.get(i);
				if (child instanceof J2seElement) {
					if (((J2seElement) child).xmlElement.equals(cc))
						return i;
				} else if (cc instanceof tigase.xml.Element && ElementComparator.equal(new J2seElement(cc, null), child))
					return i;
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
		if (index != -1)
			this.xmlElement.getChildren().remove(index);

	}

	@Override
	public void setAttribute(String key, String value) throws XMLException {
		this.xmlElement.setAttribute(key, value);
	}

	@Override
	public void setAttributes(Map<String, String> attrs) throws XMLException {
		this.setAttributes(attrs);
	}

	@Override
	public void setParent(Element parent) throws XMLException {
		throw new RuntimeException("Not implemented in J2seElement");
	}

	@Override
	public void setValue(String value) throws XMLException {
		this.xmlElement.setCData(value);
	}

	@Override
	public void setXMLNS(String xmlns) throws XMLException {
		setAttribute("xmlns", xmlns);
	}

}
