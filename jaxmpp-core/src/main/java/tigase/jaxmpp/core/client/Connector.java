package tigase.jaxmpp.core.client;

import javax.net.ssl.TrustManager;

import tigase.jaxmpp.core.client.connector.StreamError;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Main Connector interface.
 * 
 * <dl>
 * <dt><b>Events:</b></dt>
 * 
 * <dd><b>{@link Connector#Connected Connected}</b> : {@link ConnectorEvent
 * ConnectorEvent} ()<br>
 * <div>Fires after creates XMPP Stream</div>
 * <ul>
 * </ul></dd>
 * 
 * <dd><b>{@link Connector#EncryptionEstablished EncryptionEstablished}</b> :
 * {@link ConnectorEvent ConnectorEvent} ()<br>
 * <div>Fires after encrypted connection is established.</div>
 * <ul>
 * </ul></dd>
 * 
 * <dd><b>{@link Connector#Error Error}</b> : {@link ConnectorEvent
 * ConnectorEvent} (caught)<br>
 * <div>Fires on XMPP Stream error</div>
 * <ul>
 * <li>caught : exception</li>
 * </ul>
 * </dd>
 * 
 * <dd><b>{@link Connector#StateChanged StateChanged}</b> :
 * {@link ConnectorEvent ConnectorEvent} ()<br>
 * <div>Fires after connection state is changed</div>
 * <ul>
 * </ul></dd>
 * 
 * <dd><b>{@link Connector#StanzaReceived StanzaReceived}</b> :
 * {@link ConnectorEvent ConnectorEvent} (stanza)<br>
 * <div>Fires after next stanza is received</div>
 * <ul>
 * <li>stanza : received stanza</li>
 * </ul>
 * </dd>
 * 
 * <dd><b>{@link Connector#StreamTerminated StreamTerminated}</b> :
 * {@link ConnectorEvent ConnectorEvent} ()<br>
 * <div>Fires after XMPP Stream is terminated</div>
 * <ul>
 * </ul></dd>
 * 
 * 
 * <br/>
 * <dt><b>Properties:</b></dt>
 * 
 * <dd><b>{@link Connector#TRUST_MANAGER_KEY TRUST_MANAGER}</b>: Custom
 * {@link TrustManager TrustManager} instead of dummy (accespts all
 * certificates) builded in.</dd>
 * 
 * 
 * </dl>
 * 
 * @author $Author$
 * @version $Revision$
 */
public interface Connector {

	public static class ConnectorEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		public static long getSerialversionuid() {
			return serialVersionUID;
		}

		private Throwable caught;

		private Element stanza;

		private StreamError streamError;

		private Element streamErrorElement;

		public ConnectorEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		public Throwable getCaught() {
			return caught;
		}

		/**
		 * Return received stanza
		 * 
		 * @return stanza
		 */
		public Element getStanza() {
			return stanza;
		}

		public StreamError getStreamError() {
			return this.streamError;
		}

		public Element getStreamErrorElement() {
			return this.streamErrorElement;
		}

		public void setCaught(Throwable caught) {
			this.caught = caught;
		}

		public void setStanza(Element stanza) {
			this.stanza = stanza;
		}

		public void setStreamError(StreamError streamError) {
			this.streamError = streamError;
		}

		public void setStreamErrorElement(Element streamErrorElement) {
			this.streamErrorElement = streamErrorElement;
		}
	}

	public static enum State {
		connected,
		connecting,
		disconnected,
		disconnecting
	}

	public final static EventType BodyReceived = new EventType();

	/**
	 * Event fires after creates XMPP Stream
	 */
	public final static EventType Connected = new EventType();

	public final static String CONNECTOR_STAGE_KEY = "CONNECTOR#STAGE_KEY";

	public final static String DISABLE_KEEPALIVE_KEY = "CONNECTOR#DISABLEKEEPALIVE";

	public final static String DISABLE_SOCKET_TIMEOUT_KEY = "CONNECTOR#DISABLE_SOCKET_TIMEOUT_KEY";

	public final static String ENCRYPTED_KEY = "CONNECTOR#ENCRYPTED_KEY";

	/**
	 * Event fires after encrypted connection is established.
	 */
	public final static EventType EncryptionEstablished = new EventType();

	/**
	 * Event fires on XMPP Stream error.
	 * <p>
	 * Filled fields:
	 * <ul>
	 * <li>caught : exception</li>
	 * </ul>
	 * </p>
	 */
	public final static EventType Error = new EventType();

	public final static String EXTERNAL_KEEPALIVE_KEY = "CONNECTOR#EXTERNAL_KEEPALIVE_KEY";

	/**
	 * Event fires after creates XMPP Stream.
	 * <p>
	 * Filled fields:
	 * <ul>
	 * <li>{@link ConnectorEvent#getStanza() stanza} : received stanza</li>
	 * </ul>
	 * </p>
	 */
	public final static EventType StanzaReceived = new EventType();

	public final static EventType StanzaSending = new EventType();

	/**
	 * Event fires after connection state is changed.
	 */
	public final static EventType StateChanged = new EventType();

	/**
	 * Event fires after XMPP Stream is terminated.
	 */
	public final static EventType StreamTerminated = new EventType();

	/**
	 * Key for define {@linkplain SessionObject#setUserProperty(String, Object)
	 * property}. Custom {@link TrustManager TrustManager} instead of dummy
	 * (accespts all certificates) builded in.
	 */
	public static final String TRUST_MANAGER_KEY = "TRUST_MANAGER_KEY";

	public void addListener(EventType eventType, Listener<? extends ConnectorEvent> listener);

	public XmppSessionLogic createSessionLogic(XmppModulesManager modulesManager, PacketWriter writer);

	public Observable getObservable();

	public State getState();

	boolean isSecure();

	/**
	 * Whitespace ping.
	 * 
	 * @throws JaxmppException
	 */
	public void keepalive() throws JaxmppException;

	public void removeAllListeners();

	public void removeListener(EventType eventType, Listener<ConnectorEvent> listener);

	public void restartStream() throws XMLException, JaxmppException;

	public void send(byte[] buffer) throws JaxmppException;

	public void send(final Element stanza) throws XMLException, JaxmppException;

	public void setObservable(Observable observable);

	public void start() throws XMLException, JaxmppException;

	public void stop() throws XMLException, JaxmppException;

	public void stop(boolean terminate) throws XMLException, JaxmppException;

}
