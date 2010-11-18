package tigase.jaxmpp.core.client.xmpp.modules.sasl;

import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.criteria.Or;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.mechanisms.PlainMechanism;

public class SaslModule implements XmppModule {

	public static class DefaultCredentialsCallback implements CredentialsCallback {

		private final SessionObject sessionObject;

		public DefaultCredentialsCallback(SessionObject sessionObject) {
			this.sessionObject = sessionObject;
		}

		@Override
		public String getPassword() {
			return sessionObject.getProperty(SessionObject.PASSWORD);
		}

		@Override
		public String getUsername() {
			return sessionObject.getProperty(SessionObject.USER_JID);
		}
	}

	public static final class SaslEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		public SaslEvent(EventType type) {
			super(type);
		}
	}

	private final static Criteria CRIT = new Or(new Criteria[] {
			ElementCriteria.name("success", "urn:ietf:params:xml:ns:xmpp-sasl"),
			ElementCriteria.name("failure", "urn:ietf:params:xml:ns:xmpp-sasl"),
			ElementCriteria.name("challenge", "urn:ietf:params:xml:ns:xmpp-sasl") });

	public static final String SASL_CREDENTIALS_CALLBACK = "jaxmpp#credentialsCallback";

	public final static EventType SASL_FAILED = new EventType();

	public static final String SASL_MECHANISM = "jaxmpp#saslMechanism";

	public final static EventType SASL_START = new EventType();

	public final static EventType SASL_SUCCESS = new EventType();

	protected final Logger log;

	private final Observable observable = new Observable();

	protected final SessionObject sessionObject;

	protected final PacketWriter writer;

	public SaslModule(SessionObject sessionObject, PacketWriter packetWriter) {
		log = Logger.getLogger(this.getClass().getName());
		this.sessionObject = sessionObject;
		this.writer = packetWriter;
	}

	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		// TODO Auto-generated method stub
		return null;
	}

	public void login() throws XMLException {
		observable.fireEvent(SASL_START);

		sessionObject.setProperty(SASL_MECHANISM, new PlainMechanism());

		SaslMechanism mechanism = sessionObject.getProperty(SASL_MECHANISM);
		Element auth = new DefaultElement("auth");
		auth.setAttribute("xmlns", "urn:ietf:params:xml:ns:xmpp-sasl");
		auth.setAttribute("mechanism", mechanism.name());

		writer.write(auth);
	}

	@Override
	public void process(Element element) throws XMPPException, XMLException {
		if ("success".equals(element.getName())) {
			processSuccess(element);
		} else if ("failure".equals(element.getName())) {
			processFailure(element);
		} else if ("challenge".equals(element.getName())) {
			processChallenge(element);
		}
	}

	protected void processChallenge(Element element) throws XMPPException, XMLException {
		SaslMechanism mechanism = sessionObject.getProperty(SASL_MECHANISM);

	}

	protected void processFailure(Element element) throws XMPPException, XMLException {
		observable.fireEvent(SASL_FAILED, new SaslEvent(SASL_FAILED));
	}

	protected void processSuccess(Element element) throws XMPPException, XMLException {
		log.fine("Authenticated");
		System.out.println("Authenticated");
		observable.fireEvent(SASL_SUCCESS, new SaslEvent(SASL_SUCCESS));
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

}
