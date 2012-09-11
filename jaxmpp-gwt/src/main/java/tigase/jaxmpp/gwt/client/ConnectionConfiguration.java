package tigase.jaxmpp.gwt.client;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;

/**
 * Connection configuration object.
 */
public class ConnectionConfiguration extends tigase.jaxmpp.core.client.ConnectionConfiguration {

	ConnectionConfiguration(SessionObject sessionObject) {
		super(sessionObject);
	}

	/**
	 * Set BOSH Service URL.
	 * 
	 * @param boshService
	 *            BOSH service URL
	 */
	public void setBoshService(String boshService) {
		sessionObject.setUserProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY, boshService);

	}
}
