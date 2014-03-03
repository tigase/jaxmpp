package tigase.jaxmpp.core.client.xmpp.modules.extensions;

import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;

public interface Extension<M extends XmppModule> {

	Element afterReceive(Element received) throws JaxmppException;

	Element beforeSend(Element received) throws JaxmppException;

	String[] getFeatures();

	void setXmppModule(M module);
}
