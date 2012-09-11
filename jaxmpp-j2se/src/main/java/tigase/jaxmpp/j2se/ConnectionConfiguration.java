package tigase.jaxmpp.j2se;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

/**
 * Connection configuration object.
 */
public class ConnectionConfiguration extends tigase.jaxmpp.core.client.ConnectionConfiguration {

	public static enum ConnectionType {
		bosh,
		socket
	}

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

}
