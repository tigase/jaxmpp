/*
 * Presence.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.gwt.client;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;

/**
 * Class which helps with initialization presence related modules
 *
 * @author andrzej
 */
public class Presence {

	public static void initialize(Jaxmpp jaxmpp) throws JaxmppException {
		// initializing presence store if not exists
		PresenceStore presenceStore = PresenceModule.getPresenceStore(jaxmpp.getSessionObject());
		if (presenceStore == null) {
			presenceStore = new GWTPresenceStore();
			PresenceModule.setPresenceStore(jaxmpp.getSessionObject(), presenceStore);
		}
		jaxmpp.set(presenceStore);

		// registering presence modules
		jaxmpp.getModulesManager().register(new PresenceModule());
	}

}
