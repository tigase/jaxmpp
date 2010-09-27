package tigase.jaxmpp.gwt.client.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tigase.jaxmpp.core.client.xml.Element;

import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class GwtElement implements Element {

	@Override
	public String toString() {
		return this.xmlElement.toString();
	}

	private final com.google.gwt.xml.client.Element xmlElement;

	public GwtElement(com.google.gwt.xml.client.Element xmlElement) {
		this.xmlElement = xmlElement;
	}

	public static GwtElement parse(String data) {
		com.google.gwt.xml.client.Element e = XMLParser.parse(data).getDocumentElement();
		return new GwtElement(e);
	}

	@Override
	public Element addChild(Element child) {
		com.google.gwt.xml.client.Element a = XMLParser.parse(child.toString()).getDocumentElement();
		this.xmlElement.appendChild(a);
		return new GwtElement(a);
	}

	public Element addChild(String nodeName, String xmlns) {
		final com.google.gwt.xml.client.Element child = xmlElement.getOwnerDocument().createElement(nodeName);
		if (xmlns != null)
			child.setAttribute("xmlns", xmlns);
		xmlElement.appendChild(child);
		return new GwtElement(child);
	}

	@Override
	public void addChildren(Collection<Element> children) {
		for (Element element : children) {
			addChild(element);
		}
	}

	@Override
	public String getAttribute(String attName) {
		return this.xmlElement.getAttribute(attName);
	}

	@Override
	public Map<String, String> getAttributes() {
		HashMap<String, String> result = new HashMap<String, String>();
		for (int i = 0; i < this.xmlElement.getAttributes().getLength(); i++) {
			Node a = this.xmlElement.getAttributes().item(i);
			result.put(a.getNodeName(), a.getNodeValue());
		}
		return result;
	}

	@Override
	public String getCData() {
		Node x = xmlElement.getFirstChild();
		if (x != null) {
			return x.getNodeValue();
		}
		return null;
	}

	@Override
	public Element getChild(String name, String childXmlns) {
		NodeList nodes = this.xmlElement.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof Element) {
				final String x = ((Element) node).getAttribute("xmlns");
				GwtElement gpi = new GwtElement((com.google.gwt.xml.client.Element) node);
				if (x != null && x.equals(childXmlns) && name.equals(gpi.getName())) {
					return gpi;
				}
			}
		}
		return null;
	}

	@Override
	public List<Element> getChildren() {
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
	public String getName() {
		String n = this.xmlElement.getNodeName();
		return n;
	}

	@Override
	public String getXMLNS() {
		return getAttribute("xmlns");
	}

	@Override
	public void removeAttribute(String key) {
		this.xmlElement.removeAttribute(key);
	}

	@Override
	public void removeChild(Element child) {
		if (child instanceof GwtElement) {
			this.xmlElement.removeChild(((GwtElement) child).xmlElement);
		} else {
			Element x = getChild(child.getName(), child.getXMLNS());
			this.xmlElement.removeChild(((GwtElement) x).xmlElement);
		}
	}

	@Override
	public void setAttribute(String key, String value) {
		this.xmlElement.setAttribute(key, value);

	}

	@Override
	public void setAttributes(Map<String, String> attrs) {
		for (Entry<String, String> a : attrs.entrySet()) {
			setAttribute(a.getKey(), a.getValue());
		}
	}

	@Override
	public void setCData(String cData) {
		final NodeList nodes = xmlElement.getChildNodes();
		for (int index = 0; index < nodes.getLength(); index++) {
			final Node child = nodes.item(index);
			if (child.getNodeType() == Node.TEXT_NODE) {
				xmlElement.removeChild(child);
			}
		}
		xmlElement.appendChild(xmlElement.getOwnerDocument().createTextNode(cData));
	}

	@Override
	public void setDefXMLNS(String xmlns) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setXMLNS(String xmlns) {
		setAttribute("xmlns", xmlns);
	}

}
