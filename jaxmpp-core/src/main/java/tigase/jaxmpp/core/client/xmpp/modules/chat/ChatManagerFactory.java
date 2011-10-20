package tigase.jaxmpp.core.client.xmpp.modules.chat;


public class ChatManagerFactory {

	public static interface ChatManagerFactorySpi {
		AbstractChatManager createChatManager();
	}

	private static ChatManagerFactorySpi spi = new ChatManagerFactorySpi() {

		@Override
		public AbstractChatManager createChatManager() {
			return new DefaultChatManager();
		}
	};

	public static AbstractChatManager createChatManager() {
		return spi.createChatManager();
	}

	public static void setSpi(ChatManagerFactorySpi spi) {
		ChatManagerFactory.spi = spi;
	}

}
