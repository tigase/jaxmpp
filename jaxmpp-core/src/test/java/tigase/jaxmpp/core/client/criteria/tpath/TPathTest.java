/*
 * TPathTest.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.core.client.criteria.tpath;

import org.junit.Test;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import static org.junit.Assert.*;

public class TPathTest {

	private final IQ iq;

	private final TPath tpath = new TPath();

	public TPathTest() throws JaxmppException {
		this.iq = IQ.create();
		iq.setTo(JID.jidInstance("a@b.c"));
		iq.setType(StanzaType.set);
		iq.setAttribute("from", "wojtas@wp.pl");
		final Element pubsub = ElementFactory.create("pubsub", null, "a:b");
		iq.addChild(pubsub);
		final Element publish = ElementFactory.create("publish");
		publish.setAttribute("node", "123");
		pubsub.addChild(publish);
		Element item = ElementFactory.create("item");
		item.setValue("x");
		item.setAttribute("id", "345");
		publish.addChild(item);
		item = ElementFactory.create("item");
		item.setAttribute("id", "456");
		publish.addChild(item);
		item = ElementFactory.create("item");
		item.setAttribute("id", "567");
		publish.addChild(item);
	}

	@Test
	public void testEvaluate() {
		try {
			assertEquals("set", tpath.compile("/*/attr('type')").evaluateAsArray(iq).get(0));
			assertEquals("wojtas@wp.pl", tpath.compile("/*/attr('from')").evaluateAsArray(iq).get(0));
			assertTrue(tpath.compile("/x/").evaluateAsArray(iq).isEmpty());
			assertArrayEquals(new String[]{"345", "456", "567"},
							  tpath.compile("/*[@type='set']/pubsub/publish/item/attr('id')")
									  .evaluateAsArray(iq)
									  .toArray(new String[]{}));
			assertArrayEquals(new String[]{"x"}, tpath.compile("/*[@type='set']/pubsub/publish/item/value()")
					.evaluateAsArray(iq)
					.toArray(new String[]{}));

			assertEquals("x", tpath.compile("/*[@type='set']/pubsub/publish/item/value()").evaluate(iq));
			assertNull(tpath.compile("/*[@type='get']/pubsub/publish/item/value()").evaluate(iq));

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}