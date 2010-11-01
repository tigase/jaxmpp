package tigase.jaxmpp.core.client.criteria;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class Or implements Criteria {

	private Criteria[] crits;

	public Or(Criteria criteria) {
		this.crits = new Criteria[] { criteria };
	}

	public Or(Criteria criteria1, Criteria criteria2) {
		this.crits = new Criteria[] { criteria1, criteria2 };
	}

	public Or(Criteria[] criteria) {
		this.crits = criteria;
	}

	public Criteria add(Criteria criteria) {
		throw new RuntimeException("Or.add() is not implemented!");
	}

	public boolean match(Element element) throws XMLException {
		for (int i = 0; i < crits.length; i++) {
			Criteria c = this.crits[i];
			if (c.match(element)) {
				return true;
			}
		}
		return false;
	}

}
