package tigase.jaxmpp.core.client.xmpp.modules;

import java.util.logging.Logger;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class SessionEstablishmentModule implements XmppModule {

	public static final class SessionEstablishmentEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private ErrorCondition error;

		public SessionEstablishmentEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		public ErrorCondition getError() {
			return error;
		}

		public void setError(ErrorCondition error) {
			this.error = error;
		}

	}

	public static final String SESSION_ESTABLISHED = "jaxmpp#sessionEstablished";

	public static final EventType SessionEstablishmentError = new EventType();

	public static final EventType SessionEstablishmentSuccess = new EventType();

	public static boolean isSessionEstablishingAvailable(final SessionObject sessionObject) throws XMLException {
		final Element features = sessionObject.getStreamFeatures();

		return features != null && features.getChildrenNS("session", "urn:ietf:params:xml:ns:xmpp-session") != null;
	}

	protected final Logger log;

	private final Observable observable;

	protected final SessionObject sessionObject;

	protected final PacketWriter writer;

	public SessionEstablishmentModule(Observable parentObservable, SessionObject sessionObject, PacketWriter packetWriter) {
		this.observable = new Observable(parentObservable);
		log = Logger.getLogger(this.getClass().getName());
		this.sessionObject = sessionObject;
		this.writer = packetWriter;
	}

	public void addListener(EventType eventType, Listener<SessionEstablishmentEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void establish() throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setXMLNS("jabber:client");
		iq.setType(StanzaType.set);

		Element bind = new DefaultElement("session", null, "urn:ietf:params:xml:ns:xmpp-session");
		iq.addChild(bind);

		writer.write(iq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
				sessionObject.setProperty(SESSION_ESTABLISHED, Boolean.FALSE);
				SessionEstablishmentEvent event = new SessionEstablishmentEvent(SessionEstablishmentError, sessionObject);
				event.setError(error);
				observable.fireEvent(SessionEstablishmentError, event);
			}

			@Override
			public void onSuccess(Stanza responseStanza) throws JaxmppException {
				sessionObject.setProperty(SESSION_ESTABLISHED, Boolean.TRUE);
				SessionEstablishmentEvent event = new SessionEstablishmentEvent(SessionEstablishmentSuccess, sessionObject);
				observable.fireEvent(SessionEstablishmentSuccess, event);
			}

			@Override
			public void onTimeout() throws JaxmppException {
				SessionEstablishmentEvent event = new SessionEstablishmentEvent(SessionEstablishmentError, sessionObject);
				observable.fireEvent(SessionEstablishmentError, event);
			}
		});
	}

	@Override
	public Criteria getCriteria() {
		return null;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	@Override
	public void process(Element element) throws XMPPException, XMLException {
	}

	public void removeListener(EventType eventType, Listener<SessionEstablishmentEvent> listener) {
		observable.removeListener(eventType, listener);
	}

}
