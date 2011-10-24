package tigase.jaxmpp.core.client.factory;

import java.util.HashMap;

public class UniversalFactory {

	public static interface FactorySpi<T> {
		T create();
	}

	private static UniversalFactory instance;

	public static <T> T createInstance(String key) {
		FactorySpi<T> spi = (FactorySpi<T>) instance().factories.get(key);
		return spi.create();
	}

	static UniversalFactory instance() {
		if (instance == null)
			instance = new UniversalFactory();
		return instance;
	}

	public static void setSpi(String key, FactorySpi<?> spi) {
		instance().factories.put(key, spi);
	}

	private final HashMap<String, FactorySpi<?>> factories = new HashMap<String, FactorySpi<?>>();

}
