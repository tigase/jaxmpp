/*
 * ProcessorTest.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
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

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.PingModule;

public class ProcessorTest
		extends AbstractJaxmppTest {

	private Processor processor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		XmppModulesManager xmppModulesManages = new XmppModulesManager(context);
		xmppModulesManages.register(new PingModule());
		this.processor = new Processor(xmppModulesManages, context);
	}

	public void test01() throws XMLException {
		Element e = ElementFactory.create("iq");
		Runnable r = processor.process(e);
		r.run();

		Element expected = ElementFactory.create("iq");
		expected.setAttribute("type", "error");
		Element er = expected.addChild(ElementFactory.create("error"));
		er.setAttribute("code", "501");
		er.setAttribute("type", "cancel");
		Element fni = er.addChild(ElementFactory.create("feature-not-implemented"));
		fni.setXMLNS("urn:ietf:params:xml:ns:xmpp-stanzas");

		assertEquals(expected, ((MockWriter) context.getWriter()).poll());

	}

	public void test02() throws XMLException {
		Element e = ElementFactory.create("iq");
		e.setAttribute("type", "set");
		e.addChild(ElementFactory.create("ping", null, "urn:xmpp:ping"));

		Runnable r = processor.process(e);
		r.run();

		Element expected = ElementFactory.create("iq");
		expected.setAttribute("type", "error");
		Element er = expected.addChild(ElementFactory.create("error"));
		er.setAttribute("code", "405");
		er.setAttribute("type", "cancel");
		Element fni = er.addChild(ElementFactory.create("not-allowed"));
		fni.setXMLNS("urn:ietf:params:xml:ns:xmpp-stanzas");

		assertEquals(expected, ((MockWriter) context.getWriter()).poll());
	}

	public void test03() throws XMLException {
		Element e = ElementFactory.create("iq");
		e.setAttribute("to", "a@b.c");
		e.setAttribute("type", "get");
		e.addChild(ElementFactory.create("ping", null, "urn:xmpp:ping"));

		Runnable r = processor.process(e);
		r.run();

		Element expected = ElementFactory.create("iq");
		expected.setAttribute("from", "a@b.c");
		expected.setAttribute("type", "result");

		assertEquals(expected, ((MockWriter) context.getWriter()).poll());
	}

	public void test04() throws XMLException {
		Element e = ElementFactory.create("iq");
		e.setAttribute("to", "a@b.c");
		e.setAttribute("type", "get");
		e.addChild(ElementFactory.create("ping", null, "urn:xmpp:ping"));

		Runnable r = processor.process(e);
		r.run();

		Element expected = ElementFactory.create("iq");
		expected.setAttribute("from", "a@b.c");
		expected.setAttribute("type", "result");

		assertEquals(expected, ((MockWriter) context.getWriter()).poll());
	}

}