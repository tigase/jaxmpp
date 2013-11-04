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
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.EventType;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 * Module for <a href='http://xmpp.org/rfcs/rfc6120.html#bind'>Resource
 * Binding</a>.
 */
public class ResourceBinderModule implements XmppModule {

	/**
	 * Event fires on binding error.
	 */
	public interface ResourceBindErrorHandler extends EventHandler {

		public static class ResourceBindErrorEvent extends JaxmppEvent<ResourceBindErrorHandler> {

			public static final EventType<ResourceBindErrorHandler> TYPE = new EventType<ResourceBindErrorHandler>();
			private ErrorCondition error;

			public ResourceBindErrorEvent(SessionObject sessionObject, ErrorCondition error) {
				super(TYPE, sessionObject);
				this.error = error;
			}

			@Override
			protected void dispatch(ResourceBindErrorHandler handler) {
				handler.onResourceBindError(sessionObject, error);
			}

			public ErrorCondition getError() {
				return error;
			}

			public void setError(ErrorCondition error) {
				this.error = error;
			}

		}

		void onResourceBindError(SessionObject sessionObject, ErrorCondition errorCondition);
	}

	/**
	 * Event fires on binding success.
	 */
	public interface ResourceBindSuccessHandler extends EventHandler {

		public static class ResourceBindSuccessEvent extends JaxmppEvent<ResourceBindSuccessHandler> {

			public static final EventType<ResourceBindSuccessHandler> TYPE = new EventType<ResourceBindSuccessHandler>();
			private JID bindedJid;

			public ResourceBindSuccessEvent(SessionObject sessionObject, JID jid) {
				super(TYPE, sessionObject);
				this.bindedJid = jid;
			}

			@Override
			protected void dispatch(ResourceBindSuccessHandler handler) {
				handler.onResourceBindSuccess(sessionObject, bindedJid);
			}

			public JID getBindedJid() {
				return bindedJid;
			}

			public void setBindedJid(JID bindedJid) {
				this.bindedJid = bindedJid;
			}

		}

		void onResourceBindSuccess(SessionObject sessionObject, JID bindedJid);
	}

	/**
	 * Property name for retrieve binded resource from
	 * {@linkplain SessionObject}.
	 */
	public static final String BINDED_RESOURCE_JID = "BINDED_RESOURCE_JID";

	public static JID getBindedJID(SessionObject sessionObject) {
		return sessionObject.getProperty(BINDED_RESOURCE_JID);
	}

	private final Context context;

	protected final Logger log;

	public ResourceBinderModule(Context context) {
		log = Logger.getLogger(this.getClass().getName());
		this.context = context;
	}

	public void addResourceBindErrorHandler(ResourceBindErrorHandler handler) {
		context.getEventBus().addHandler(ResourceBindErrorHandler.ResourceBindErrorEvent.TYPE, handler);
	}

	public void addResourceBindSuccessHandler(ResourceBindSuccessHandler handler) {
		context.getEventBus().addHandler(ResourceBindSuccessHandler.ResourceBindSuccessEvent.TYPE, handler);
	}

	public void bind() throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setXMLNS("jabber:client");
		iq.setType(StanzaType.set);

		Element bind = new DefaultElement("bind", null, "urn:ietf:params:xml:ns:xmpp-bind");
		iq.addChild(bind);
		bind.addChild(new DefaultElement("resource", (String) context.getSessionObject().getProperty(SessionObject.RESOURCE),
				null));

		context.getWriter().write(iq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
				ResourceBindErrorHandler.ResourceBindErrorEvent event = new ResourceBindErrorHandler.ResourceBindErrorEvent(
						context.getSessionObject(), error);
				context.getEventBus().fire(event, ResourceBinderModule.this);
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
					context.getSessionObject().setProperty(BINDED_RESOURCE_JID, jid);
					ResourceBindSuccessHandler.ResourceBindSuccessEvent event = new ResourceBindSuccessHandler.ResourceBindSuccessEvent(
							context.getSessionObject(), jid);
					context.getEventBus().fire(event, ResourceBinderModule.this);
				} else {
					ResourceBindErrorHandler.ResourceBindErrorEvent event = new ResourceBindErrorHandler.ResourceBindErrorEvent(
							context.getSessionObject(), null);
					context.getEventBus().fire(event, ResourceBinderModule.this);
				}
			}

			@Override
			public void onTimeout() throws JaxmppException {
				ResourceBindErrorHandler.ResourceBindErrorEvent event = new ResourceBindErrorHandler.ResourceBindErrorEvent(
						context.getSessionObject(), null);
				context.getEventBus().fire(event, ResourceBinderModule.this);
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

	public void removeResourceBindErrorHandler(ResourceBindErrorHandler handler) {
		context.getEventBus().remove(ResourceBindErrorHandler.ResourceBindErrorEvent.TYPE, handler);
	}

	public void removeResourceBindSuccessHandler(ResourceBindSuccessHandler handler) {
		context.getEventBus().remove(ResourceBindSuccessHandler.ResourceBindSuccessEvent.TYPE, handler);
	}

}