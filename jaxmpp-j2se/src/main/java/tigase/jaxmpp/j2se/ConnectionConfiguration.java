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
package tigase.jaxmpp.j2se;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;

import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

/**
 * Connection configuration object.
 */
public class ConnectionConfiguration extends tigase.jaxmpp.core.client.ConnectionConfiguration {

	ConnectionConfiguration(SessionObject sessionObject) {
		super(sessionObject);
	}

	/**
	 * Set BOSH Service URL. Required if connection type is <code>bosh</code>.
	 *
	 * @param boshService
	 *            BOSH service URL
	 */
	public void setBoshService(String boshService) {
		sessionObject.setUserProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY, boshService);

	}

	/**
	 * Set connection type.
	 *
	 * @param connectionType
	 *            connection type
	 */
	public void setConnectionType(ConnectionType connectionType) {
		sessionObject.setUserProperty(Jaxmpp.CONNECTOR_TYPE, connectionType.name());
	}

	/**
	 * Enable or disable TLS usage.
	 *
	 * @param disabled
	 *            <code>true</code> is TLS should be disabled.
	 */
	public void setDisableTLS(boolean disabled) {
		sessionObject.setUserProperty(SocketConnector.TLS_DISABLED_KEY, disabled);
	}

	/**
	 * Set server port. Default is 5222
	 *
	 * @param port
	 */
	public void setPort(int port) {
		sessionObject.setUserProperty(SocketConnector.SERVER_PORT, port);
	}

	/**
	 * Set connection proxy.
	 *
	 * @param host
	 *            proxy host or {@code null} for direct connection.
	 * @param port
	 *            proxy port.
	 */
	public void setProxy(String host, int port) {
		sessionObject.setUserProperty(Connector.PROXY_HOST, host);
		sessionObject.setUserProperty(Connector.PROXY_PORT, port);
	}

	/**
	 * Set proxy type.
	 *
	 * @param type
	 *            type of proxy. Available values: {@code HTTP} and
	 *            {@code SOCKS}.
	 */
	public void setProxyType(Proxy.Type type) {
		sessionObject.setUserProperty(Connector.PROXY_TYPE, type);
	}

	/**
	 * Set proxy authentication credentials.
	 * 
	 * @param username
	 *            proxy username.
	 * @param password
	 *            proxy password.
	 */
	public void setProxyUsernamePassword(final String username, final String password) {
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				if (getRequestorType() == RequestorType.PROXY) {
					String host = "" + sessionObject.getProperty(Connector.PROXY_HOST);
					String port = "" + sessionObject.getProperty(Connector.PROXY_PORT);

					if (getRequestingHost().equalsIgnoreCase(host)) {
						if (Integer.parseInt(port) == getRequestingPort()) {
							return new PasswordAuthentication(username, password.toCharArray());
						}
					}
				}
				return null;
			}
		});
	}

	/**
	 * Set server hostname. Not needed if it is equals to hostname of JID.
	 *
	 * @param server
	 *            hostname
	 */
	public void setServer(String server) {
		sessionObject.setUserProperty(SocketConnector.SERVER_HOST, server);
	}

	/**
	 * Enable o disable SASL. Default <code>true</code>.
	 *
	 * @param useSASL
	 *            <code>false</code> is only non-SASL authentication (XEP-0078)
	 *            should be available.
	 */
	public void setUseSASL(boolean useSASL) {
		sessionObject.setUserProperty(AuthModule.FORCE_NON_SASL, !useSASL);

	}

	public static enum ConnectionType {
		bosh,
		socket,
		websocket
	}

}