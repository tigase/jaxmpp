/*
 * FlexibleOfflineMessageRetrieval.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.core.client.xmpp.modules.workgroup;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static tigase.jaxmpp.core.client.xmpp.modules.workgroup.WorkgroupAgentModule.OfferReceivedHandler.OfferReceivedEvent;

/**
 * WorkGroup module *STUB* - only handles offers from WG component
 */
public class WorkgroupAgentModule
		extends AbstractIQModule {

	public static final String WORKGROUP_XMLNS = "http://jabber.org/protocol/workgroup";
	private final Criteria CRIT = ElementCriteria.empty().add(ElementCriteria.xmlns(WORKGROUP_XMLNS));
	private final Map<JID, JID> offers = new ConcurrentHashMap<>();

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	@Override
	protected void processGet(IQ element) throws JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	@Override
	protected void processSet(IQ element) throws JaxmppException {

		Element offer = element.getChildrenNS("offer", WORKGROUP_XMLNS);
		if (offer != null) {
			JID userJid = JID.jidInstance(offer.getAttribute("jid"));
			Element timeoutElement = offer.getFirstChild("timeout");
			long timeout = Long.MIN_VALUE;
			if (timeoutElement != null) {
				timeout = Long.parseLong(timeoutElement.getValue());
			}
			offers.put(userJid, element.getFrom());
			final OfferReceivedEvent event;
			event = new OfferReceivedEvent(context.getSessionObject(), userJid, element.getFrom(), timeout);
			fireEvent(event);
		}
	}

	public interface OfferReceivedHandler
			extends EventHandler {

		void onOfferReceived(SessionObject sessionObject, JID userJID, JID workgroupJID, long timeout);

		class OfferReceivedEvent
				extends JaxmppEvent<OfferReceivedHandler> {

			long timeout;
			JID userJID;
			JID workgroupJID;

			public OfferReceivedEvent(SessionObject sessionObject, JID userJID, JID workgroupJID, long timeout) {
				super(sessionObject);
				this.userJID = userJID;
				this.workgroupJID = workgroupJID;
				this.timeout = timeout;
			}

			@Override
			public void dispatch(OfferReceivedHandler handler) {
				handler.onOfferReceived(sessionObject, userJID, workgroupJID, timeout);
			}

			public JID getUserJID() {
				return userJID;
			}

			public JID getWorkgroupJID() {
				return workgroupJID;
			}

			public long getTimeout() {
				return timeout;
			}
		}
	}
}
