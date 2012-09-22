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
package tigase.jaxmpp.core.client.factory;

import java.util.HashMap;

public class UniversalFactory {

	public static interface FactorySpi<T> {
		T create();
	}

	private static UniversalFactory instance;

	public static <T> T createInstance(String key) {
		FactorySpi<T> spi = (FactorySpi<T>) instance().factories.get(key);
		if (spi == null)
			return null;
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