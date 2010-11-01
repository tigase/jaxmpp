package tigase.jaxmpp.core.client.criteria;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public interface Criteria {

	Criteria add(Criteria criteria);

	boolean match(Element element) throws XMLException;

}
