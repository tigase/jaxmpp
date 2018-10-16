/*
 * EntityTimeModule.java
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

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xml.XmlTools;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Implementation of
 * <a href='http://xmpp.org/extensions/xep-0202.html'>XEP-0202: Entity Time</a>.
 */
public class EntityTimeModule
		extends AbstractIQModule {

	private static final DateTimeFormat format = new DateTimeFormat();
	private final Criteria CRIT = ElementCriteria.name("iq").add(ElementCriteria.name("time", "urn:xmpp:time"));
	private final String[] FEATURES = new String[]{"urn:xmpp:time"};

	private static String getFirst(List<Element> list) throws XMLException {
		if (list == null || list.size() == 0) {
			return null;
		}
		Element x = list.get(0);
		return x == null ? null : x.getValue();
	}

	public EntityTimeModule() {
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	/**
	 * Request for XMPP entity time.
	 *
	 * @param jid entity to request.
	 * @param asyncCallback general callback
	 */
	public void getEntityTime(JID jid, AsyncCallback asyncCallback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.get);
		iq.setTo(jid);
		iq.addChild(ElementFactory.create("time", null, "urn:xmpp:time"));

		write(iq, asyncCallback);
	}

	/**
	 * Request for XMPP entity time.
	 *
	 * @param jid entity to request.
	 * @param asyncCallback entity time callback
	 */
	public void getEntityTime(JID jid, EntityTimeAsyncCallback asyncCallback) throws JaxmppException {
		getEntityTime(jid, (AsyncCallback) asyncCallback);
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
	}

	@Override
	protected void processGet(IQ stanza) throws JaxmppException {
		Element result = XmlTools.makeResult(stanza);
		Element time = ElementFactory.create("time", null, "urn:xmpp:time");
		result.addChild(time);

		int offset = TimeZone.getDefault().getRawOffset();
		String tz = String.format("%s%02d:%02d", offset >= 0 ? "+" : "-", offset / 3600000, (offset / 60000) % 60);
		String tm = format.format(new Date());

		time.addChild(ElementFactory.create("tzo", tz, null));
		time.addChild(ElementFactory.create("utc", tm, null));

		write(result);
	}

	@Override
	protected void processSet(IQ stanza) throws XMPPException, XMLException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	/**
	 * Ping callback.
	 */
	public static abstract class EntityTimeAsyncCallback
			implements AsyncCallback {

		private Date time;

		/**
		 * Called on success.
		 *
		 * @param time entity time
		 */
		protected abstract void onEntityTimeReceived(String tzo, Date time);

		@Override
		public void onSuccess(Stanza responseStanza) throws XMLException {
			Element time = responseStanza.getChildrenNS("time", "urn:xmpp:time");
			if (time != null) {
				String tzo = getFirst(time.getChildren("tzo"));
				String utc = getFirst(time.getChildren("utc"));
				onEntityTimeReceived(tzo, format.parse(utc));
			}
		}
	}

}