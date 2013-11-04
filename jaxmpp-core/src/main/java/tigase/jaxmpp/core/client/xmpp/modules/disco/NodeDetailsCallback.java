package tigase.jaxmpp.core.client.xmpp.modules.disco;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule.Identity;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule.Item;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;

public interface NodeDetailsCallback {

	String[] getFeatures(SessionObject sessionObject, IQ requestStanza, String node) throws JaxmppException;

	Identity getIdentity(SessionObject sessionObject, IQ requestStanza, String node) throws JaxmppException;

	Item[] getItems(SessionObject sessionObject, IQ requestStanza, String node) throws JaxmppException;

}
