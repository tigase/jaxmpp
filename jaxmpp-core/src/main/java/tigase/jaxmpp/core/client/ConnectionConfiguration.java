package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.CredentialsCallback;

/**
 * Connection configuration object.
 * 
 * It is wrapper around {@linkplain SessionObject}.
 * 
 * @author bmalkow
 * 
 */
public abstract class ConnectionConfiguration {

	protected final SessionObject sessionObject;

	protected ConnectionConfiguration(SessionObject sessionObject) {
		this.sessionObject = sessionObject;
	}

	/**
	 * Set credentials callback;
	 * 
	 * @param credentialsCallback
	 *            callback
	 */
	public void setCredentialsCallback(CredentialsCallback credentialsCallback) {
		sessionObject.setUserProperty(AuthModule.CREDENTIALS_CALLBACK, credentialsCallback);
	}

	/**
	 * Set XMPP resource.
	 * 
	 * @param resource
	 *            resource
	 */
	public void setResource(String resource) {
		sessionObject.setUserProperty(SessionObject.RESOURCE, resource);
	}

	/**
	 * Set users JabberID.
	 * 
	 * @param jid
	 *            JabberID
	 */
	public void setUserJID(BareJID jid) {
		sessionObject.setUserProperty(SessionObject.USER_BARE_JID, jid);
	}

	/**
	 * Set users password.
	 * 
	 * @param password
	 *            password. If <code>null</code> then ANONYMOUS authentication
	 *            will be used.
	 */
	public void setUserPassword(String password) {
		sessionObject.setUserProperty(SessionObject.PASSWORD, password);
	}

}
