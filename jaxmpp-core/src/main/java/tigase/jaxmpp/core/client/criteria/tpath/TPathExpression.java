package tigase.jaxmpp.core.client.criteria.tpath;

import java.util.ArrayList;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class TPathExpression {

	private Node node;

	TPathExpression(Node rootNode) {
		this.node = rootNode;
	}

	public Object evaluate(Element element) throws XMLException {
		ArrayList<Object> x = new ArrayList<Object>();
		this.node.evaluate(x, element);

		return x;
		// if (x.size() == 0) {
		// return null;
		// } else if (x.size() == 1) {
		// return x.get(0);
		// } else
		// return x;

	}

}
