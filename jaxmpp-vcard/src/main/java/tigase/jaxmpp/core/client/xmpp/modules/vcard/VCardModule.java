/*
 * VCardModule.java
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
package tigase.jaxmpp.core.client.xmpp.modules.vcard;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class VCardModule
		extends AbstractStanzaModule<Stanza> {

	public VCardModule() {
	}

	@Override
	public Criteria getCriteria() {
		return null;
	}

	@Override
	public String[] getFeatures() {
		return new String[]{"vcard-temp"};
	}

	@Override
	public void process(Stanza element) throws JaxmppException {
	}

	public void retrieveVCard(JID jid, AsyncCallback asyncCallback) throws JaxmppException {
		retrieveVCard(jid, null, asyncCallback);
	}

	public void retrieveVCard(JID jid, Long timeout, AsyncCallback asyncCallback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.get);
		iq.setTo(jid);
		iq.addChild(ElementFactory.create("vCard", null, "vcard-temp"));

		write(iq, timeout, asyncCallback);
	}

	public void retrieveVCard(JID jid, VCardAsyncCallback asyncCallback) throws JaxmppException {
		retrieveVCard(jid, (AsyncCallback) asyncCallback);
	}

	public static abstract class VCardAsyncCallback
			implements AsyncCallback {

		@Override
		public void onSuccess(final Stanza responseStanza) throws XMLException {
			Element query = responseStanza.getChildrenNS("vCard", "vcard-temp");
			if (query != null) {
				VCard v = new VCard();
				v.loadData(query);
				onVCardReceived(v);
			}
		}

		protected abstract void onVCardReceived(VCard vcard) throws XMLException;

	}
}