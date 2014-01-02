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
package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.PingModule;

public class ProcessorTest extends AbstractJaxmppTest {

	private Processor processor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		XmppModulesManager xmppModulesManages = new XmppModulesManager(context);
		xmppModulesManages.register(new PingModule(context));
		this.processor = new Processor(xmppModulesManages, context);
	}

	public void test01() throws XMLException {
		Element e = new DefaultElement("iq");
		Runnable r = processor.process(e);
		r.run();
		assertEquals(
				"<iq type=\"error\"><error code=\"501\" type=\"cancel\"><feature-not-implemented xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/></error></iq>",
				((MockWriter) context.getWriter()).poll().getAsString());
	}

	public void test02() throws XMLException {
		Element e = new DefaultElement("iq");
		e.setAttribute("type", "set");
		e.addChild(new DefaultElement("ping", null, "urn:xmpp:ping"));

		Runnable r = processor.process(e);
		r.run();
		assertEquals(
				"<iq type=\"error\"><error code=\"405\" type=\"cancel\"><not-allowed xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/></error></iq>",
				((MockWriter) context.getWriter()).poll().getAsString());
	}

	public void test03() throws XMLException {
		Element e = new DefaultElement("iq");
		e.setAttribute("to", "a@b.c");
		e.setAttribute("type", "get");
		e.addChild(new DefaultElement("ping", null, "urn:xmpp:ping"));

		Runnable r = processor.process(e);
		r.run();
		assertEquals("<iq from=\"a@b.c\" type=\"result\"/>", ((MockWriter) context.getWriter()).poll().getAsString());
	}

	public void test04() throws XMLException {
		Element e = new DefaultElement("iq");
		e.setAttribute("to", "a@b.c");
		e.setAttribute("type", "get");
		e.addChild(new DefaultElement("ping", null, "urn:xmpp:ping"));

		Runnable r = processor.process(e);
		r.run();
		assertEquals("<iq from=\"a@b.c\" type=\"result\"/>", ((MockWriter) context.getWriter()).poll().getAsString());
	}

}