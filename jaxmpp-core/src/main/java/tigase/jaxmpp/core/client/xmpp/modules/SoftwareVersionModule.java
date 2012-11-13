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

import java.util.List;

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
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xml.XmlTools;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 * Implementation of <a
 * href='http://xmpp.org/extensions/xep-0092.html'>XEP-0092: Software
 * Version</a>.
 */
public class SoftwareVersionModule extends AbstractIQModule {

	/**
	 * Software version callback.
	 */
	public static abstract class SoftwareVersionAsyncCallback implements AsyncCallback {

		@Override
		public void onSuccess(final Stanza responseStanza) throws XMLException {
			Element query = responseStanza.getChildrenNS("query", "jabber:iq:version");
			if (query != null) {
				String eName = getFirst(query.getChildren("name"));
				String eVersion = getFirst(query.getChildren("version"));
				String eOs = getFirst(query.getChildren("os"));
				onVersionReceived(eName, eVersion, eOs);
			}
		}

		/**
		 * Called on success.
		 * 
		 * @param name
		 *            received software name
		 * @param version
		 *            received software version
		 * @param os
		 *            received operating system name
		 */
		protected abstract void onVersionReceived(final String name, final String version, final String os) throws XMLException;
	}

	/**
	 * Default software name.
	 */
	public final static String DEFAULT_NAME_VAL = "Tigase based software";

	/**
	 * Key to keep software name in {@link SessionObject}.
	 */
	public final static String NAME_KEY = "SOFTWARE_VERSION#NAME_KEY";

	/**
	 * Key to keep operating system name in {@link SessionObject}.
	 */
	public final static String OS_KEY = "SOFTWARE_VERSION#OS_KEY";

	/**
	 * Key to keep software version in {@link SessionObject}.
	 */
	public final static String VERSION_KEY = "SOFTWARE_VERSION#VERSION_KEY";

	private static String getFirst(List<Element> list) throws XMLException {
		if (list == null || list.size() == 0)
			return null;
		Element x = list.get(0);
		return x == null ? null : x.getValue();
	}

	private final Criteria CRIT = ElementCriteria.name("iq").add(ElementCriteria.name("query", "jabber:iq:version"));

	private final String[] FEATURES = new String[] { "jabber:iq:version" };

	public SoftwareVersionModule(Observable parentObservable, SessionObject sessionObject, PacketWriter packetWriter) {
		super(ObservableFactory.instance(parentObservable), sessionObject, packetWriter);
	}

	/**
	 * Requests software version for given entity.
	 * 
	 * @param jid
	 *            entity
	 * @param callback
	 *            general callback
	 */
	public void checkSoftwareVersion(JID jid, AsyncCallback callback) throws JaxmppException {
		IQ pingIq = IQ.create();
		pingIq.setTo(jid);
		pingIq.setType(StanzaType.get);
		pingIq.addChild(new DefaultElement("ping", null, "jabber:iq:version"));

		writer.write(pingIq, callback);
	}

	/**
	 * Requests software version for given entity.
	 * 
	 * @param jid
	 *            entity
	 * @param callback
	 *            software version callback
	 */
	public void checkSoftwareVersion(JID jid, SoftwareVersionAsyncCallback callback) throws XMLException, JaxmppException {
		checkSoftwareVersion(jid, (AsyncCallback) callback);
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
	}

	@Override
	protected void processGet(IQ element) throws XMPPException, XMLException, JaxmppException {
		Element result = XmlTools.makeResult(element);
		Element query = new DefaultElement("query", null, "jabber:iq:version");
		result.addChild(query);

		String name = sessionObject.getProperty(NAME_KEY);
		String version = sessionObject.getProperty(VERSION_KEY);
		String os = sessionObject.getProperty(OS_KEY);

		query.addChild(new DefaultElement("name", name == null ? DEFAULT_NAME_VAL : name, null));
		query.addChild(new DefaultElement("version", version == null ? "0.0.0" : version, null));
		if (os != null)
			query.addChild(new DefaultElement("os", os, null));

		writer.write(result);
	}

	@Override
	protected void processSet(IQ element) throws XMPPException, XMLException, JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

}