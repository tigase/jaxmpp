package tigase.jaxmpp.core.client.xml;

import java.util.Map;

public class ElementBuilder {

	public static final ElementBuilder create(String name) throws XMLException {
		Element e = ElementFactory.create(name);
		return new ElementBuilder(e);
	}

	public static final ElementBuilder create(String name, String xmlns) throws XMLException {
		Element e = ElementFactory.create(name);
		ElementBuilder eb = new ElementBuilder(e);
		eb.setXMLNS(xmlns);
		return eb;
	}

	private Element currentElement;

	private final Element rootElement;

	private ElementBuilder(Element rootElement) {
		this.rootElement = rootElement;
		this.currentElement = rootElement;
	}

	public ElementBuilder child(String name) throws XMLException {
		Element e = ElementFactory.create(name);
		currentElement.addChild(e);
		currentElement = e;
		return this;
	}

	public Element getElement() {
		return rootElement;
	}

	public ElementBuilder setAttribute(String key, String value) throws XMLException {
		currentElement.setAttribute(key, value);
		return this;
	}

	public ElementBuilder setAttributes(Map<String, String> attrs) throws XMLException {
		currentElement.setAttributes(attrs);
		return this;
	}

	public ElementBuilder setValue(String value) throws XMLException {
		currentElement.setValue(value);
		return this;
	}

	public ElementBuilder setXMLNS(String xmlns) throws XMLException {
		currentElement.setXMLNS(xmlns);
		return this;
	}

	public ElementBuilder up() throws XMLException {
		currentElement = currentElement.getParent();
		return this;
	}

}
