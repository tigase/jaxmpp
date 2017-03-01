/*
 * InitializingModule.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
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

import tigase.jaxmpp.core.client.XmppModulesManager;

/**
 * Interface should be implemented by module that need to be informed about its
 * state in {@linkplain XmppModulesManager}.
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
