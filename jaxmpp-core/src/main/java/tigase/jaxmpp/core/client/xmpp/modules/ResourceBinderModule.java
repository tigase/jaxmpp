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

import tigase.jaxmpp.core.client.*;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import java.util.logging.Logger;

/**
 * Module for <a href='http://xmpp.org/rfcs/rfc6120.html#bind'>Resource
 * Binding</a>.
 */
public class ResourceBinderModule implements XmppModule, ContextAware {

	/**
	 * Property name for retrieve binded resource from
	 * {@linkplain SessionObject}.
	 */
	public static final String BINDED_RESOURCE_JID = "BINDED_RESOURCE_JID";
	protected final Logger log;
	private Context context;

	public ResourceBinderModule() {
		log = Logger.getLogger(this.getClass().getName());
	}

	public static JID getBindedJID(SessionObject sessionObject) {
		return sessionObject.getProperty(BINDED_RESOURCE_JID);
	}

	public void addResourceBindErrorHandler(ResourceBindErrorHandler handler) {
		context.getEventBus().addHandler(ResourceBindErrorHandler.ResourceBindErrorEvent.class, handler);
	}

	public void addResourceBindSuccessHandler(ResourceBindSuccessHandler handler) {
		context.getEventBus().addHandler(ResourceBindSuccessHandler.ResourceBindSuccessEvent.class, handler);
	}

	public void bind() throws JaxmppException {
		IQ iq = IQ.create();
		iq.setXMLNS("jabber:client");
		iq.setType(StanzaType.set);

		Element bind = ElementFactory.create("bind", null, "urn:ietf:params:xml:ns:xmpp-bind");
		iq.addChild(bind);
		bind.addChild(ElementFactory.create("resource",
				(String) context.getSessionObject().getProperty(SessionObject.RESOURCE), null));

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

	public JID getBindedJID() {
		return getBindedJID(context.getSessionObject());
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
		context.getEventBus().remove(ResourceBindErrorHandler.ResourceBindErrorEvent.class, handler);
	}

	public void removeResourceBindSuccessHandler(ResourceBindSuccessHandler handler) {
		context.getEventBus().remove(ResourceBindSuccessHandler.ResourceBindSuccessEvent.class, handler);
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * Event fires on binding error.
	 */
	public interface ResourceBindErrorHandler extends EventHandler {

		void onResourceBindError(SessionObject sessionObject, ErrorCondition errorCondition);

		class ResourceBindErrorEvent extends JaxmppEvent<ResourceBindErrorHandler> {

			private ErrorCondition error;

			public ResourceBindErrorEvent(SessionObject sessionObject, ErrorCondition error) {
				super(sessionObject);
				this.error = error;
			}

			@Override
			public void dispatch(ResourceBindErrorHandler handler) {
				handler.onResourceBindError(sessionObject, error);
			}

			public ErrorCondition getError() {
				return error;
			}

			public void setError(ErrorCondition error) {
				this.error = error;
			}

		}
	}

	/**
	 * Event fires on binding success.
	 */
	public interface ResourceBindSuccessHandler extends EventHandler {

		void onResourceBindSuccess(SessionObject sessionObject, JID bindedJid) throws JaxmppException;

		class ResourceBindSuccessEvent extends JaxmppEvent<ResourceBindSuccessHandler> {

			private JID bindedJid;

			public ResourceBindSuccessEvent(SessionObject sessionObject, JID jid) {
				super(sessionObject);
				this.bindedJid = jid;
			}

			@Override
			public void dispatch(ResourceBindSuccessHandler handler) throws JaxmppException {
				handler.onResourceBindSuccess(sessionObject, bindedJid);
			}

			public JID getBindedJid() {
				return bindedJid;
			}

			public void setBindedJid(JID bindedJid) {
				this.bindedJid = bindedJid;
			}

		}
	}

}