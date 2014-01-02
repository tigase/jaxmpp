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

import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public class ResponseManagerTest extends AbstractJaxmppTest {

	private final ResponseManager rm = new ResponseManager();

	public void test01() {
		try {
			Element es = new DefaultElement("iq");
			es.setAttribute("id", "1");
			es.setAttribute("type", "set");
			es.setAttribute("to", "a@b.c");

			Element er = new DefaultElement("iq");
			er.setAttribute("type", "result");
			er.setAttribute("id", "1");
			er.setAttribute("from", "a@b.c");

			rm.registerResponseHandler(es, null, new AsyncCallback() {

				@Override
				public void onError(Stanza responseStanza, ErrorCondition error) {
					fail();
				}

				@Override
				public void onSuccess(Stanza responseStanza) throws JaxmppException {
					assertEquals("1", responseStanza.getAttribute("id"));
					assertEquals("a@b.c", responseStanza.getAttribute("from"));

					Element es = new DefaultElement("response");
					context.getWriter().write(es);
				}

				@Override
				public void onTimeout() {
					fail();
				}
			});

			Runnable r = rm.getResponseHandler(er, context.getWriter(), null);

			r.run();

			assertEquals("response", ((MockWriter) context.getWriter()).poll().getName());

		} catch (JaxmppException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}

	}

	public void test02() {
		try {
			Element es = new DefaultElement("iq");
			es.setAttribute("id", "1");
			es.setAttribute("type", "set");
			es.setAttribute("to", "a@b.c");

			Element er = new DefaultElement("iq");
			er.setAttribute("type", "error");
			er.setAttribute("id", "1");
			er.setAttribute("from", "a@b.c");

			Element e1 = new DefaultElement("error");
			e1.setAttribute("type", "wait");
			er.addChild(e1);

			Element e2 = new DefaultElement("internal-server-error", null, "urn:ietf:params:xml:ns:xmpp-stanzas");
			e1.addChild(e2);

			rm.registerResponseHandler(es, null, new AsyncCallback() {

				@Override
				public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
					assertEquals(ErrorCondition.internal_server_error, error);
					assertEquals("1", responseStanza.getAttribute("id"));
					assertEquals("a@b.c", responseStanza.getAttribute("from"));

					Element es = new DefaultElement("response");
					context.getWriter().write(es);
				}

				@Override
				public void onSuccess(Stanza responseStanza) throws XMLException {
					fail();
				}

				@Override
				public void onTimeout() {
					fail();
				}
			});

			Runnable r = rm.getResponseHandler(er, context.getWriter(), null);

			r.run();

			assertEquals("response", ((MockWriter) context.getWriter()).poll().getName());

		} catch (JaxmppException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}

	}
}