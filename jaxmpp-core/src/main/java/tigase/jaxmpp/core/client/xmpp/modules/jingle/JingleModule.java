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
package tigase.jaxmpp.core.client.xmpp.modules.jingle;

import java.util.List;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class JingleModule extends AbstractIQModule {

	public static final String JINGLE_XMLNS = "urn:xmpp:jingle:1";
	public static final String JINGLE_RTP1_XMLNS = "urn:xmpp:jingle:apps:rtp:1";

	public static final Criteria CRIT = ElementCriteria.name("iq").add(ElementCriteria.name("jingle", JINGLE_XMLNS));

	public static final String[] FEATURES = { JINGLE_XMLNS, JINGLE_RTP1_XMLNS };

	public static class JingleSessionEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private final JID sender;
		private final String sid;

		public JingleSessionEvent(EventType type, SessionObject sessionObject, JID sender, String sid) {
			super(type, sessionObject);

			this.sender = sender;
			this.sid = sid;
		}

		public JID getSender() {
			return sender;
		}

		public String getSid() {
			return sid;
		}

	}

	public static class JingleSessionInitiationEvent extends JingleSessionEvent {

		private static final long serialVersionUID = 1L;

		private final Element description;
		private final List<Element> transports;

		public JingleSessionInitiationEvent(SessionObject sessionObject, JID sender, String sid, Element description,
				List<Element> transports) {
			super(JingleSessionInitiation, sessionObject, sender, sid);

			this.description = description;
			this.transports = transports;
		}

		public Element getDescription() {
			return description;
		}

		public List<Element> getTransports() {
			return transports;
		}

	}

	public static class JingleSessionAcceptEvent extends JingleSessionEvent {

		private static final long serialVersionUID = 1L;

		private final Element description;
		private final Element transport;

		public JingleSessionAcceptEvent(SessionObject sessionObject, JID sender, String sid, Element description,
				Element transport) {
			super(JingleSessionAccept, sessionObject, sender, sid);

			this.description = description;
			this.transport = transport;
		}

		public Element getDescription() {
			return description;
		}

		public Element getTransport() {
			return transport;
		}

	}

	public static class JingleSessionTerminateEvent extends JingleSessionEvent {

		private static final long serialVersionUID = 1L;

		public JingleSessionTerminateEvent(SessionObject sessionObject, JID sender, String sid) {
			super(JingleSessionTerminate, sessionObject, sender, sid);
		}

	}

	public static class JingleSessionInfoEvent extends JingleSessionEvent {

		private static final long serialVersionUID = 1L;

		private final Element info;

		public JingleSessionInfoEvent(SessionObject sessionObject, JID sender, String sid, Element info) {
			super(JingleSessionInfo, sessionObject, sender, sid);

			this.info = info;
		}

		public Element getInfo() {
			return info;
		}

	}

	public static final EventType JingleSessionInitiation = new EventType();
	public static final EventType JingleSessionAccept = new EventType();
	public static final EventType JingleSessionTerminate = new EventType();
	public static final EventType JingleSessionInfo = new EventType();

	private final Observable observable;

	public JingleModule(Observable parentObservable, SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);

		observable = ObservableFactory.instance(parentObservable);
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
	protected void processGet(IQ iq) throws JaxmppException {

	}

	@Override
	protected void processSet(IQ iq) throws JaxmppException {
		Element jingle = iq.getChildrenNS("jingle", JINGLE_XMLNS);

		List<Element> contents = jingle.getChildren("content");
		if (contents == null || contents.isEmpty()) {
			// no point in parsing this any more
			return;
		}

		JID from = iq.getFrom();
		String sid = jingle.getAttribute("sid");

		String action = jingle.getAttribute("action");
		if ("session-terminate".equals(action)) {
			observable.fireEvent(JingleSessionTerminate, new JingleSessionTerminateEvent(sessionObject, from, sid));
		} else if ("session-info".equals(action)) {
			List<Element> infos = jingle.getChildrenNS("urn:xmpp:jingle:apps:rtp:info:1");
			Element info = (infos != null && !infos.isEmpty()) ? infos.get(0) : null;
			observable.fireEvent(JingleSessionInfo, new JingleSessionInfoEvent(sessionObject, from, sid, info));
		} else {
			Element content = contents.get(0);
			List<Element> descriptions = content.getChildren("description");

			Element description = descriptions.get(0);
			List<Element> transports = content.getChildren("transport");

			if ("session-initiate".equals(action)) {
				observable.fireEvent(JingleSessionInitiation, new JingleSessionInitiationEvent(sessionObject, from, sid,
						description, transports));
			} else if ("session-accept".equals(action)) {
				observable.fireEvent(JingleSessionAccept, new JingleSessionAcceptEvent(sessionObject, from, sid, description,
						transports.get(0)));
			}
		}

		// sending result - here should be always ok
		IQ response = IQ.create();
		response.setType(StanzaType.result);
		response.setTo(iq.getFrom());

		writer.write(response);
	}

	public void initiateSession(JID jid, String sid, String name, Element description, List<Element> transports)
			throws JaxmppException {
		IQ iq = IQ.create();

		iq.setTo(jid);
		iq.setType(StanzaType.set);

		Element jingle = new DefaultElement("jingle");
		jingle.setXMLNS(JINGLE_XMLNS);
		jingle.setAttribute("action", "session-initiate");
		jingle.setAttribute("sid", sid);

		JID initiator = sessionObject.getProperty(ResourceBinderModule.BINDED_RESOURCE_JID);
		jingle.setAttribute("initiator", initiator.toString());

		iq.addChild(jingle);

		Element content = new DefaultElement("content");
		content.setAttribute("creator", "initiator");
		content.setAttribute("name", name);

		jingle.addChild(content);

		content.addChild(description);
		for (Element transport : transports) {
			content.addChild(transport);
		}

		writer.write(iq);
	}

	public void acceptSession(JID jid, String sid, String name, Element description, Element transport) throws JaxmppException {
		IQ iq = IQ.create();

		iq.setTo(jid);
		iq.setType(StanzaType.set);

		Element jingle = new DefaultElement("jingle");
		jingle.setXMLNS(JINGLE_XMLNS);
		jingle.setAttribute("action", "session-accept");
		jingle.setAttribute("sid", sid);

		jingle.setAttribute("initiator", jid.toString());

		JID initiator = sessionObject.getProperty(ResourceBinderModule.BINDED_RESOURCE_JID);
		jingle.setAttribute("responder", initiator.toString());

		iq.addChild(jingle);

		Element content = new DefaultElement("content");
		content.setAttribute("creator", "initiator");
		content.setAttribute("name", name);

		jingle.addChild(content);

		content.addChild(description);
		content.addChild(transport);

		writer.write(iq);
	}

	public void terminateSession(JID jid, String sid, JID initiator) throws JaxmppException {
		IQ iq = IQ.create();

		iq.setTo(jid);
		iq.setType(StanzaType.set);

		Element jingle = new DefaultElement("jingle");
		jingle.setXMLNS(JINGLE_XMLNS);
		jingle.setAttribute("action", "session-terminate");
		jingle.setAttribute("sid", sid);

		jingle.setAttribute("initiator", initiator.toString());

		iq.addChild(jingle);

		Element reason = new DefaultElement("result");
		jingle.addChild(reason);

		Element success = new DefaultElement("success");
		reason.addChild(success);

		writer.write(iq);
	}
}