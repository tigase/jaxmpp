package tigase.jaxmpp.core.client;

import junit.framework.TestCase;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public class ResponseManagerTest extends TestCase {

	private final ResponseManager rm = new ResponseManager();

	private final MockWriter writer = new MockWriter();

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

			rm.registerResponseHandler(es, new AsyncCallback() {

				@Override
				public void onError(Stanza responseStanza, ErrorCondition error) {
					fail();
				}

				@Override
				public void onSuccess(Stanza responseStanza) throws XMLException {
					assertEquals("1", responseStanza.getAttribute("id"));
					assertEquals("a@b.c", responseStanza.getAttribute("from"));

					Element es = new DefaultElement("response");
					writer.write(es);
				}

				@Override
				public void onTimeout() {
					fail();
				}
			});

			Runnable r = rm.getResponseHandler(er, writer, null);

			r.run();

			assertEquals("response", writer.poll().getName());

		} catch (XMLException e1) {
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

			rm.registerResponseHandler(es, new AsyncCallback() {

				@Override
				public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
					assertEquals(ErrorCondition.internal_server_error, error);
					assertEquals("1", responseStanza.getAttribute("id"));
					assertEquals("a@b.c", responseStanza.getAttribute("from"));

					Element es = new DefaultElement("response");
					writer.write(es);
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

			Runnable r = rm.getResponseHandler(er, writer, null);

			r.run();

			assertEquals("response", writer.poll().getName());

		} catch (XMLException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}

	}
}
