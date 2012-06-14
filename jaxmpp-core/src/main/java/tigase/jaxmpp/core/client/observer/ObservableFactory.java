package tigase.jaxmpp.core.client.observer;

import tigase.jaxmpp.core.client.factory.UniversalFactory;

public class ObservableFactory {

	public static interface FactorySpi extends UniversalFactory.FactorySpi<Observable> {

		@Override
		public Observable create();

		public Observable create(Observable parent);

	}

	private static FactorySpi DEFAULT_FACTORY_SPI = new FactorySpi() {

		@Override
		public Observable create() {
			return new DefaultObservable();
		}

		@Override
		public Observable create(Observable parent) {
			return new DefaultObservable(parent);
		}
	};

	private static FactorySpi factorySpi;

	public static Observable instance() {
		return instance(null);
	}

	public static Observable instance(Observable parent) {
		Observable o;
		if (factorySpi != null)
			o = factorySpi.create(parent);
		else
			o = DEFAULT_FACTORY_SPI.create(parent);

		System.out.println("Observer: " + o);

		return o;
	}

	public static void setFactorySpi(FactorySpi spi) {
		factorySpi = spi;
	}

	private ObservableFactory() {
	}

}
