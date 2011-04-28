package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
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
	 * @throws JaxmppException
	 */
	void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException;

	/**
	 * Called when received response has type {@linkplain StanzaType#result
	 * result}.
	 * 
	 * @param responseStanza
	 *            received stanza
	 * @throws JaxmppException
	 */
	void onSuccess(Stanza responseStanza) throws JaxmppException;

	/**
	 * Called when response wasn't received in given time.
	 * 
	 * @throws JaxmppException
	 */
	void onTimeout() throws JaxmppException;

}
