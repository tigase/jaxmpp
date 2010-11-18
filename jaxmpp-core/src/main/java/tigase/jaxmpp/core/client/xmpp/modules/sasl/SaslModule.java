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

public class SaslModule implements XmppModule {

	public static final class SaslEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		public SaslEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}
	}

	private final static Criteria CRIT = new Or(new Criteria[] {
			ElementCriteria.name("success", "urn:ietf:params:xml:ns:xmpp-sasl"),
			ElementCriteria.name("failure", "urn:ietf:params:xml:ns:xmpp-sasl"),
			ElementCriteria.name("challenge", "urn:ietf:params:xml:ns:xmpp-sasl") });

	public final static EventType SASL_FAILED = new EventType();

	public final static EventType SASL_START = new EventType();

	public final static EventType SASL_SUCCESS = new EventType();

	protected final Logger log;

	private final Observable observable = new Observable();

	public SaslModule() {
		log = Logger.getLogger(this.getClass().getName());
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

	public void login(SessionObject sessionObject, PacketWriter writer) throws XMLException {
		observable.fireEvent(SASL_START, sessionObject);

		Element auth = new DefaultElement("auth");
		auth.setAttribute("xmlns", "urn:ietf:params:xml:ns:xmpp-sasl");
		auth.setAttribute("mechanism", "ANONYMOUS");

		writer.write(auth);
	}

	@Override
	public void process(Element element, SessionObject sessionObject, PacketWriter writer) throws XMPPException, XMLException {
		if ("success".equals(element.getName())) {
			processSuccess(element, sessionObject, writer);
		} else if ("failure".equals(element.getName())) {
			processFailure(element, sessionObject, writer);
		} else if ("challenge".equals(element.getName())) {
			processChallenge(element, sessionObject, writer);
		}
	}

	protected void processChallenge(Element element, SessionObject sessionObject, PacketWriter writer) throws XMPPException,
			XMLException {
	}

	protected void processFailure(Element element, SessionObject sessionObject, PacketWriter writer) throws XMPPException,
			XMLException {
		observable.fireEvent(SASL_FAILED, new SaslEvent(SASL_FAILED, sessionObject));
	}

	protected void processSuccess(Element element, SessionObject sessionObject, PacketWriter writer) throws XMPPException,
			XMLException {
		log.fine("Authenticated");
		System.out.println("Authenticated");
		observable.fireEvent(SASL_SUCCESS, new SaslEvent(SASL_SUCCESS, sessionObject));
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

}
