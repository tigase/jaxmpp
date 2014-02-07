package tigase.jaxmpp.core.client.xml;

public class ElementFactory {

	public static Element create(final Element src) throws XMLException {
		return DefaultElement.create(src, -1);
	}

	public static Element create(final String name) throws XMLException {
		return new DefaultElement(name);
	}

	public static Element create(String name, String value, String xmlns) throws XMLException {
		return new DefaultElement(name, value, xmlns);
	}

	private ElementFactory() {
	}

}
