/*
 * PushNotificationModule.java
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
package tigase.jaxmpp.core.client.xmpp.modules.push;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.UIDGenerator;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.modules.InitializingModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import java.util.HashSet;

public class PushNotificationModule
		extends AbstractStanzaModule<IQ>
		implements InitializingModule {

	private static final String XMLNS = "urn:xmpp:push:0";
	private static final String[] FEATURES = {XMLNS};

	/**
	 * Disables push notifications.
	 *
	 * @param pushComponentJid JID of XMPP Push Service.
	 * @param node provisioned node specified by the App Server.
	 * @param callback callback.
	 */
	public void disable(JID pushComponentJid, String node, AsyncCallback callback) throws JaxmppException {
		IQ iq = IQ.createIQ();
		iq.setType(StanzaType.set);
		iq.setId(UIDGenerator.next());

		Element disable = ElementFactory.create("disable");
		disable.setXMLNS(XMLNS);
		disable.setAttribute("jid", pushComponentJid.toString());
		if (node != null) {
			disable.setAttribute("node", node);
		}
		iq.addChild(disable);

		write(iq, callback);
	}

	/**
	 * Enables push notifications.
	 *
	 * @param pushComponentJid JID of XMPP Push Service.
	 * @param node provisioned node specified by the App Server.
	 * @param callback callback.
	 */
	public void enable(JID pushComponentJid, String node, AsyncCallback callback) throws JaxmppException {
		IQ iq = IQ.createIQ();
		iq.setType(StanzaType.set);
		iq.setId(UIDGenerator.next());

		Element enable = ElementFactory.create("enable");
		enable.setXMLNS(XMLNS);
		enable.setAttribute("jid", pushComponentJid.toString());
		if (node != null) {
			enable.setAttribute("node", node);
		}
		iq.addChild(enable);

		write(iq, callback);
	}

	@Override
	public Criteria getCriteria() {
		return null;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	public boolean isSupportedByServer() {
		HashSet<String> serverFeatures = context.getSessionObject().getProperty(DiscoveryModule.SERVER_FEATURES_KEY);
		return serverFeatures != null && serverFeatures.contains(XMLNS);
	}

	@Override
	public void process(IQ stanza) throws JaxmppException {
		throw new XMPPException(XMPPException.ErrorCondition.feature_not_implemented);
	}
}
