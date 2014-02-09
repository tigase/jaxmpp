/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc. <office@tigase.com>
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
package tigase.jaxmpp.core.client.xmpp.modules;

import java.util.Set;
import tigase.jaxmpp.core.client.XmppModule;

/**
 *
 * @author andrzej
 */
public interface ModuleProvider {
	
	public Set<String> getAvailableFeatures();
	
	/**
	 * Return module implementation by module class.
	 * 
	 * @param moduleClass
	 *            module class
	 * @return module implementation
	 */
	@SuppressWarnings("unchecked")
	public <T extends XmppModule> T getModule(Class<T> moduleClass);
	
}
