/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
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

import java.util.List;

import junit.framework.TestCase;

public class DefaultElementTest extends TestCase {

	private static final Element createElement() throws XMLException {
		Element message = ElementFactory.create("message", null, null);
		message.setAttribute("to", "romeo@example.net");
		message.setAttribute("from", "juliet@example.com/balcony");
		message.setAttribute("type", "chat");

		message.addChild(ElementFactory.create("subject", "I implore you!", null));
		message.addChild(ElementFactory.create("body", "Wherefore art thou, Romeo?", null));
		message.addChild(ElementFactory.create("thread", "e0ffe42b28561960c6b12b944a092794b9683a38", null));
		message.addChild(ElementFactory.create("x", "tigase:offline", "tigase"));

		return message;
	}

	public void testCreate() throws XMLException {
		final Element element = createElement();
		Element e1 = ElementFactory.create(element);
		assertEquals(
				"<message to=\"romeo@example.net\" from=\"juliet@example.com/balcony\" type=\"chat\"><subject>I implore you!</subject><body>Wherefore art thou, Romeo?</body><thread>e0ffe42b28561960c6b12b944a092794b9683a38</thread><x xmlns=\"tigase\">tigase:offline</x></message>",
				e1.getAsString());

		e1 = DefaultElement.create(element, 0);
		assertEquals("<message to=\"romeo@example.net\" from=\"juliet@example.com/balcony\" type=\"chat\"/>", e1.getAsString());

		e1 = DefaultElement.create(element, 1);
		assertEquals(
				"<message to=\"romeo@example.net\" from=\"juliet@example.com/balcony\" type=\"chat\"><subject>I implore you!</subject><body>Wherefore art thou, Romeo?</body><thread>e0ffe42b28561960c6b12b944a092794b9683a38</thread><x xmlns=\"tigase\">tigase:offline</x></message>",
				e1.getAsString());
	}

	public void testGetAsString00() throws XMLException {
		Element element = createElement();
		try {
			String t = element.getAsString();
			assertEquals(
					"<message to=\"romeo@example.net\" from=\"juliet@example.com/balcony\" type=\"chat\"><subject>I implore you!</subject><body>Wherefore art thou, Romeo?</body><thread>e0ffe42b28561960c6b12b944a092794b9683a38</thread><x xmlns=\"tigase\">tigase:offline</x></message>",
					t);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	public void testGetAsString01() throws XMLException {
		Element element = ElementFactory.create("iq", null, null);
		assertEquals("<iq/>", element.getAsString());
	}

	public void testGetAsString02() throws XMLException {
		Element element = ElementFactory.create("iq", "a", null);
		assertEquals("<iq>a</iq>", element.getAsString());
	}

	public void testGetAsString03() throws XMLException {
		Element element = ElementFactory.create("iq", "a", "b");
		assertEquals("<iq xmlns=\"b\">a</iq>", element.getAsString());
	}

	public void testGetAsString04() throws XMLException {
		Element element = ElementFactory.create("iq", null, "b");
		assertEquals("<iq xmlns=\"b\"/>", element.getAsString());
	}

	public void testGetAsString05() throws XMLException {
		Element element = ElementFactory.create("iq", null, "b");
		element.addChild(ElementFactory.create("query", null, "d"));
		assertEquals("<iq xmlns=\"b\"><query xmlns=\"d\"/></iq>", element.getAsString());
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