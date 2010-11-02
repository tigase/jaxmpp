package tigase.jaxmpp.core.client.xml;

import java.util.List;
import java.util.Map;

public class ElementWrapper implements Element {

	private final Element element;

	public ElementWrapper(Element element) {
		this.element = element;
	}

	public Element addChild(Element child) throws XMLException {
		return element.addChild(child);
	}

	@Override
	public boolean equals(Object obj) {
		return this.element.equals(obj);
	}

	public String getAsString() throws XMLException {
		return element.getAsString();
	}

	public String getAttribute(String attName) throws XMLException {
		return element.getAttribute(attName);
	}

	public Map<String, String> getAttributes() throws XMLException {
		return element.getAttributes();
	}

	public Element getChildAfter(Element child) throws XMLException {
		return element.getChildAfter(child);
	}

	public List<Element> getChildren() throws XMLException {
		return element.getChildren();
	}

	public List<Element> getChildren(String name) throws XMLException {
		return element.getChildren(name);
	}

	public List<Element> getChildrenNS(String xmlns) throws XMLException {
		return element.getChildrenNS(xmlns);
	}

	public List<Element> getChildrenNS(String name, String xmlns) throws XMLException {
		return element.getChildrenNS(name, xmlns);
	}

	public Element getFirstChild() throws XMLException {
		return element.getFirstChild();
	}

	public String getName() throws XMLException {
		return element.getName();
	}

	public Element getNextSibling() throws XMLException {
		return element.getNextSibling();
	}

	public Element getParent() throws XMLException {
		return element.getParent();
	}

	public String getValue() throws XMLException {
		return element.getValue();
	}

	public String getXMLNS() throws XMLException {
		return element.getXMLNS();
	}

	@Override
	public int hashCode() {
		return this.element.hashCode();
	}

	public void removeAttribute(String key) throws XMLException {
		element.removeAttribute(key);
	}

	public void removeChild(Element child) throws XMLException {
		element.removeChild(child);
	}

	public void setAttribute(String key, String value) throws XMLException {
		element.setAttribute(key, value);
	}

	public void setAttributes(Map<String, String> attrs) throws XMLException {
		element.setAttributes(attrs);
	}

	public void setParent(Element parent) throws XMLException {
		element.setParent(parent);
	}

	public void setValue(String value) throws XMLException {
		element.setValue(value);
	}

	public void setXMLNS(String xmlns) throws XMLException {
		element.setXMLNS(xmlns);
	}
}
