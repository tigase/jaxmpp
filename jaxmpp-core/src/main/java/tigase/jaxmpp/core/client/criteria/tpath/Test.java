package tigase.jaxmpp.core.client.criteria.tpath;

import java.util.ArrayList;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class Test {

	public static void main(String[] args) throws Exception {

		IQ iq = IQ.create();
		iq.setTo(JID.jidInstance("a@b.c"));
		iq.setType(StanzaType.set);
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

		ArrayList<Object> x = new ArrayList<Object>();

		Node node = new Node(ElementCriteria.name("iq"), new Node(ElementCriteria.name("pubsub"), new Node(
				ElementCriteria.name("publish"), new Node(null, null))));

		node.evaluate(x, iq);

		for (Object object : x) {
			System.out.println(object);
		}

		String p = "/iq[@type='set']/pubsub"; // element <pubsub> z <iq
												// type='set'>

		p = "/iq[@type='set']/pubsub/value()";

	}

}
