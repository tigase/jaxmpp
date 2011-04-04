package tigase.jaxmpp.core.client.criteria.tpath;

import java.util.Collection;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class TPath {

	public static void main(String[] args) throws Exception {
		TPath path = new TPath();

		// path.createNode("iq[@type='get']");
		// path.createNode("iq");
		// path.createNode("value()");

		String x = "/*[@type='sets']/pubsub/publish/*";
		TPathExpression exp = path.compile(x);

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

		Collection<Object> r = (Collection<Object>) exp.evaluate(iq);
		for (Object object : r) {
			if (object instanceof Element) {
				System.out.println(((Element) object).getAsString());
			} else
				System.out.println(object);
		}
	}

	public TPathExpression compile(final String path) {
		String[] tokens = path.split("/");
		Node rootNode = null;
		Node lastNode = null;
		for (String string : tokens) {
			if (rootNode == null && string.isEmpty())
				continue;
			Node n = createNode(string);

			if (rootNode == null)
				rootNode = n;

			if (lastNode != null) {
				lastNode.setSubnode(n);
			}

			lastNode = n;
		}

		return new TPathExpression(rootNode);
	}

	private Node createNode(String item) {
		String[] tkns = item.split("[\\[\\]]");
		String name = null;
		for (int i = 0; i < tkns.length; i++) {
			String l = tkns[i];
			if (i == 0) {
				name = l;
			} else if (l.startsWith("@")) {
			}

		}

		final String fName = name;

		Criteria criteria = new Criteria() {

			@Override
			public Criteria add(Criteria criteria) {
				return null;
			}

			@Override
			public boolean match(Element element) throws XMLException {
				if (!fName.equals("*") && !element.getName().equals(fName))
					return false;

				return true;
			}
		};
		Node n = new Node(criteria, null);

		return n;
	}

}
