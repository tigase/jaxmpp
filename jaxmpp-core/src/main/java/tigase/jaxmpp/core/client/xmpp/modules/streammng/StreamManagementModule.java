package tigase.jaxmpp.core.client.xmpp.modules.streammng;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.AbstractSessionObject;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.forms.BooleanField;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
import tigase.jaxmpp.core.client.xmpp.modules.streammng.StreamManagementModule.StreamManagementEnabledHandler.StreamManagementEnabledEvent;
import tigase.jaxmpp.core.client.xmpp.modules.streammng.StreamManagementModule.StreamManagementFailedHandler.StreamManagementFailedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.streammng.StreamManagementModule.StreamResumedHandler.StreamResumedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.streammng.StreamManagementModule.UnacknowledgedHandler.UnacknowledgedEvent;

public class StreamManagementModule implements XmppModule {

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

	public interface StreamManagementEnabledHandler extends EventHandler {

		public static class StreamManagementEnabledEvent extends JaxmppEvent<StreamManagementEnabledHandler> {

			private Boolean resume;
			private String resumeId;

			public StreamManagementEnabledEvent(SessionObject sessionObject, Boolean resume, String id) {
				super(sessionObject);
				this.resume = resume;
				this.resumeId = id;

			}

			@Override
			protected void dispatch(StreamManagementEnabledHandler handler) {
				handler.onStreamManagementEnabled(sessionObject, resume, resumeId);
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

		void onStreamManagementEnabled(SessionObject sessionObject, Boolean resume, String resumeId);
	}

	public interface StreamManagementFailedHandler extends EventHandler {

		public static class StreamManagementFailedEvent extends JaxmppEvent<StreamManagementFailedHandler> {

			private ErrorCondition condition;

			public StreamManagementFailedEvent(SessionObject sessionObject, ErrorCondition condition) {
				super(sessionObject);
				this.condition = condition;
			}

			@Override
			protected void dispatch(StreamManagementFailedHandler handler) {
				handler.onStreamManagementFailed(sessionObject, condition);
			}

		}

		void onStreamManagementFailed(SessionObject sessionObject, ErrorCondition condition);
	}

	public interface StreamResumedHandler extends EventHandler {

		public static class StreamResumedEvent extends JaxmppEvent<StreamResumedHandler> {

			private Long h;

			private String previd;

			public StreamResumedEvent(SessionObject sessionObject, Long h, String previd) {
				super(sessionObject);
				this.h = h;
				this.previd = previd;
			}

			@Override
			protected void dispatch(StreamResumedHandler handler) throws JaxmppException {
				handler.onStreamResumed(sessionObject, h, previd);
			}

			public Long getH() {
				return h;
			}

			public String getPrevid() {
				return previd;
			}

			public void setH(Long h) {
				this.h = h;
			}

			public void setPrevid(String previd) {
				this.previd = previd;
			}

		}

		void onStreamResumed(SessionObject sessionObject, Long h, String previd) throws JaxmppException;
	}

	public interface UnacknowledgedHandler extends EventHandler {

		public static class UnacknowledgedEvent extends JaxmppEvent<UnacknowledgedHandler> {

			private List<Element> elements;

			public UnacknowledgedEvent(SessionObject sessionObject, List<Element> elements) {
				super(sessionObject);
				this.elements = elements;
			}

			@Override
			protected void dispatch(UnacknowledgedHandler handler) throws JaxmppException {
				handler.onUnacknowledged(sessionObject, elements);
			}

			public List<Element> getElements() {
				return elements;
			}

			public void setElements(List<Element> elements) {
				this.elements = elements;
			}

		}

		void onUnacknowledged(SessionObject sessionObject, List<Element> elements) throws JaxmppException;
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

	public static final String STREAM_MANAGEMENT_RESUMPTION_TIME_KEY = "urn:xmpp:sm:3#STREAM_MANAGEMENT_RESUMPTION_TIMEOUT_KEY";

	/**
	 * Property to keep Boolean if stream management is turned on.
	 */
	public final static String STREAM_MANAGEMENT_TURNED_ON_KEY = "urn:xmpp:sm:3#STREAM_MANAGEMENT_TURNED_ON";

	public static final String XMLNS = "urn:xmpp:sm:3";

	public static long getResumptionTime(final SessionObject sessionObject, long defaultValue) {
		try {
			Long x = sessionObject.getProperty(STREAM_MANAGEMENT_RESUMPTION_TIME_KEY);
			return x == null ? defaultValue : x.longValue();
		} catch (Exception e) {
			return defaultValue;
		}
	}

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
		final Element features = StreamFeaturesModule.getStreamFeatures(sessionObject);

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

	private final Context context;

	private final Criteria crit = ElementCriteria.xmlns(XMLNS);

	private final JaxmppCore jaxmpp;

	protected final Logger log;

	private final LinkedList<Element> outgoingQueue = new LinkedList<Element>();

	public StreamManagementModule(JaxmppCore jaxmpp, Context context) {
		log = Logger.getLogger(this.getClass().getName());
		this.jaxmpp = jaxmpp;
		this.context = context;

		jaxmpp.getEventBus().addHandler(Connector.StanzaSendingHandler.StanzaSendingEvent.class,
				new Connector.StanzaSendingHandler() {

					@Override
					public void onStanzaSending(SessionObject sessionObject, Element stanza) throws JaxmppException {
						processOutgoingElement(stanza);
					}
				});

	}

	public void addStreamManagementEnabledHandler(StreamManagementEnabledHandler handler) {
		this.context.getEventBus().addHandler(StreamManagementEnabledHandler.StreamManagementEnabledEvent.class, handler);
	}

	public void addStreamManagementFailedHandler(StreamManagementFailedHandler handler) {
		this.context.getEventBus().addHandler(StreamManagementFailedHandler.StreamManagementFailedEvent.class, handler);
	}

	public void addStreamResumedHandler(StreamResumedHandler handler) {
		this.context.getEventBus().addHandler(StreamResumedHandler.StreamResumedEvent.class, handler);
	}

	public void addUnacknowledgedHandler(UnacknowledgedHandler handler) {
		this.context.getEventBus().addHandler(UnacknowledgedHandler.UnacknowledgedEvent.class, handler);
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

		context.getWriter().write(request);
	}

	private Number getAckHValue(String key) {
		MutableLong v = context.getSessionObject().getProperty(key);
		if (v == null) {
			v = new MutableLong();
			context.getSessionObject().setProperty(key, v);
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
		MutableLong v = context.getSessionObject().getProperty(key);
		if (v == null) {
			v = new MutableLong();
			context.getSessionObject().setProperty(key, v);
		}
		++v.value;
		if (v.value < 0)
			v.value = 0;
		return v.value;
	}

	@Override
	public void process(Element element) throws XMPPException, XMLException, JaxmppException {
		final boolean enabled = isStreamManagementTurnedOn(context.getSessionObject());
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
				UnacknowledgedEvent event = new UnacknowledgedEvent(context.getSessionObject(), notSentElements);
				context.getEventBus().fire(event);
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
		context.getWriter().write(response);
	}

	private void processFailed(Element element) throws JaxmppException {
		List<Element> errors = element.getChildrenNS(XMPPException.XMLNS);

		context.getSessionObject().setProperty(STREAM_MANAGEMENT_TURNED_ON_KEY, Boolean.FALSE);
		context.getSessionObject().setProperty(Scope.stream, SM_ACK_ENABLED_KEY, Boolean.FALSE);
		context.getSessionObject().setProperty(STREAM_MANAGEMENT_RESUME_KEY, null);
		context.getSessionObject().setProperty(STREAM_MANAGEMENT_RESUMPTION_ID_KEY, null);

		XMPPException.ErrorCondition condition = ErrorCondition.unexpected_request;
		for (Element element2 : errors) {
			ErrorCondition tmp = XMPPException.ErrorCondition.getByElementName(element2.getName());
			if (tmp != null) {
				condition = tmp;
				break;
			}
		}

		StreamManagementFailedEvent event = new StreamManagementFailedEvent(context.getSessionObject(), condition);
		context.getEventBus().fire(event);
	}

	public boolean processIncomingStanza(Element element) throws XMLException {
		if (!isAckEnabled(context.getSessionObject()))
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
		if (!isAckEnabled(context.getSessionObject()))
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

		context.getSessionObject().setProperty(Scope.stream, SM_ACK_ENABLED_KEY, Boolean.TRUE);

		StreamResumedEvent event = new StreamResumedEvent(context.getSessionObject(), newH, element.getAttribute("previd"));
		context.getEventBus().fire(event);
	}

	private void processStreamManagementEnabled(Element element) throws JaxmppException {
		String id = element.getAttribute("id");
		String r = element.getAttribute("resume");
		String mx = element.getAttribute("max");
		Boolean resume = r == null ? false : BooleanField.parse(r);

		if (log.isLoggable(Level.INFO)) {
			log.info("Stream management is enabled. id=" + id + "; resume=" + r);
		}

		context.getSessionObject().setProperty(STREAM_MANAGEMENT_TURNED_ON_KEY, Boolean.TRUE);
		context.getSessionObject().setProperty(STREAM_MANAGEMENT_RESUME_KEY, resume);
		context.getSessionObject().setProperty(STREAM_MANAGEMENT_RESUMPTION_ID_KEY, id);
		context.getSessionObject().setProperty(Scope.stream, SM_ACK_ENABLED_KEY, Boolean.TRUE);

		if (mx != null) {
			context.getSessionObject().setProperty(STREAM_MANAGEMENT_RESUMPTION_TIME_KEY, Long.valueOf(mx));
		}

		StreamManagementEnabledEvent event = new StreamManagementEnabledEvent(context.getSessionObject(), resume, id);
		context.getEventBus().fire(event);
	}

	public void removeStreamManagementEnabledHandler(StreamManagementEnabledHandler handler) {
		this.context.getEventBus().remove(StreamManagementEnabledHandler.StreamManagementEnabledEvent.class, handler);
	}

	public void removeStreamManagementFailedHandler(StreamManagementFailedHandler handler) {
		this.context.getEventBus().remove(StreamManagementFailedHandler.StreamManagementFailedEvent.class, handler);
	}

	public void removeStreamResumedHandler(StreamResumedHandler handler) {
		this.context.getEventBus().remove(StreamResumedHandler.StreamResumedEvent.class, handler);
	}

	public void removeUnacknowledgedHandler(UnacknowledgedHandler handler) {
		this.context.getEventBus().remove(UnacknowledgedHandler.UnacknowledgedEvent.class, handler);
	}

	/**
	 * Request acknowledgement of received stanzas.
	 */
	public void request() throws JaxmppException {
		Long lr = context.getSessionObject().getProperty(LAST_REQUEST_TIMESTAMP_KEY);

		final long now = (new Date()).getTime();

		if (lr != null && now - lr < 1000)
			return;

		Element request = new DefaultElement("r", null, XMLNS);
		// context.getWriter().write(request);
		jaxmpp.getConnector().send(request);
		context.getSessionObject().setProperty(LAST_REQUEST_TIMESTAMP_KEY, now);
	}

	public void resume() throws JaxmppException {
		Element resume = new DefaultElement("resume", null, XMLNS);

		resume.setAttribute("h", getAckHValue(INCOMING_STREAM_H_KEY).toString());
		resume.setAttribute("previd", (String) context.getSessionObject().getProperty(STREAM_MANAGEMENT_RESUMPTION_ID_KEY));

		if (log.isLoggable(Level.INFO))
			log.info("Stream resumption");

		context.getWriter().write(resume);
	}

	private void setAckHValue(String key, Long value) {
		MutableLong v = context.getSessionObject().getProperty(key);
		if (v == null) {
			v = new MutableLong();
			context.getSessionObject().setProperty(key, v);
		}
		v.value = value == null ? 0 : value;
		if (v.value < 0)
			v.value = 0;
	}

}
