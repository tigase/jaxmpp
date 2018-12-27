/*
 * SocketXmppSessionLogic.java
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
package tigase.jaxmpp.j2se.connectors.socket;

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.connector.AbstractSocketXmppSessionLogic;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;

public class SocketXmppSessionLogic
		extends AbstractSocketXmppSessionLogic<SocketConnector> {

	public SocketXmppSessionLogic(SocketConnector connector, XmppModulesManager modulesManager, Context context) {
		super(connector, modulesManager, context);
	}

	@Override
	public void onStreamManagementFailed(SessionObject sessionObject, XMPPException.ErrorCondition condition) {
		try {
			PresenceStore store = PresenceModule.getPresenceStore(sessionObject);
			if (store != null) {
				store.clear();
			}
		} catch (JaxmppException e) {
			e.printStackTrace();
		}
		super.onStreamManagementFailed(sessionObject, condition);
	}

	@Override
	protected void processStreamFeatures(Element featuresElement) throws JaxmppException {
		final Boolean tlsDisabled = context.getSessionObject().getProperty(SocketConnector.TLS_DISABLED_KEY);
		final boolean tlsAvailable = SocketConnector.isTLSAvailable(context.getSessionObject());
		final Boolean compressionDisabled = context.getSessionObject()
				.getProperty(SocketConnector.COMPRESSION_DISABLED_KEY);
		final boolean zlibAvailable = SocketConnector.isZLibAvailable(context.getSessionObject());

		final boolean isConnectionSecure = connector.isSecure();
		final boolean isConnectionCompressed = connector.isCompressed();

		if (!isConnectionSecure && tlsAvailable && (tlsDisabled == null || !tlsDisabled)) {
			connector.startTLS();
		} else if (!isConnectionCompressed && zlibAvailable && (compressionDisabled == null || !compressionDisabled)) {
			connector.startZLib();
		} else {
			super.processStreamFeatures(featuresElement);
		}
	}
}