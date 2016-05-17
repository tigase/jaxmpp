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
package tigase.jaxmpp.core.client.connector;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.xmpp.utils.MutableBoolean;

/**
 *
 * @author andrzej
 */
public interface SeeOtherHostHandler extends EventHandler {

	void onSeeOtherHost(String seeHost, MutableBoolean handled);

	class SeeOtherHostEvent extends JaxmppEvent<SeeOtherHostHandler> {

		private final String seeHost;
		private final MutableBoolean handled;

		public SeeOtherHostEvent(SessionObject sessionObject, String seeHost, MutableBoolean handled) {
			super(sessionObject);
			this.seeHost = seeHost;
			this.handled = handled;
		}

		@Override
		public void dispatch(SeeOtherHostHandler handler) throws Exception {
			handler.onSeeOtherHost(seeHost, handled);
		}

	}
}
