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

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class ResourceBinderModule implements XmppModule {

	public static final class ResourceBindEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private ErrorCondition error;

		private JID jid;

		public ResourceBindEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		public ErrorCondition getError() {
			return error;
		}

		public JID getJid() {
			return jid;
		}

		public void setError(ErrorCondition error) {
			this.error = error;
		}

		public void setJid(JID jid) {
			this.jid = jid;
		}
	}

	public static final String BINDED_RESOURCE_JID = "jaxmpp#bindedResource";

	public static final EventType ResourceBindError = new EventType();

	public static final EventType ResourceBindSuccess = new EventType();

	protected final Logger log;

	private final Observable observable;

	protected final SessionObject sessionObject;

	protected final PacketWriter writer;

	public ResourceBinderModule(Observable parentObservable, SessionObject sessionObject, PacketWriter packetWriter) {
		this.observable = ObservableFactory.instance(parentObservable);
		log = Logger.getLogger(this.getClass().getName());
		this.sessionObject = sessionObject;
		this.writer = packetWriter;
	}

	public void addListener(EventType eventType, Listener<ResourceBindEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void bind() throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setXMLNS("jabber:client");
		iq.setType(StanzaType.set);

		Element bind = new DefaultElement("bind", null, "urn:ietf:params:xml:ns:xmpp-bind");
		iq.addChild(bind);
		bind.addChild(new DefaultElement("resource", (String) sessionObject.getProperty(SessionObject.RESOURCE), null));

		writer.write(iq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
				ResourceBindEvent event = new ResourceBindEvent(ResourceBindError, sessionObject);
				event.setError(error);
				observable.fireEvent(ResourceBindError, event);
			}

			@Override
			public void onSuccess(Stanza responseStanza) throws JaxmppException {
				String name = null;
				Element bind = responseStanza.getChildrenNS("bind", "urn:ietf:params:xml:ns:xmpp-bind");
				if (bind != null) {
					Element c = bind.getFirstChild();
					name = c != null ? c.getValue() : null;
				}
				if (name != null) {
					JID jid = JID.jidInstance(name);
					sessionObject.setProperty(BINDED_RESOURCE_JID, jid);
					ResourceBindEvent event = new ResourceBindEvent(ResourceBindSuccess, sessionObject);
					event.setJid(jid);
					observable.fireEvent(ResourceBindSuccess, event);
				} else {
					ResourceBindEvent event = new ResourceBindEvent(ResourceBindError, sessionObject);
					observable.fireEvent(ResourceBindError, event);
				}
			}

			@Override
			public void onTimeout() throws JaxmppException {
				ResourceBindEvent event = new ResourceBindEvent(ResourceBindError, sessionObject);
				observable.fireEvent(ResourceBindError, event);
			}
		});
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
	public void process(Element element) throws XMPPException, XMLException {
	}

	public void removeListener(EventType eventType, Listener<ResourceBindEvent> listener) {
		observable.removeListener(eventType, listener);
	}

}