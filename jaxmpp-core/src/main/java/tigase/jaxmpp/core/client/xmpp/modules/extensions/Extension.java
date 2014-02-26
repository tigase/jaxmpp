package tigase.jaxmpp.core.client.xmpp.modules.extensions;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;

public interface Extension<T extends Element> {

	T afterReceive(T received) throws JaxmppException;

	T beforeSend(T received) throws JaxmppException;

	String[] getFeatures();
}
