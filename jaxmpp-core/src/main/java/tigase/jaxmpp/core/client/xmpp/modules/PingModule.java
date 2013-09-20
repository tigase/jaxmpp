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

import java.util.Date;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xml.XmlTools;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 * Implementation of <a href='http://xmpp.org/extensions/xep-0199.html'>XEP-0199
 * XMPP Ping</a>.
 */
public class PingModule extends AbstractIQModule {

	/**
	 * Ping callback.
	 */
	public static abstract class PingAsyncCallback implements AsyncCallback {

		private long pingTimestamp;

		/**
		 * Called on success.
		 * 
		 * @param time
		 *            ping time
		 */
		protected abstract void onPong(final long time);

		@Override
		public void onSuccess(Stanza responseStanza) throws XMLException {
			onPong((new Date()).getTime() - pingTimestamp);
		}
	}

	private final Criteria CRIT = ElementCriteria.name("iq").add(ElementCriteria.name("ping", "urn:xmpp:ping"));

	private final String[] FEATURES = new String[] { "urn:xmpp:ping" };

	public PingModule(SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
	}

	/**
	 * Ping given XMPP entity.
	 * 
	 * @param jid
	 *            entity to ping.
	 * @param asyncCallback
	 *            general callback
	 */
	public void ping(JID jid, AsyncCallback asyncCallback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.get);
		iq.setTo(jid);
		iq.addChild(new DefaultElement("ping", null, "urn:xmpp:ping"));

		writer.write(iq, asyncCallback);
	}

	/**
	 * Ping given XMPP entity.
	 * 
	 * @param jid
	 *            entity to ping.
	 * @param asyncCallback
	 *            ping callback
	 */
	public void ping(JID jidInstance, PingAsyncCallback asyncCallback) throws JaxmppException {
		asyncCallback.pingTimestamp = (new Date()).getTime();
		ping(jidInstance, (AsyncCallback) asyncCallback);
	}

	@Override
	protected void processGet(IQ stanza) throws JaxmppException {
		Element response = XmlTools.makeResult(stanza);

		writer.write(response);
	}

	@Override
	protected void processSet(IQ stanza) throws XMPPException, XMLException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

}