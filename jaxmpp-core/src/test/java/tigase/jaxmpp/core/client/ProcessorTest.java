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
		SessionObject sessionObject = new SessionObject() {

			@Override
			public <T> T getProperty(String key) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Runnable getResponseHandler(Element element, PacketWriter writer, SessionObject sessionObject)
					throws XMLException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Element getStreamFeatures() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String registerResponseHandler(Element stanza, AsyncCallback callback) throws XMLException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setProperty(String key, Object value) {
				// TODO Auto-generated method stub

			}

			@Override
			public void setStreamFeatures(Element element) {
				// TODO Auto-generated method stub

			}
		};
		XmppModulesManager xmppModulesManages = new XmppModulesManager();
		xmppModulesManages.register(new PingModule());
		this.writer = new MockWriter();
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

		System.out.println(e.getAsString());
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

		System.out.println(e.getAsString());
		Runnable r = processor.process(e);
		r.run();
		assertEquals("<iq from=\"a@b.c\" type=\"result\"/>", writer.poll().getAsString());
	}

	public void test04() throws XMLException {
		Element e = new DefaultElement("iq");
		e.setAttribute("to", "a@b.c");
		e.setAttribute("type", "get");
		e.addChild(new DefaultElement("ping", null, "urn:xmpp:ping"));

		System.out.println(e.getAsString());
		Runnable r = processor.process(e);
		r.run();
		assertEquals("<iq from=\"a@b.c\" type=\"result\"/>", writer.poll().getAsString());
	}

}
