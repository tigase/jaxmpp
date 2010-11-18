package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public interface AsyncCallback {

	void onError(Stanza responseStanza, ErrorCondition error) throws XMLException;

	void onSuccess(Stanza responseStanza) throws XMLException;

	void onTimeout() throws XMLException;

}
