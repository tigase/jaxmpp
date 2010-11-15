package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.xmpp.modules.PingModule;

public class DefaultXmppModulesManager extends XmppModulesManager {

	public DefaultXmppModulesManager() {
		super();

		register(new PingModule());
	}
}
