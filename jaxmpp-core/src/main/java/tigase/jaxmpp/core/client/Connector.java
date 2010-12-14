package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public interface Connector {

	public static class ConnectorEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		public static long getSerialversionuid() {
			return serialVersionUID;
		}

		private Throwable caught;

		private Element stanza;

		public ConnectorEvent(EventType type) {
			super(type);
		}

		public Throwable getCaught() {
			return caught;
		}

		public Element getStanza() {
			return stanza;
		}

		public void setCaught(Throwable caught) {
			this.caught = caught;
		}

		public void setStanza(Element stanza) {
			this.stanza = stanza;
		}
	}

	public static enum Stage {
		connected,
		connecting,
		disconnected,
		disconnecting
	}

	public final static EventType Connected = new EventType();

	public final static String CONNECTOR_STAGE = "connector#stage";

	public final static String ENCRYPTED = "connector#encrypted";

	public final static EventType EncryptionEstablished = new EventType();

	public final static EventType Error = new EventType();

	public final static EventType StageChanged = new EventType();

	public final static EventType StanzaReceived = new EventType();

	public final static EventType StreamTerminated = new EventType();

	public static final String TRUST_MANAGER = "connector#trustManager";

	public void addListener(EventType eventType, Listener<ConnectorEvent> listener);

	public XmppSessionLogic createSessionLogic(XmppModulesManager modulesManager, PacketWriter writer);

	public Stage getStage();

	boolean isSecure();

	public void removeAllListeners();

	public void removeListener(EventType eventType, Listener<ConnectorEvent> listener);

	public void restartStream() throws XMLException, JaxmppException;

	public void send(final Element stanza) throws XMLException, JaxmppException;

	public void start() throws XMLException, JaxmppException;

	public void stop() throws XMLException, JaxmppException;

}
