package tigase.jaxmpp.core.client;

import junit.framework.TestCase;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.PingModule;

public class ProcessorTest extends TestCase {

	private Processor processor;

	private MockWriter writer;

	public ProcessorTest() {
		SessionObject sessionObject = new DefaultSessionObject();
		this.writer = new MockWriter();

		XmppModulesManager xmppModulesManages = new XmppModulesManager();
		xmppModulesManages.register(new PingModule(sessionObject, writer));
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
