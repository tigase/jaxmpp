package tigase.jaxmpp.core.client.xmpp.modules.adhoc;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;

public interface AdHocCommand {

	String[] getFeatures();

	String getName();

	String getNode();

	void handle(AdHocRequest request, AdHocResponse response) throws JaxmppException;

	boolean isAllowed(JID jid);

}
