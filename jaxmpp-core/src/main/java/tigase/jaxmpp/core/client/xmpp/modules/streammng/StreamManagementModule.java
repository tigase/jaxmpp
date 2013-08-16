package tigase.jaxmpp.core.client.xmpp.modules.streammng;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.AbstractSessionObject;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
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

	public static class StreamManagementEnabledEvent extends AbstractStreamManagementEvent {

		private static final long serialVersionUID = 1L;

		private Boolean resume;

		private String resumeId;

		StreamManagementEnabledEvent(SessionObject sessionObject, Boolean resume, String id) {
			super(StreamManagementEnabled, sessionObject);
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

	public static class StreamManagementFailedEvent extends AbstractStreamManagementEvent {

		private static final long serialVersionUID = 1L;

		private ErrorCondition condition;

		public StreamManagementFailedEvent(SessionObject sessionObject, ErrorCondition condition) {
			super(StreamManagementFailed, sessionObject);
			this.condition = condition;
		}

		public ErrorCondition getCondition() {
			return condition;
		}

		public void setCondition(ErrorCondition condition) {
			this.condition = condition;
		}
	}

	public static class StreamResumedEvent extends AbstractStreamManagementEvent {

		private static final long serialVersionUID = 1L;

		private Long h;

		private String previd;

		public StreamResumedEvent(EventType type, SessionObject sessionObject, Long h, String previd) {
			super(type, sessionObject);
			this.h = h;
			this.previd = previd;
		}

		protected StreamResumedEvent(SessionObject sessionObject, Long h, String previd) {
			super(StreamResumed, sessionObject);
			this.h = h;
			this.previd = previd;
		}
	}

	public static class UnacknowledgedEvent extends AbstractStreamManagementEvent {

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

	public static final String INCOMING_STREAM_H_KEY = "urn:xmpp:sm:3#INCOMING_STREAM_H";

	private final static String LAST_REQUEST_TIMESTAMP_KEY = "urn:xmpp:sm:3#lastRequestTimestamp";

	public static final String OUTGOING_STREAM_H_KEY = "urn:xmpp:sm:3#OUTGOING_STREAM_H";

	private static final String SM_ACK_ENABLED_KEY = "urn:xmpp:sm:3#SM_ACK_ENABLED";

	/**
	 * Property to disable stream management module.
	 */
	public final static String STREAM_MANAGEMENT_DISABLED_KEY = "urn:xmpp:sm:3#STREAM_MANAGEMENT_DISABLED";

	public final static String STREAM_MANAGEMENT_RESUME_KEY = "urn:xmpp:sm:3#STREAM_MANAGEMENT_RESUME";

	public final static String STREAM_MANAGEMENT_RESUMPTION_ID_KEY = "urn:xmpp:sm:3#STREAM_MANAGEMENT_RESUMPTION_ID";

	/**
	 * Property to keep Boolean if stream management is turned on.
	 */
	public final static String STREAM_MANAGEMENT_TURNED_ON_KEY = "urn:xmpp:sm:3#STREAM_MANAGEMENT_TURNED_ON";

	public static final EventType StreamManagementEnabled = new EventType();

	public static final EventType StreamManagementFailed = new EventType();

	public static final EventType StreamResumed = new EventType();

	public static final EventType Unacknowledged = new EventType();

	public static final String XMLNS = "urn:xmpp:sm:3";

	public static boolean isAckEnabled(final SessionObject sessionObject) {
		Boolean x = sessionObject.getProperty(SM_ACK_ENABLED_KEY);
		return x != null && x;
	}

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

	public static void reset(AbstractSessionObject sessionObject) {
		sessionObject.setProperty(STREAM_MANAGEMENT_TURNED_ON_KEY, Boolean.FALSE);
		sessionObject.setProperty(STREAM_MANAGEMENT_RESUME_KEY, null);
		sessionObject.setProperty(STREAM_MANAGEMENT_RESUMPTION_ID_KEY, null);
		sessionObject.setProperty(Scope.stream, SM_ACK_ENABLED_KEY, Boolean.FALSE);
		sessionObject.setProperty(LAST_REQUEST_TIMESTAMP_KEY, null);

		sessionObject.setProperty(OUTGOING_STREAM_H_KEY, null);
		sessionObject.setProperty(INCOMING_STREAM_H_KEY, null);
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

		request.setAttribute("resume", "true");

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
		return null;
	}

	private long incrementAckHValue(String key) {
		MutableLong v = sessionObject.getProperty(key);
		if (v == null) {
			v = new MutableLong();
			sessionObject.setProperty(key, v);
		}
		++v.value;
		if (v.value < 0)
			v.value = 0;
		return v.value;
	}

	@Override
	public void process(Element element) throws XMPPException, XMLException, JaxmppException {
		final boolean enabled = isStreamManagementTurnedOn(sessionObject);
		if ("resumed".equals(element.getName()) && element.getXMLNS() != null && XMLNS.endsWith(element.getXMLNS())) {
			processResumed(element);
		} else if ("failed".equals(element.getName()) && element.getXMLNS() != null && XMLNS.endsWith(element.getXMLNS())) {
			processFailed(element);
		} else if ("enabled".equals(element.getName()) && element.getXMLNS() != null && XMLNS.endsWith(element.getXMLNS())) {
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

	private void processFailed(Element element) throws JaxmppException {
		List<Element> errors = element.getChildrenNS(XMPPException.XMLNS);

		sessionObject.setProperty(STREAM_MANAGEMENT_TURNED_ON_KEY, Boolean.FALSE);
		sessionObject.setProperty(Scope.stream, SM_ACK_ENABLED_KEY, Boolean.FALSE);
		sessionObject.setProperty(STREAM_MANAGEMENT_RESUME_KEY, null);
		sessionObject.setProperty(STREAM_MANAGEMENT_RESUMPTION_ID_KEY, null);

		XMPPException.ErrorCondition condition = ErrorCondition.unexpected_request;
		for (Element element2 : errors) {
			ErrorCondition tmp = XMPPException.ErrorCondition.getByElementName(element2.getName());
			if (tmp != null) {
				condition = tmp;
				break;
			}
		}

		StreamManagementFailedEvent event = new StreamManagementFailedEvent(sessionObject, condition);
		observable.fireEvent(event);
	}

	public boolean processIncomingStanza(Element element) throws XMLException {
		if (!isAckEnabled(sessionObject))
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
		if (!isAckEnabled(sessionObject))
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

	private void processResumed(Element element) throws JaxmppException {
		String hs = element.getAttribute("h");
		final Long newH = hs == null ? null : Long.parseLong(hs);

		sessionObject.setProperty(Scope.stream, SM_ACK_ENABLED_KEY, Boolean.TRUE);

		StreamResumedEvent event = new StreamResumedEvent(sessionObject, newH, element.getAttribute("previd"));
		observable.fireEvent(event);
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
		sessionObject.setProperty(Scope.stream, SM_ACK_ENABLED_KEY, Boolean.TRUE);

		StreamManagementEnabledEvent event = new StreamManagementEnabledEvent(sessionObject, resume, id);
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

	public void resume() throws JaxmppException {
		Element resume = new DefaultElement("resume", null, XMLNS);

		resume.setAttribute("h", getAckHValue(INCOMING_STREAM_H_KEY).toString());
		resume.setAttribute("previd", (String) sessionObject.getProperty(STREAM_MANAGEMENT_RESUMPTION_ID_KEY));

		if (log.isLoggable(Level.INFO))
			log.info("Stream resumption");

		writer.write(resume);
	}

	private void setAckHValue(String key, Long value) {
		MutableLong v = sessionObject.getProperty(key);
		if (v == null) {
			v = new MutableLong();
			sessionObject.setProperty(key, v);
		}
		v.value = value == null ? 0 : value;
		if (v.value < 0)
			v.value = 0;
	}

}
