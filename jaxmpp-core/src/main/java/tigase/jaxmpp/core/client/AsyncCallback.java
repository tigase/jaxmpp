package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 * Main interface for callback of all <a
 * href='http://xmpp.org/rfcs/rfc6120.html#stanzas-semantics-iq'>IQ</a>
 * asynchronous request-response mechanism.
 * 
 * @author bmalkow
 * 
 */
public interface AsyncCallback {

	/**
	 * Called when received response has type {@linkplain StanzaType#error
	 * error}.
	 * 
	 * @param responseStanza
	 *            received IQ stanza
	 * @param error
	 *            error condition
	 * @throws XMLException
	 */
	void onError(Stanza responseStanza, ErrorCondition error) throws XMLException;

	/**
	 * Called when received response has type {@linkplain StanzaType#result
	 * result}.
	 * 
	 * @param responseStanza
	 *            received stanza
	 * @throws XMLException
	 */
	void onSuccess(Stanza responseStanza) throws XMLException;

	/**
	 * Called when response wasn't received in given time.
	 * 
	 * @throws XMLException
	 */
	void onTimeout() throws XMLException;

}
