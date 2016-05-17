/*
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2013 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
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
package tigase.jaxmpp.j2se.connection;

import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xmpp.modules.ContextAware;
import tigase.jaxmpp.core.client.xmpp.modules.connection.ConnectionSession;

import java.net.Socket;

/**
 * 
 * @author andrzej
 */
public interface ConnectionManager extends ContextAware {

	void connectTcp(JaxmppCore jaxmpp, ConnectionSession session) throws JaxmppException;

	void connectUdp(JaxmppCore jaxmpp, ConnectionSession session) throws JaxmppException;

	void initConnection(JaxmppCore jaxmpp, ConnectionSession session, InitializedCallback callback) throws JaxmppException;

	interface ConnectionClosedHandler extends EventHandler {

		void onConnectionClosed(SessionObject sessionObject);

		class ConnectionClosedEvent extends JaxmppEvent<ConnectionClosedHandler> {

			public ConnectionClosedEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			public void dispatch(ConnectionClosedHandler handler) {
				handler.onConnectionClosed(sessionObject);
			}

		}
	}

	interface ConnectionEstablishedHandler extends EventHandler {

		void onConnectionEstablished(SessionObject sessionObject, ConnectionSession connectionSession, Socket socket)
				throws JaxmppException;

		class ConnectionEstablishedEvent extends JaxmppEvent<ConnectionEstablishedHandler> {

			private ConnectionSession connectionSession;

			private Socket socket;

			public ConnectionEstablishedEvent(SessionObject sessionObject, ConnectionSession connectionSession, Socket socket) {
				super(sessionObject);
				this.connectionSession = connectionSession;
				this.socket = socket;
			}

			@Override
			public void dispatch(ConnectionEstablishedHandler handler) throws JaxmppException {
				handler.onConnectionEstablished(sessionObject, connectionSession, socket);
			}

			public ConnectionSession getConnectionSession() {
				return connectionSession;
			}

			public void setConnectionSession(ConnectionSession connectionSession) {
				this.connectionSession = connectionSession;
			}

			public Socket getSocket() {
				return socket;
			}

			public void setSocket(Socket socket) {
				this.socket = socket;
			}

		}
	}

	interface ConnectionFailedHandler extends EventHandler {

		void onConnectionFailed(SessionObject sessionObject, ConnectionSession connectionSession);

		class ConnectionFailedEvent extends JaxmppEvent<ConnectionFailedHandler> {

			private ConnectionSession connectionSession;

			public ConnectionFailedEvent(SessionObject sessionObject, ConnectionSession connectionSession) {
				super(sessionObject);
				this.connectionSession = connectionSession;
			}

			@Override
			public void dispatch(ConnectionFailedHandler handler) {
				handler.onConnectionFailed(sessionObject, connectionSession);
			}

			public ConnectionSession getConnectionSession() {
				return connectionSession;
			}

			public void setConnectionSession(ConnectionSession connectionSession) {
				this.connectionSession = connectionSession;
			}

		}
	}

	interface InitializedCallback {

		void initialized(JaxmppCore jaxmpp, ConnectionSession session);

	}

}
