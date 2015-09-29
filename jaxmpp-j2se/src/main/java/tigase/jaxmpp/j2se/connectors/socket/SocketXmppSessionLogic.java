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
package tigase.jaxmpp.j2se.connectors.socket;

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.connector.AbstractSocketXmppSessionLogic;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;

public class SocketXmppSessionLogic extends AbstractSocketXmppSessionLogic<SocketConnector> {

	public SocketXmppSessionLogic(SocketConnector connector, XmppModulesManager modulesManager, Context context) {
		super(connector, modulesManager, context);
	}

	@Override
	protected void processStreamFeatures(Element featuresElement) throws JaxmppException {
		final Boolean tlsDisabled = context.getSessionObject().getProperty(SocketConnector.TLS_DISABLED_KEY);
		final boolean tlsAvailable = SocketConnector.isTLSAvailable(context.getSessionObject());
		final Boolean compressionDisabled = context.getSessionObject().getProperty(SocketConnector.COMPRESSION_DISABLED_KEY);
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