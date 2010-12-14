package tigase.jaxmpp.j2se.xml;

import java.util.List;
import java.util.Queue;

import junit.framework.TestCase;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.xml.DomBuilderHandler;
import tigase.xml.SimpleParser;
import tigase.xml.SingletonFactory;

public class J2seElementTest extends TestCase {

	private final SimpleParser parser = SingletonFactory.getParserInstance();

	private final Element createElement() throws XMLException {
		return new J2seElement(
				parse("<message to=\"romeo@example.net\" from=\"juliet@example.com/balcony\" type=\"chat\"><subject>I implore you!</subject><body>Wherefore art thou, Romeo?</body><thread>e0ffe42b28561960c6b12b944a092794b9683a38</thread><x xmlns=\"tigase\">tigase:offline</x></message>"));
	}

	public tigase.xml.Element parse(String data) {
		DomBuilderHandler domHandler = new DomBuilderHandler();
		parser.parse(domHandler, data.toCharArray(), 0, data.toCharArray().length);
		Queue<tigase.xml.Element> q = domHandler.getParsedElements();
		return q.element();
	}

	public void test01() throws Exception {
		J2seElement x = new J2seElement(parse("<iq to='x@y.z'><query/></iq>"));
		assertEquals("iq", x.getName());
		assertEquals("x@y.z", x.getAttribute("to"));
	}

	public void testGetAttribute() throws XMLException {
		final Element element = createElement();

		assertEquals("juliet@example.com/balcony", element.getAttribute("from"));
		assertEquals("romeo@example.net", element.getAttribute("to"));
	}

	public void testGetChildren() throws XMLException {
		final Element element = createElement();
		assertEquals("message", element.getName());

		List<Element> c = element.getChildren("subject");
		assertEquals(1, c.size());
		Element e = c.get(0);
		assertEquals("subject", e.getName());

		c = element.getChildrenNS("tigase");
		assertEquals(1, c.size());
		e = c.get(0);
		assertEquals("x", e.getName());

		Element c1 = element.getChildrenNS("x", "tigase");
		assertNotNull(c1);
		assertEquals("x", c1.getName());

	}

	public void testGetChildrenAfter() throws XMLException {
		final Element element = createElement();

		List<Element> c = element.getChildren("subject");
		assertEquals(1, c.size());
		Element e = c.get(0);
		Element body = element.getChildAfter(e);
		assertEquals("body", body.getName());
		assertEquals("Wherefore art thou, Romeo?", body.getValue());
	}

	public void testGetFirstChild() throws XMLException {
		final Element element = createElement();
		Element fc = element.getFirstChild();

		assertEquals("subject", fc.getName());
	}

	public void testGetNextSibling() throws XMLException {
		final Element element = createElement();

		List<Element> c = element.getChildren("subject");
		assertEquals(1, c.size());
		Element e = c.get(0);
		Element body = e.getNextSibling();
		assertEquals("body", body.getName());
		assertEquals("Wherefore art thou, Romeo?", body.getValue());
	}

	public void testGetXMLNS() throws XMLException {
		final Element element = createElement();

		assertNull(element.getXMLNS());

		Element c = element.getChildrenNS("x", "tigase");
		assertNotNull(c);
		assertEquals("x", c.getName());
		assertEquals("tigase", c.getXMLNS());

	}

}
