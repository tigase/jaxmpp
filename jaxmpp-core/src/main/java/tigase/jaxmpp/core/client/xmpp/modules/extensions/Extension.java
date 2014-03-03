package tigase.jaxmpp.core.client.xmpp.modules.extensions;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;

public interface Extension {

	Element afterReceive(Element received) throws JaxmppException;

	Element beforeSend(Element received) throws JaxmppException;

	String[] getFeatures();
}
