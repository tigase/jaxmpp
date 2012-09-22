/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
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

		return o;
	}

	public static void setFactorySpi(FactorySpi spi) {
		factorySpi = spi;
	}

	private ObservableFactory() {
	}

}