/*
 * StreamFeaturesModule.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.jaxmpp.core.client.xmpp.modules;

import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.criteria.Or;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule.StreamFeaturesReceivedHandler.StreamFeaturesReceivedEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.StreamPacket;
import tigase.jaxmpp.core.client.xmpp.stream.XMPPStream;
import tigase.jaxmpp.core.client.xmpp.stream.XmppStreamsManager;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Module for <a href=
 * 'http://xmpp.org/rfcs/rfc6120.html#streams-negotiation-features'>Stream
 * Features</a>.
 */
public class StreamFeaturesModule
		implements XmppModule, ContextAware, InitializingModule {

	public final static String CACHE_PROVIDER_KEY = "StreamFeaturesModule#CACHE_PROVIDER";
	private final static Criteria CRIT = new Or(ElementCriteria.name("stream:features"),
												ElementCriteria.name("features"));
	private final static String STREAMS_FEATURES_LIST_KEY = "StreamFeaturesModule#STREAMS_FEATURES_LIST";
	private final static String PIPELINING_ACTIVE_KEY = "StreamFeaturesModule#PIPELINING_ACTIVE";
	private final static String EMBEDDED_STREAMS_COUNTER_KEY = "StreamFeaturesModule#EMBEDDED_STREAMS_COUNTER";
	protected final Logger log;

	private Context context;
	private final SessionEstablishmentModule.SessionEstablishmentSuccessHandler sessionEstablishmentHandler = new SessionEstablishmentModule.SessionEstablishmentSuccessHandler() {
		@Override
		public void onSessionEstablishmentSuccess(SessionObject sessionObject) throws JaxmppException {
			onSessionEstablish(sessionObject);
		}
	};
	private final Connector.StreamRestartedHandler streamRestartedHandler = new Connector.StreamRestartedHandler() {
		@Override
		public void onStreamRestarted(SessionObject sessionObject) throws JaxmppException {
			streamRestarted(sessionObject);
		}
	};
	private final Connector.StreamTerminatedHandler streamTerminatedHandler = new Connector.StreamTerminatedHandler() {
		@Override
		public void onStreamTerminated(SessionObject sessionObject) throws JaxmppException {
			connectorDisconnected(sessionObject);
		}
	};
	private final Connector.DisconnectedHandler disconnectedHandler = new Connector.DisconnectedHandler() {
		@Override
		public void onDisconnected(SessionObject sessionObject) {
			connectorDisconnected(sessionObject);
		}
	};

	public static Element getStreamFeatures(SessionObject sessionObject) {
		XmppStreamsManager sm = XmppStreamsManager.getStreamsManager(sessionObject);
		return sm == null ? null : sm.getDefaultStream().getFeatures();
	}

	public static boolean isPipeliningActive(SessionObject sessionObject) {
		Boolean x = sessionObject.getProperty(PIPELINING_ACTIVE_KEY);
		return x == null ? false : x;
	}

	public static void setCacheProvider(SessionObject sessionObject, CacheProvider provider) {
		sessionObject.setProperty(SessionObject.Scope.user, CACHE_PROVIDER_KEY, provider);
	}

	public StreamFeaturesModule() {
		log = Logger.getLogger(this.getClass().getName());
	}

	public void addStreamFeaturesReceivedHandler(StreamFeaturesReceivedHandler handler) {
		context.getEventBus().addHandler(StreamFeaturesReceivedHandler.StreamFeaturesReceivedEvent.class, handler);
	}

	@Override
	public void afterRegister() {
	}

	@Override
	public void beforeRegister() {
		context.getEventBus()
				.addHandler(
						SessionEstablishmentModule.SessionEstablishmentSuccessHandler.SessionEstablishmentSuccessEvent.class,
						sessionEstablishmentHandler);
		context.getEventBus().addHandler(Connector.DisconnectedHandler.DisconnectedEvent.class, disconnectedHandler);
		context.getEventBus()
				.addHandler(Connector.StreamTerminatedHandler.StreamTerminatedEvent.class, streamTerminatedHandler);
		context.getEventBus()
				.addHandler(Connector.StreamRestartedHandler.StreamRestaredEvent.class, streamRestartedHandler);
	}

	@Override
	public void beforeUnregister() {
		context.getEventBus()
				.remove(Connector.StreamTerminatedHandler.StreamTerminatedEvent.class, streamTerminatedHandler);
		context.getEventBus()
				.remove(SessionEstablishmentModule.SessionEstablishmentSuccessHandler.SessionEstablishmentSuccessEvent.class,
						sessionEstablishmentHandler);
		context.getEventBus().remove(Connector.DisconnectedHandler.DisconnectedEvent.class, disconnectedHandler);
		context.getEventBus()
				.remove(Connector.StreamRestartedHandler.StreamRestaredEvent.class, streamRestartedHandler);

	}

	private void connectorDisconnected(SessionObject sessionObject) {
		setCounter(0);
		getStreamsFeaturesList().clear();
	}

	private int getCounter() {
		Integer x = context.getSessionObject().getProperty(EMBEDDED_STREAMS_COUNTER_KEY);
		return x == null ? 0 : x;
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	private ArrayList<Element> getStreamsFeaturesList() {
		ArrayList<Element> e = context.getSessionObject().getProperty(STREAMS_FEATURES_LIST_KEY);
		if (e == null) {
			e = new ArrayList<>();
			context.getSessionObject().setProperty(SessionObject.Scope.session, STREAMS_FEATURES_LIST_KEY, e);
		}
		return e;
	}

	private void onSessionEstablish(SessionObject sessionObject) {
		final CacheProvider provider = sessionObject.getProperty(CACHE_PROVIDER_KEY);
		final ArrayList<Element> list = getStreamsFeaturesList();
		try {
			int count = 0;
			for (Element element : list) {
				if (element.getChildrenNS("pipelining", "urn:xmpp:features:pipelining") != null) {
					++count;
				}
			}

			if (provider != null && count > 0) {
				provider.save(sessionObject, list);
				getStreamsFeaturesList().clear();
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(Element element) throws JaxmppException {
		if (element instanceof StreamPacket) {
			XMPPStream xmppStream = ((StreamPacket) element).getXmppStream();
			xmppStream.setFeatures(element);
		}

		getStreamsFeaturesList().add(element);

		if (!isPipeliningActive(context.getSessionObject())) {
			context.getEventBus().fire(new StreamFeaturesReceivedEvent(context.getSessionObject(), element));
		}
	}

	public void removeStreamFeaturesReceivedHandler(StreamFeaturesReceivedHandler handler) {
		context.getEventBus().remove(StreamFeaturesReceivedHandler.StreamFeaturesReceivedEvent.class, handler);
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}

	private int setCounter(int value) {
		context.getSessionObject().setProperty(EMBEDDED_STREAMS_COUNTER_KEY, value);
		return value;
	}

	private void streamRestarted(SessionObject sessionObject) throws XMLException {
		final int c = getCounter();

		CacheProvider provider = sessionObject.getProperty(CACHE_PROVIDER_KEY);

		if (provider != null) {
			ArrayList<Element> data = provider.load(sessionObject);
			log.finest("Cached features: " + data);
			if (data != null && data.size() > c) {
				Element sfe = data.get(c);
				XmppStreamsManager sm = XmppStreamsManager.getStreamsManager(sessionObject);
				sm.getDefaultStream().setFeatures(sfe);
				sessionObject.setProperty(PIPELINING_ACTIVE_KEY, true);
				context.getEventBus().fire(new StreamFeaturesReceivedEvent(context.getSessionObject(), sfe));
				log.fine("Pipelining is enabled");
				log.fine("Used cached features: " + sfe.getAsString());
			} else {
				log.fine("Pipelining is disabled");
				sessionObject.setProperty(PIPELINING_ACTIVE_KEY, false);
			}
		} else {
			log.fine("Pipelining is disabled");
			sessionObject.setProperty(PIPELINING_ACTIVE_KEY, false);
		}
		setCounter(c + 1);
	}

	public interface CacheProvider {

		ArrayList<Element> load(SessionObject sessionObject);

		void save(SessionObject sessionObject, ArrayList<Element> features);
	}

	/**
	 * Event fires when stream features are received.
	 */
	public interface StreamFeaturesReceivedHandler
			extends EventHandler {

		void onStreamFeaturesReceived(SessionObject sessionObject, Element featuresElement) throws JaxmppException;

		class StreamFeaturesReceivedEvent
				extends JaxmppEvent<StreamFeaturesReceivedHandler> {

			private Element featuresElement;

			public StreamFeaturesReceivedEvent(SessionObject sessionObject, Element element) {
				super(sessionObject);
				this.featuresElement = element;
			}

			@Override
			public void dispatch(StreamFeaturesReceivedHandler handler) throws JaxmppException {
				handler.onStreamFeaturesReceived(sessionObject, featuresElement);
			}

			public Element getFeaturesElement() {
				return featuresElement;
			}

			public void setFeaturesElement(Element featuresElement) {
				this.featuresElement = featuresElement;
			}

			@Override
			public String toString() {
				try {
					return "StreamFeaturesReceivedEvent[" +
							(featuresElement == null ? "NULL" : featuresElement.getAsString()) + "]";
				} catch (XMLException e) {
					return "StreamFeaturesReceivedEvent[---ERROR---]";
				}
			}

		}
	}

}