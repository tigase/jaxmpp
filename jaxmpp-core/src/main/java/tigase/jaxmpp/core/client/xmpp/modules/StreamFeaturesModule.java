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

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.criteria.Or;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.EventType;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule.StreamFeaturesReceivedHandler.StreamFeaturesReceivedEvent;

/**
 * Module for <a href=
 * 'http://xmpp.org/rfcs/rfc6120.html#streams-negotiation-features'>Stream
 * Features</a>.
 */
public class StreamFeaturesModule implements XmppModule {

	/**
	 * Event fires when stream features are received.
	 */
	public interface StreamFeaturesReceivedHandler extends EventHandler {

		public static class StreamFeaturesReceivedEvent extends JaxmppEvent<StreamFeaturesReceivedHandler> {

			public static final EventType<StreamFeaturesReceivedHandler> TYPE = new EventType<StreamFeaturesReceivedHandler>();

			private Element featuresElement;

			public StreamFeaturesReceivedEvent(SessionObject sessionObject, Element element) {
				super(TYPE, sessionObject);
				this.featuresElement = element;
			}

			@Override
			protected void dispatch(StreamFeaturesReceivedHandler handler) {
				handler.onStreamFeaturesReceived(sessionObject, featuresElement);
			}

			public Element getFeaturesElement() {
				return featuresElement;
			}

			public void setFeaturesElement(Element featuresElement) {
				this.featuresElement = featuresElement;
			}

		}

		void onStreamFeaturesReceived(SessionObject sessionObject, Element featuresElement);
	}

	private final static Criteria CRIT = new Or(new Criteria[] { ElementCriteria.name("stream:features"),
			ElementCriteria.name("features") });

	private final Context context;

	protected final Logger log;

	public StreamFeaturesModule(Context context) {
		this.context = context;
		log = Logger.getLogger(this.getClass().getName());
	}

	public void addStreamFeaturesReceivedHandler(StreamFeaturesReceivedHandler handler) {
		context.getEventBus().addHandler(StreamFeaturesReceivedHandler.StreamFeaturesReceivedEvent.TYPE, handler);
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
		context.getSessionObject().setStreamFeatures(element);
		context.getEventBus().fire(new StreamFeaturesReceivedEvent(context.getSessionObject(), element), this);
	}

	public void removeStreamFeaturesReceivedHandler(StreamFeaturesReceivedHandler handler) {
		context.getEventBus().remove(StreamFeaturesReceivedHandler.StreamFeaturesReceivedEvent.TYPE, handler);
	}

}