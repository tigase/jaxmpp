package tigase.jaxmpp.core.client.xmpp.modules.streammng;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.forms.BooleanField;

public class StreamManagementModule implements XmppModule {

	public static abstract class AbstractStreamManagementEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		protected AbstractStreamManagementEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}
	}

	public static class AckEnabledEvent extends AckEvent {

		private static final long serialVersionUID = 1L;

		private Boolean resume;

		private String resumeId;

		AckEnabledEvent(SessionObject sessionObject, Boolean resume, String id) {
			super(AckEnabled, sessionObject);
			this.resume = resume;
			this.resumeId = id;
		}

		public Boolean getResume() {
			return resume;
		}

		public String getResumeId() {
			return resumeId;
		}

		public void setResume(Boolean resume) {
			this.resume = resume;
		}

		public void setResumeId(String resumeId) {
			this.resumeId = resumeId;
		}
	}

	public static abstract class AckEvent extends AbstractStreamManagementEvent {

		private static final long serialVersionUID = 1L;

		protected AckEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}
	}

	private static class MutableLong extends Number {

		private static final long serialVersionUID = 1L;

		private long value;

		@Override
		public double doubleValue() {
			return value;
		}

		@Override
		public float floatValue() {
			return value;
		}

		@Override
		public int intValue() {
			return (int) value;
		}

		@Override
		public long longValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}

	public static class UnacknowledgedEvent extends AckEvent {

		private static final long serialVersionUID = 1L;

		private List<Element> elements = new ArrayList<Element>();

		UnacknowledgedEvent(SessionObject sessionObject) {
			super(Unacknowledged, sessionObject);
		}

		public List<Element> getElements() {
			return elements;
		}

		public void setElements(List<Element> elements) {
			this.elements = elements;
		}
	}

	public static final EventType AckEnabled = new EventType();

	public static final String INCOMING_STREAM_H_KEY = "INCOMING_STREAM_H";

	private final static String LAST_REQUEST_TIMESTAMP_KEY = "urn:xmpp:sm:3-lastRequestTimestamp";

	public static final String OUTGOING_STREAM_H_KEY = "OUTGOING_STREAM_H";

	/**
	 * Property to disable stream management module.
	 */
	public final static String STREAM_MANAGEMENT_DISABLED_KEY = "STREAM_MANAGEMENT_DISABLED";

	public final static String STREAM_MANAGEMENT_RESUME_KEY = "STREAM_MANAGEMENT_RESUME";

	public final static String STREAM_MANAGEMENT_RESUMPTION_ID_KEY = "STREAM_MANAGEMENT_RESUMPTION_ID";

	/**
	 * Property to keep Boolean if stream management is turned on.
	 */
	public final static String STREAM_MANAGEMENT_TURNED_ON_KEY = "STREAM_MANAGEMENT_TURNED_ON";

	public static final EventType Unacknowledged = new EventType();

	public static final String XMLNS = "urn:xmpp:sm:3";

	public static boolean isResumptionEnabled(final SessionObject sessionObject) {
		Boolean en = sessionObject.getProperty(STREAM_MANAGEMENT_TURNED_ON_KEY);
		Boolean re = sessionObject.getProperty(STREAM_MANAGEMENT_RESUME_KEY);
		String id = sessionObject.getProperty(STREAM_MANAGEMENT_RESUMPTION_ID_KEY);
		return en != null && en && re != null && re && id != null;
	}

	public static boolean isStreamManagementAvailable(SessionObject sessionObject) throws JaxmppException {
		final Element features = sessionObject.getStreamFeatures();

		boolean supported = features != null && features.getChildrenNS("sm", XMLNS) != null;

		return supported;
	}

	public static boolean isStreamManagementTurnedOn(final SessionObject sessionObject) {
		Boolean x = sessionObject.getProperty(STREAM_MANAGEMENT_TURNED_ON_KEY);
		return x != null && x;
	}

	private final Criteria crit = ElementCriteria.xmlns(XMLNS);

	private final JaxmppCore jaxmpp;

	protected final Logger log;

	private final Observable observable;

	private final LinkedList<Element> outgoingQueue = new LinkedList<Element>();

	private final SessionObject sessionObject;

	private final PacketWriter writer;

	public StreamManagementModule(JaxmppCore jaxmpp, Observable parentObservable, SessionObject sessionObject,
			PacketWriter writer) {
		log = Logger.getLogger(this.getClass().getName());
		this.jaxmpp = jaxmpp;
		this.observable = ObservableFactory.instance(parentObservable);
		this.sessionObject = sessionObject;
		this.writer = writer;

		jaxmpp.addListener(Connector.StanzaSending, new Listener<ConnectorEvent>() {

			@Override
			public void handleEvent(ConnectorEvent be) throws JaxmppException {
				processOutgoingElement(be.getStanza());
			}
		});

	}

	/**
	 * Adds a listener bound by the given event type.
	 * 
	 * @param eventType
	 *            type of event
	 * @param listener
	 *            the listener
	 */
	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	/**
	 * Add a listener bound by the all event types.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addListener(Listener<? extends BaseEvent> listener) {
		observable.addListener(listener);
	}

	/**
	 * Client enables stream management.
	 */
	public void enable() throws JaxmppException {
		if (log.isLoggable(Level.INFO)) {
			log.info("Enabling stream management");
		}
		Element request = new DefaultElement("enable", null, XMLNS);
		writer.write(request);
	}

	private Number getAckHValue(String key) {
		MutableLong v = sessionObject.getProperty(key);
		if (v == null) {
			v = new MutableLong();
			sessionObject.setProperty(key, v);
		}
		return v;
	}

	@Override
	public Criteria getCriteria() {
		return crit;
	}

	@Override
	public String[] getFeatures() {
		// TODO Auto-generated method stub
		return null;
	}

	private long incrementAckHValue(String key) {
		MutableLong v = sessionObject.getProperty(key);
		if (v == null) {
			v = new MutableLong();
			sessionObject.setProperty(key, v);
		}
		++v.value;
		return v.value;
	}

	@Override
	public void process(Element element) throws XMPPException, XMLException, JaxmppException {
		final boolean enabled = isStreamManagementTurnedOn(sessionObject);
		if ("enabled".equals(element.getName()) && element.getXMLNS() != null && XMLNS.endsWith(element.getXMLNS())) {
			processStreamManagementEnabled(element);
		} else if (enabled && "r".equals(element.getName()) && element.getXMLNS() != null && XMLNS.endsWith(element.getXMLNS())) {
			processAckRequest(element);
		} else if (enabled && "a".equals(element.getName()) && element.getXMLNS() != null && XMLNS.endsWith(element.getXMLNS())) {
			throw new JaxmppException("Should be processed already");
			// processAckAnswer(element);
		} else
			throw new XMPPException(ErrorCondition.feature_not_implemented);
	}

	private void processAckAnswer(Element element) throws XMLException {
		String hs = element.getAttribute("h");
		try {
			final long oldH = getAckHValue(OUTGOING_STREAM_H_KEY).longValue();
			final long newH = Long.parseLong(hs);

			log.fine("Current h=" + oldH + "; received h=" + newH);

			if (oldH > newH) {
				ArrayList<Element> notSentElements = new ArrayList<Element>();
				synchronized (this.outgoingQueue) {
					for (int i = 0; i < oldH - newH; i++) {
						if (!outgoingQueue.isEmpty()) {
							Element ee = this.outgoingQueue.removeLast();
							notSentElements.add(0, ee);
						}
					}
					this.outgoingQueue.clear();
				}
				UnacknowledgedEvent event = new UnacknowledgedEvent(sessionObject);
				event.setElements(notSentElements);
				observable.fireEvent(event);
			} else {
				synchronized (this.outgoingQueue) {
					this.outgoingQueue.clear();
				}
			}
			setAckHValue(OUTGOING_STREAM_H_KEY, newH);
		} catch (Exception e) {

		}

	}

	private void processAckRequest(Element element) throws JaxmppException {
		Element response = new DefaultElement("a", null, XMLNS);
		response.setAttribute("h", getAckHValue(INCOMING_STREAM_H_KEY).toString());
		writer.write(response);
	}

	public boolean processIncomingStanza(Element element) throws XMLException {
		if (!isStreamManagementTurnedOn(sessionObject))
			return false;

		if (element.getXMLNS() != null && XMLNS.endsWith(element.getXMLNS())) {

			if ("a".equals(element.getName())) {
				processAckAnswer(element);
				return true;
			} else
				return false;
		}

		incrementAckHValue(INCOMING_STREAM_H_KEY);

		return false;
	}

	public void processOutgoingElement(final Element element) throws JaxmppException {
		if (!isStreamManagementTurnedOn(sessionObject))
			return;
		if (("r".equals(element.getName()) || "a".equals(element.getName())) && element.getXMLNS() != null
				&& XMLNS.endsWith(element.getXMLNS()))
			return;

		synchronized (this.outgoingQueue) {
			incrementAckHValue(OUTGOING_STREAM_H_KEY);
			outgoingQueue.offer(element);
		}
		if (outgoingQueue.size() > 3) {
			Runnable r = new Runnable() {

				@Override
				public void run() {
					try {
						request();
					} catch (JaxmppException e) {
						log.log(Level.WARNING, "Can't send ACK request!");
					}
				}
			};
			jaxmpp.execute(r);
		}
	}

	private void processStreamManagementEnabled(Element element) throws JaxmppException {
		String id = element.getAttribute("id");
		String r = element.getAttribute("resume");
		Boolean resume = r == null ? false : BooleanField.parse(r);

		if (log.isLoggable(Level.INFO)) {
			log.info("Stream management is enabled. id=" + id + "; resume=" + r);
		}

		sessionObject.setProperty(STREAM_MANAGEMENT_TURNED_ON_KEY, Boolean.TRUE);
		sessionObject.setProperty(STREAM_MANAGEMENT_RESUME_KEY, resume);
		sessionObject.setProperty(STREAM_MANAGEMENT_RESUMPTION_ID_KEY, id);

		AckEnabledEvent event = new AckEnabledEvent(sessionObject, resume, id);
		observable.fireEvent(event);
	}

	/**
	 * Removes all listeners.
	 */
	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	/**
	 * Removes a listener.
	 * 
	 * @param eventType
	 *            type of event
	 * @param listener
	 *            listener
	 */
	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param listener
	 *            listener
	 */
	public void removeListener(Listener<? extends BaseEvent> listener) {
		observable.removeListener(listener);
	}

	/**
	 * Request acknowledgement of received stanzas.
	 */
	public void request() throws JaxmppException {
		Long lr = sessionObject.getProperty(LAST_REQUEST_TIMESTAMP_KEY);

		final long now = (new Date()).getTime();

		if (lr != null && now - lr < 1000)
			return;

		Element request = new DefaultElement("r", null, XMLNS);
		// writer.write(request);
		jaxmpp.getConnector().send(request);
		sessionObject.setProperty(LAST_REQUEST_TIMESTAMP_KEY, now);
	}

	private void setAckHValue(String key, Long value) {
		MutableLong v = sessionObject.getProperty(key);
		if (v == null) {
			v = new MutableLong();
			sessionObject.setProperty(key, v);
		}
		v.value = value == null ? 0 : value;
	}

}
