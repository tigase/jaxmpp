package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public interface AsyncCallback {

	void onError(Stanza responseStanza, ErrorCondition error, PacketWriter writer) throws XMLException;

	void onSuccess(Stanza responseStanza, PacketWriter writer) throws XMLException;

	void onTimeout(PacketWriter writer) throws XMLException;

}
