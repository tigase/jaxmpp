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

import junit.framework.TestCase;
import tigase.jaxmpp.core.client.observer.DefaultObservable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.PingModule;

public class ProcessorTest extends TestCase {

	private Processor processor;

	private MockWriter writer;

	public ProcessorTest() {
		MockSessionObject sessionObject = new MockSessionObject();
		this.writer = new MockWriter(sessionObject);

		DefaultObservable observable = new DefaultObservable();
		XmppModulesManager xmppModulesManages = new XmppModulesManager(observable, writer);
		xmppModulesManages.register(new PingModule(observable, sessionObject, writer));
		this.processor = new Processor(xmppModulesManages, sessionObject, writer);
	}

	public void test01() throws XMLException {
		Element e = new DefaultElement("iq");
		Runnable r = processor.process(e);
		r.run();
		assertEquals(
				"<iq type=\"error\"><error code=\"501\" type=\"cancel\"><feature-not-implemented xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/></error></iq>",
				writer.poll().getAsString());
	}

	public void test02() throws XMLException {
		Element e = new DefaultElement("iq");
		e.setAttribute("type", "set");
		e.addChild(new DefaultElement("ping", null, "urn:xmpp:ping"));

		Runnable r = processor.process(e);
		r.run();
		assertEquals(
				"<iq type=\"error\"><error code=\"405\" type=\"cancel\"><not-allowed xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/></error></iq>",
				writer.poll().getAsString());
	}

	public void test03() throws XMLException {
		Element e = new DefaultElement("iq");
		e.setAttribute("to", "a@b.c");
		e.setAttribute("type", "get");
		e.addChild(new DefaultElement("ping", null, "urn:xmpp:ping"));

		Runnable r = processor.process(e);
		r.run();
		assertEquals("<iq from=\"a@b.c\" type=\"result\"/>", writer.poll().getAsString());
	}

	public void test04() throws XMLException {
		Element e = new DefaultElement("iq");
		e.setAttribute("to", "a@b.c");
		e.setAttribute("type", "get");
		e.addChild(new DefaultElement("ping", null, "urn:xmpp:ping"));

		Runnable r = processor.process(e);
		r.run();
		assertEquals("<iq from=\"a@b.c\" type=\"result\"/>", writer.poll().getAsString());
	}

}