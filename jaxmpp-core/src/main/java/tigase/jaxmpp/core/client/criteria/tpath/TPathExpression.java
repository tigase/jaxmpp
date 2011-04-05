package tigase.jaxmpp.core.client.criteria.tpath;

import java.util.ArrayList;
import java.util.List;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class TPathExpression {

	private Node node;

	TPathExpression(Node rootNode) {
		this.node = rootNode;
	}

	public Object evaluate(Element element) throws XMLException {
		List<Object> r = evaluateAsArray(element);
		if (r.isEmpty())
			return null;
		else if (r.size() == 1) {
			return r.get(0);
		} else
			return r;
	}

	public List<Object> evaluateAsArray(Element element) throws XMLException {
		ArrayList<Object> x = new ArrayList<Object>();
		this.node.evaluate(x, element);

		return x;
	}

}
