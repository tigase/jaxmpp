package tigase.jaxmpp.core.client.xmpp.modules.extensions;

import tigase.jaxmpp.core.client.XmppModule;

public interface ExtendableModule {

	void addExtension(Extension<XmppModule> f);

	ExtensionsChain getExtensionChain();

	void removeExtension(Extension<XmppModule> f);

}
