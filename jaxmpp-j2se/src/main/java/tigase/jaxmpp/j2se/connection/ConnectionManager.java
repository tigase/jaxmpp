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

import java.net.Socket;

import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xmpp.modules.ContextAware;
import tigase.jaxmpp.core.client.xmpp.modules.connection.ConnectionSession;

/**
 * 
 * @author andrzej
 */
public interface ConnectionManager extends ContextAware {

	public interface ConnectionClosedHandler extends EventHandler {

		public static class ConnectionClosedEvent extends JaxmppEvent<ConnectionClosedHandler> {
			
			public ConnectionClosedEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			protected void dispatch(ConnectionClosedHandler handler) {
				handler.onConnectionClosed(sessionObject);
			}

		}

		void onConnectionClosed(SessionObject sessionObject);
	};

	public interface ConnectionEstablishedHandler extends EventHandler {

		public static class ConnectionEstablishedEvent extends JaxmppEvent<ConnectionEstablishedHandler> {

			private ConnectionSession connectionSession;

			private Socket socket;

			public ConnectionEstablishedEvent(SessionObject sessionObject, ConnectionSession connectionSession, Socket socket) {
				super(sessionObject);
				this.connectionSession = connectionSession;
				this.socket = socket;
			}

			@Override
			protected void dispatch(ConnectionEstablishedHandler handler) throws JaxmppException {
				handler.onConnectionEstablished(sessionObject, connectionSession, socket);
			}

			public ConnectionSession getConnectionSession() {
				return connectionSession;
			}

			public Socket getSocket() {
				return socket;
			}

			public void setConnectionSession(ConnectionSession connectionSession) {
				this.connectionSession = connectionSession;
			}

			public void setSocket(Socket socket) {
				this.socket = socket;
			}

		}

		void onConnectionEstablished(SessionObject sessionObject, ConnectionSession connectionSession, Socket socket)
				throws JaxmppException;
	}

	public interface ConnectionFailedHandler extends EventHandler {

		public static class ConnectionFailedEvent extends JaxmppEvent<ConnectionFailedHandler> {

			private ConnectionSession connectionSession;

			public ConnectionFailedEvent(SessionObject sessionObject, ConnectionSession connectionSession) {
				super(sessionObject);
				this.connectionSession = connectionSession;
			}

			@Override
			protected void dispatch(ConnectionFailedHandler handler) {
				handler.onConnectionFailed(sessionObject, connectionSession);
			}

			public ConnectionSession getConnectionSession() {
				return connectionSession;
			}

			public void setConnectionSession(ConnectionSession connectionSession) {
				this.connectionSession = connectionSession;
			}

		}

		void onConnectionFailed(SessionObject sessionObject, ConnectionSession connectionSession);
	}

	public static interface InitializedCallback {

		void initialized(JaxmppCore jaxmpp, ConnectionSession session);

	}

	void connectTcp(JaxmppCore jaxmpp, ConnectionSession session) throws JaxmppException;

	void connectUdp(JaxmppCore jaxmpp, ConnectionSession session) throws JaxmppException;

	void initConnection(JaxmppCore jaxmpp, ConnectionSession session, InitializedCallback callback) throws JaxmppException;

}
