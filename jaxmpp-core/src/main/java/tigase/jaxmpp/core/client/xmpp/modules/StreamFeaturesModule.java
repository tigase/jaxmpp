/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
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

import java.util.logging.Logger;

import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.criteria.Or;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.Element;

/**
 * Module for <a href=
 * 'http://xmpp.org/rfcs/rfc6120.html#streams-negotiation-features'>Stream
 * Features</a>.
 */
public class StreamFeaturesModule implements XmppModule {

	public static class StreamFeaturesReceivedEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private Element features;

		public StreamFeaturesReceivedEvent(Element features, SessionObject sessionObject) {
			super(StreamFeaturesReceived, sessionObject);
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

	/**
	 * Event fires when stream features are received.
	 */
	public static final EventType StreamFeaturesReceived = new EventType();

	protected final Logger log;

	private final Observable observable;

	protected final PacketWriter packetWriter;

	protected final SessionObject sessionObject;

	public StreamFeaturesModule(Observable parentObservable, SessionObject sessionObject, PacketWriter packetWriter) {
		this.observable = ObservableFactory.instance(parentObservable);
		log = Logger.getLogger(this.getClass().getName());
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
	public void process(Element element) throws JaxmppException {
		sessionObject.setStreamFeatures(element);
		observable.fireEvent(StreamFeaturesReceived, new StreamFeaturesReceivedEvent(element, sessionObject));
	}

	public void removeListener(EventType eventType, Listener<? extends StreamFeaturesReceivedEvent> listener) {
		observable.removeListener(eventType, listener);
	}

}