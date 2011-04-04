package tigase.jaxmpp.core.client.criteria.tpath;

import java.util.Collection;
import java.util.List;

import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class Node {

	private Criteria criteria;

	private Function function;

	private Node subnode;

	public Node(Criteria criteria, Node subnode) {
		this(criteria, subnode, null);
	}

	public Node(Criteria criteria, Node subnode, Function f) {
		super();
		this.criteria = criteria;
		this.subnode = subnode;
		this.function = f;
	}

	public void evaluate(Collection<Object> result, Element src) throws XMLException {
		if (criteria != null && !criteria.match(src))
			return;

		if (subnode == null && function != null) {
			Object r = function.value(src);
			result.add(r);
			System.out.println("0!!! FOUND:::" + r);
			return;
		} else if (subnode == null && function == null) {
			Object r = src;
			result.add(r);
			System.out.println("1!!! FOUND:::" + r);
			return;
		}

		List<Element> children = src.getChildren();
		for (Element element : children) {
			subnode.evaluate(result, element);
		}

		return;
	}

	public Criteria getCriteria() {
		return criteria;
	}

	public Node getSubnode() {
		return subnode;
	}

	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
	}

	public void setSubnode(Node subnode) {
		this.subnode = subnode;
	}

}
