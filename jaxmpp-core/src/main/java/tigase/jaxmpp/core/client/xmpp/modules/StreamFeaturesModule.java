package tigase.jaxmpp.core.client.xmpp.modules;

import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.criteria.Or;
import tigase.jaxmpp.core.client.logger.Logger;
import tigase.jaxmpp.core.client.logger.LoggerFactory;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class StreamFeaturesModule implements XmppModule {

	public static class StreamFeaturesReceivedEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private Element features;

		public StreamFeaturesReceivedEvent(Element features) {
			super(STREAM_FEATURES_RECEIVED);
			this.features = features;
		}

		public Element getFeatures() {
			return features;
		}

		public void setFeatures(Element features) {
			this.features = features;
		}

	}

	private final static Criteria CRIT = new Or(new Criteria[] { ElementCriteria.name("stream:features"),
			ElementCriteria.name("features") });
	public static final EventType STREAM_FEATURES_RECEIVED = new EventType();

	protected final Logger log;

	private final Observable observable = new Observable();

	protected final PacketWriter packetWriter;

	protected final SessionObject sessionObject;

	public StreamFeaturesModule(SessionObject sessionObject, PacketWriter packetWriter) {
		log = LoggerFactory.getLogger(this.getClass().getName());
		this.sessionObject = sessionObject;
		this.packetWriter = packetWriter;
	}

	public void addListener(EventType eventType, Listener<? extends StreamFeaturesReceivedEvent> listener) {
		observable.addListener(eventType, listener);
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	@Override
	public void process(Element element) throws XMPPException, XMLException {
		sessionObject.setStreamFeatures(element);
		observable.fireEvent(STREAM_FEATURES_RECEIVED, new StreamFeaturesReceivedEvent(element));
	}

	public void removeListener(EventType eventType, Listener<? extends StreamFeaturesReceivedEvent> listener) {
		observable.removeListener(eventType, listener);
	}

}
