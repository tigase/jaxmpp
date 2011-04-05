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

	public void evaluate(final Collection<Object> result, Element src) throws XMLException {
		if (criteria != null && !criteria.match(src))
			return;

		if (subnode == null && function != null) {
			Object r = function.value(src);
			if (r != null)
				result.add(r);
			return;
		} else if (subnode == null && function == null) {
			Object r = src;
			if (r != null)
				result.add(r);
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

	public Function getFunction() {
		return function;
	}

	public Node getSubnode() {
		return subnode;
	}

	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public void setSubnode(Node subnode) {
		this.subnode = subnode;
	}

}
