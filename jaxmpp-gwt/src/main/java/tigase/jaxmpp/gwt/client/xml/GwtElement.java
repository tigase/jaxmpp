package tigase.jaxmpp.gwt.client.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class GwtElement implements Element {

	public static GwtElement parse(String data) {
		com.google.gwt.xml.client.Element e = XMLParser.parse(data).getDocumentElement();
		return new GwtElement(e);
	}

	private final com.google.gwt.xml.client.Element xmlElement;

	public GwtElement(com.google.gwt.xml.client.Element xmlElement) {
		this.xmlElement = xmlElement;
	}

	@Override
	public Element addChild(Element child) throws XMLException {
		com.google.gwt.xml.client.Element a = XMLParser.parse(child.toString()).getDocumentElement();
		this.xmlElement.appendChild(a);
		GwtElement c = new GwtElement(a);
		return c;
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
		HashMap<String, String> result = new HashMap<String, String>();
		for (int i = 0; i < this.xmlElement.getAttributes().getLength(); i++) {
			Node a = this.xmlElement.getAttributes().item(i);
			result.put(a.getNodeName(), a.getNodeValue());
		}
		return result;
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
			if (node instanceof Element) {
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
			if (node instanceof Element) {
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
			if (node instanceof Element) {
				final String x = ((Element) node).getAttribute("xmlns");
				GwtElement gpi = new GwtElement((com.google.gwt.xml.client.Element) node);
				if (x != null && xmlns.equals(gpi.getXMLNS())) {
					result.add(gpi);
				}
			}
		}
		return result;
	}

	@Override
	public List<Element> getChildrenNS(String name, String xmlns) throws XMLException {
		final ArrayList<Element> result = new ArrayList<Element>();
		NodeList nodes = this.xmlElement.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof Element) {
				final String x = ((Element) node).getAttribute("xmlns");
				GwtElement gpi = new GwtElement((com.google.gwt.xml.client.Element) node);
				if (x != null && x.equals(xmlns) && xmlns.equals(gpi.getXMLNS())) {
					result.add(gpi);
				}
			}
		}
		return result;
	}

	@Override
	public Element getFirstChild() throws XMLException {
		return new GwtElement((com.google.gwt.xml.client.Element) xmlElement.getFirstChild());
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
	public String getValue() throws XMLException {
		Node x = xmlElement.getFirstChild();
		if (x != null) {
			return x.getNodeValue();
		}
		return null;
	}

	@Override
	public String getXMLNS() throws XMLException {
		return this.xmlElement.getAttribute("xmlns");
	}

	private int indexOf(final Element child) {
		for (int i = 0; i < this.xmlElement.getChildNodes().getLength(); i++) {
			Node cc = this.xmlElement.getChildNodes().item(i);
			if (cc.equals(child))
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
		throw new XMLException("Unsupported in GwtElement");
	}

	@Override
	public void setAttribute(String key, String value) throws XMLException {
		this.xmlElement.setAttribute(key, value);
	}

	@Override
	public void setAttributes(Map<String, String> attrs) throws XMLException {
		for (Entry<String, String> a : attrs.entrySet()) {
			setAttribute(a.getKey(), a.getValue());
		}
	}

	@Override
	public void setParent(Element parent) throws XMLException {
		throw new XMLException("Unsupported in GwtElement");
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
	public void setXMLNS(String xmlns) throws XMLException {
		this.xmlElement.setAttribute("xmlns", xmlns);
	}
}
