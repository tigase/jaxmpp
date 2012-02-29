package tigase.jaxmpp.core.client.xmpp.modules.registration;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class InBandRegistrationModule extends AbstractIQModule {

	public static class RegistrationEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private IQ stanza;

		public RegistrationEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		public IQ getStanza() {
			return stanza;
		}

		public void setStanza(IQ stanza) {
			this.stanza = stanza;
		}

	}

	public static final String IN_BAND_REGISTRATION_MODE_KEY = "IN_BAND_REGISTRATION_MODE_KEY";

	public static final EventType NotSupportedError = new EventType();

	public final static EventType ReceivedError = new EventType();

	public final static EventType ReceivedRequestedFields = new EventType();

	public final static EventType ReceivedTimeout = new EventType();

	public static boolean isRegistrationAvailable(SessionObject sessionObject) throws JaxmppException {
		final Element features = sessionObject.getStreamFeatures();

		boolean registrationSupported = features != null
				&& features.getChildrenNS("register", "http://jabber.org/features/iq-register") != null;

		return registrationSupported;
	}

	private final Observable observable;

	public InBandRegistrationModule(Observable parentObservable, SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);
		this.observable = new Observable(parentObservable);
	}

	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<? extends BaseEvent> listener) {
		observable.addListener(listener);
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
	protected void processGet(IQ element) throws JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	@Override
	protected void processSet(IQ element) throws JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	public void register(String username, String password, String email, AsyncCallback asyncCallback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		iq.setTo(JID.jidInstance((String) sessionObject.getProperty(SessionObject.SERVER_NAME)));

		DefaultElement q = new DefaultElement("query", null, "jabber:iq:register");
		iq.addChild(q);
		q.addChild(new DefaultElement("username", username, null));
		q.addChild(new DefaultElement("password", password, null));
		q.addChild(new DefaultElement("email", email, null));

		writer.write(iq, asyncCallback);

	}

	public void removeAccount(AsyncCallback asyncCallback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		iq.setTo(JID.jidInstance((String) sessionObject.getProperty(SessionObject.SERVER_NAME)));

		DefaultElement q = new DefaultElement("query", null, "jabber:iq:register");
		iq.addChild(q);
		q.addChild(new DefaultElement("remove"));

		writer.write(iq, asyncCallback);
	}

	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	public void removeListener(Listener<? extends BaseEvent> listener) {
		observable.removeListener(listener);
	}

	public void start() throws JaxmppException {
		if (!isRegistrationAvailable(sessionObject)) {
			RegistrationEvent event = new RegistrationEvent(NotSupportedError, sessionObject);
			observable.fireEvent(event);
		} else {
			IQ iq = IQ.create();
			iq.setType(StanzaType.get);
			iq.setTo(JID.jidInstance((String) sessionObject.getProperty(SessionObject.SERVER_NAME)));

			iq.addChild(new DefaultElement("query", null, "jabber:iq:register"));

			writer.write(iq, new AsyncCallback() {

				@Override
				public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
					// TODO Auto-generated method stub
					System.out.println("??? ");
					System.out.println(responseStanza.getAsString());
					RegistrationEvent event = new RegistrationEvent(ReceivedError, sessionObject);
					event.setStanza((IQ) responseStanza);

					observable.fireEvent(event);
				}

				@Override
				public void onSuccess(Stanza responseStanza) throws JaxmppException {
					RegistrationEvent event = new RegistrationEvent(ReceivedRequestedFields, sessionObject);
					event.setStanza((IQ) responseStanza);

					observable.fireEvent(event);
				}

				@Override
				public void onTimeout() throws JaxmppException {
					RegistrationEvent event = new RegistrationEvent(ReceivedTimeout, sessionObject);

					observable.fireEvent(event);
				}
			});
		}
	}

}
