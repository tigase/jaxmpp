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
	public List<Element> getChildrenNS(String name, String xmlns) throws XMLException {
		return element.getChildrenNS(name, xmlns);
	}

	@Override
	public Element getFirstChild() throws XMLException {
		return element.getFirstChild();
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
