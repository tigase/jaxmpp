package tigase.jaxmpp.core.client.xmpp.modules;

import tigase.jaxmpp.core.client.XmppModulesManager;

/**
 * Interface should be implemented by module that need to be informed about its
 * state in {@linkplain XmppModulesManager}.
 * 
 */
public interface InitializingModule {

	/**
	 * Called when module is registered. At this moment module is formally
	 * registered and it is part of client.
	 */
	void afterRegister();

	/**
	 * Called just before registration module in {@linkplain XmppModulesManager}
	 * . It is good place to check if module is initialized properly.
	 */
	void beforeRegister();

	/**
	 * Called when module is unregistered.
	 */
	void beforeUnregister();

}
