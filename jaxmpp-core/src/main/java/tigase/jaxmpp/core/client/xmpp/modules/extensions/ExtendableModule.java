package tigase.jaxmpp.core.client.xmpp.modules.extensions;

public interface ExtendableModule {

	void addExtension(Extension f);

	ExtensionsChain getExtensionChain();

	void removeExtension(Extension f);

}
