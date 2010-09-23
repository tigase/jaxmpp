package tigase.jaxmpp.core.client.criteria;

import tigase.jaxmpp.core.client.xml.Element;

public interface Criteria {

	Criteria add(Criteria criteria);

	boolean match(Element element);

}
