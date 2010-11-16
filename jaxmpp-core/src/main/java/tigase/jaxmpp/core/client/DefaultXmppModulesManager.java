package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.xmpp.modules.PingModule;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;

public class DefaultXmppModulesManager extends XmppModulesManager {

	public DefaultXmppModulesManager() {
		super();

		register(new StreamFeaturesModule());
		register(new PingModule());
	}
}
