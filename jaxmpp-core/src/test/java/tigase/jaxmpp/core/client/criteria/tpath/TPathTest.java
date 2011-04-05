package tigase.jaxmpp.core.client.criteria.tpath;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class TPathTest {

	private final IQ iq;

	private final TPath tpath = new TPath();

	public TPathTest() throws XMLException {
		this.iq = IQ.create();
		iq.setTo(JID.jidInstance("a@b.c"));
		iq.setType(StanzaType.set);
		iq.setAttribute("from", "wojtas@wp.pl");
		final Element pubsub = new DefaultElement("pubsub", null, "a:b");
		iq.addChild(pubsub);
		final Element publish = new DefaultElement("publish");
		publish.setAttribute("node", "123");
		pubsub.addChild(publish);
		Element item = new DefaultElement("item");
		item.setValue("x");
		item.setAttribute("id", "345");
		publish.addChild(item);
		item = new DefaultElement("item");
		item.setAttribute("id", "456");
		publish.addChild(item);
		item = new DefaultElement("item");
		item.setAttribute("id", "567");
		publish.addChild(item);
	}

	@Test
	public void testEvaluate() {
		try {
			assertEquals("set", tpath.compile("/*/attr('type')").evaluateAsArray(iq).get(0));
			assertEquals("wojtas@wp.pl", tpath.compile("/*/attr('from')").evaluateAsArray(iq).get(0));
			assertTrue(tpath.compile("/x/").evaluateAsArray(iq).isEmpty());
			assertArrayEquals(
					new String[] { "345", "456", "567" },
					tpath.compile("/*[@type='set']/pubsub/publish/item/attr('id')").evaluateAsArray(iq).toArray(new String[] {}));
			assertArrayEquals(new String[] { "x" },
					tpath.compile("/*[@type='set']/pubsub/publish/item/value()").evaluateAsArray(iq).toArray(new String[] {}));

			assertEquals("x", tpath.compile("/*[@type='set']/pubsub/publish/item/value()").evaluate(iq));
			assertNull(tpath.compile("/*[@type='get']/pubsub/publish/item/value()").evaluate(iq));

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
