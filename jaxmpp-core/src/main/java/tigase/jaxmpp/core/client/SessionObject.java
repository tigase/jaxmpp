package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.xml.Element;

public interface SessionObject {
	Runnable getHandler(Element element);// This should be "Packet"...

	void setHandler(Criteria criteria, Runnable handler);
}
