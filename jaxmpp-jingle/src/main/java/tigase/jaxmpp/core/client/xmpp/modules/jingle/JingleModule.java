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

import tigase.jaxmpp.core.client.xmpp.utils.MutableBoolean;
import java.util.ArrayList;
import java.util.List;

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.JingleModule.JingleSessionAcceptHandler.JingleSessionAcceptEvent;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.JingleModule.JingleSessionInfoHandler.JingleSessionInfoEvent;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.JingleModule.JingleSessionInitiationHandler.JingleSessionInitiationEvent;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.JingleModule.JingleSessionTerminateHandler.JingleSessionTerminateEvent;
import tigase.jaxmpp.core.client.xmpp.modules.jingle.JingleModule.JingleTransportInfoHandler.JingleTransportInfoEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class JingleModule implements XmppModule {

	public interface JingleSessionAcceptHandler extends EventHandler {

		public static class JingleSessionAcceptEvent extends JaxmppEvent<JingleSessionAcceptHandler> {

			private Element description;

			private MutableBoolean handled;

			private JID sender;

			private String sid;

			private List<Transport> transports;

			public JingleSessionAcceptEvent(SessionObject sessionObject, JID sender, String sid, Element description,
					List<Transport> transports, MutableBoolean handled) {
				super(sessionObject);
				this.sender = sender;
				this.sid = sid;
				this.description = description;
				this.transports = transports;
				this.handled = handled;
			}

			@Override
			protected void dispatch(JingleSessionAcceptHandler handler) {
				handler.onJingleSessionAccept(sessionObject, sender, sid, description, transports, handled);
			}

			public Element getDescription() {
				return description;
			}

			public MutableBoolean getHandled() {
				return handled;
			}

			public JID getSender() {
				return sender;
			}

			public String getSid() {
				return sid;
			}

			public List<Transport> getTransports() {
				return transports;
			}

			public void setDescription(Element description) {
				this.description = description;
			}

			public void setHandled(MutableBoolean handled) {
				this.handled = handled;
			}

			public void setSender(JID sender) {
				this.sender = sender;
			}

			public void setSid(String sid) {
				this.sid = sid;
			}

			public void setTransports(List<Transport> transports) {
				this.transports = transports;
			}

		}

		void onJingleSessionAccept(SessionObject sessionObject, JID sender, String sid, Element description,
				List<Transport> transports, MutableBoolean handled);
	}

	public interface JingleSessionInfoHandler extends EventHandler {

		public static class JingleSessionInfoEvent extends JaxmppEvent<JingleSessionInfoHandler> {

			private List<Element> content;

			private MutableBoolean handled;

			private JID sender;

			private String sid;

			public JingleSessionInfoEvent(SessionObject sessionObject, JID sender, String sid, List<Element> content,
					MutableBoolean handled) {
				super(sessionObject);
				this.sender = sender;
				this.sid = sid;
				this.content = content;
				this.handled = handled;
			}

			@Override
			protected void dispatch(JingleSessionInfoHandler handler) {
				handler.onJingleSessionInfo(sessionObject, sender, sid, content, handled);
			}

			public List<Element> getContent() {
				return content;
			}

			public MutableBoolean getHandled() {
				return handled;
			}

			public JID getSender() {
				return sender;
			}

			public String getSid() {
				return sid;
			}

			public void setContent(List<Element> content) {
				this.content = content;
			}

			public void setHandled(MutableBoolean handled) {
				this.handled = handled;
			}

			public void setSender(JID sender) {
				this.sender = sender;
			}

			public void setSid(String sid) {
				this.sid = sid;
			}

		}

		void onJingleSessionInfo(SessionObject sessionObject, JID sender, String sid, List<Element> content,
				MutableBoolean handled);
	}

	public interface JingleSessionInitiationHandler extends EventHandler {

		public static class JingleSessionInitiationEvent extends JaxmppEvent<JingleSessionInitiationHandler> {

			private Element description;

			private MutableBoolean handled;

			private JID sender;

			private String sid;

			private List<Transport> transports;

			public JingleSessionInitiationEvent(SessionObject sessionObject, JID sender, String sid, Element description,
					List<Transport> transports, MutableBoolean handled) {
				super(sessionObject);
				this.sender = sender;
				this.sid = sid;
				this.description = description;
				this.transports = transports;
				this.handled = handled;
			}

			@Override
			protected void dispatch(JingleSessionInitiationHandler handler) {
				handler.onJingleSessionInitiation(sessionObject, sender, sid, description, transports, handled);
			}

			public Element getDescription() {
				return description;
			}

			public MutableBoolean getHandled() {
				return handled;
			}

			public JID getSender() {
				return sender;
			}

			public String getSid() {
				return sid;
			}

			public List<Transport> getTransports() {
				return transports;
			}

			public void setDescription(Element description) {
				this.description = description;
			}

			public void setHandled(MutableBoolean handled) {
				this.handled = handled;
			}

			public void setSender(JID sender) {
				this.sender = sender;
			}

			public void setSid(String sid) {
				this.sid = sid;
			}

			public void setTransports(List<Transport> transports) {
				this.transports = transports;
			}

		}

		void onJingleSessionInitiation(SessionObject sessionObject, JID sender, String sid, Element description,
				List<Transport> transports, MutableBoolean handled);
	}

	public interface JingleSessionTerminateHandler extends EventHandler {

		public static class JingleSessionTerminateEvent extends JaxmppEvent<JingleSessionTerminateHandler> {

			private MutableBoolean handled;

			private JID sender;

			private String sid;

			public JingleSessionTerminateEvent(SessionObject sessionObject, JID sender, String sid, MutableBoolean handled) {
				super(sessionObject);
				this.sender = sender;
				this.sid = sid;
				this.handled = handled;
			}

			@Override
			protected void dispatch(JingleSessionTerminateHandler handler) {
				handler.onJingleSessionTerminate(sessionObject, sender, sid, handled);
			}

			public MutableBoolean getHandled() {
				return handled;
			}

			public JID getSender() {
				return sender;
			}

			public String getSid() {
				return sid;
			}

			public void setHandled(MutableBoolean handled) {
				this.handled = handled;
			}

			public void setSender(JID sender) {
				this.sender = sender;
			}

			public void setSid(String sid) {
				this.sid = sid;
			}

		}

		void onJingleSessionTerminate(SessionObject sessionObject, JID sender, String sid, MutableBoolean handled);
	}

	public interface JingleTransportInfoHandler extends EventHandler {

		public static class JingleTransportInfoEvent extends JaxmppEvent<JingleTransportInfoHandler> {

			private Element content;

			private MutableBoolean handled;

			private JID sender;

			private String sid;

			public JingleTransportInfoEvent(SessionObject sessionObject, JID sender, String sid, Element content,
					MutableBoolean handled) {
				super(sessionObject);
				this.sender = sender;
				this.sid = sid;
				this.content = content;
				this.handled = handled;
			}

			@Override
			protected void dispatch(JingleTransportInfoHandler handler) throws JaxmppException {
				handler.onJingleTransportInfo(sessionObject, sender, sid, content, handled);
			}

			public Element getContent() {
				return content;
			}

			public MutableBoolean getHandled() {
				return handled;
			}

			public JID getSender() {
				return sender;
			}

			public String getSid() {
				return sid;
			}

			public void setContent(Element content) {
				this.content = content;
			}

			public void setHandled(MutableBoolean handled) {
				this.handled = handled;
			}

			public void setSender(JID sender) {
				this.sender = sender;
			}

			public void setSid(String sid) {
				this.sid = sid;
			}

		}

		void onJingleTransportInfo(SessionObject sessionObject, JID sender, String sid, Element content, MutableBoolean handled)
				throws JaxmppException;
	}

	public static final String JINGLE_RTP1_XMLNS = "urn:xmpp:jingle:apps:rtp:1";

	public static final String JINGLE_XMLNS = "urn:xmpp:jingle:1";

	public static final Criteria CRIT = ElementCriteria.name("iq").add(ElementCriteria.name("jingle", JINGLE_XMLNS));

	public static final String[] FEATURES = { JINGLE_XMLNS, JINGLE_RTP1_XMLNS };

	private Context context;

	public JingleModule(Context context) {
		this.context = context;
	}

	public void acceptSession(JID jid, String sid, String name, Element description, List<Transport> transports)
			throws JaxmppException {
		IQ iq = IQ.create();

		iq.setTo(jid);
		iq.setType(StanzaType.set);

		Element jingle = ElementFactory.create("jingle");
		jingle.setXMLNS(JINGLE_XMLNS);
		jingle.setAttribute("action", "session-accept");
		jingle.setAttribute("sid", sid);

		jingle.setAttribute("initiator", jid.toString());

		JID initiator = context.getSessionObject().getProperty(ResourceBinderModule.BINDED_RESOURCE_JID);
		jingle.setAttribute("responder", initiator.toString());

		iq.addChild(jingle);

		Element content = ElementFactory.create("content");
		content.setXMLNS(JINGLE_XMLNS);
		content.setAttribute("creator", "initiator");
		content.setAttribute("name", name);

		jingle.addChild(content);

		content.addChild(description);
		if (transports != null) {
			for (Element transport : transports) {
				content.addChild(transport);
			}
		}

		context.getWriter().write(iq);
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
	}

	public void initiateSession(JID jid, String sid, String name, Element description, List<Transport> transports)
			throws JaxmppException {
		IQ iq = IQ.create();

		iq.setTo(jid);
		iq.setType(StanzaType.set);

		Element jingle = ElementFactory.create("jingle");
		jingle.setXMLNS(JINGLE_XMLNS);
		jingle.setAttribute("action", "session-initiate");
		jingle.setAttribute("sid", sid);

		JID initiator = context.getSessionObject().getProperty(ResourceBinderModule.BINDED_RESOURCE_JID);
		jingle.setAttribute("initiator", initiator.toString());

		iq.addChild(jingle);

		Element content = ElementFactory.create("content");
		content.setXMLNS(JINGLE_XMLNS);
		content.setAttribute("creator", "initiator");
		content.setAttribute("name", name);

		jingle.addChild(content);

		content.addChild(description);
		for (Element transport : transports) {
			content.addChild(transport);
		}

		context.getWriter().write(iq);
	}

	@Override
	public void process(Element element) throws XMPPException, XMLException, JaxmppException {
		if ("iq".equals(element.getName())) {
			IQ iq = (IQ) Stanza.create(element);
			processIq(iq);
		}
	}

	protected void processIq(IQ iq) throws JaxmppException {
		Element jingle = iq.getChildrenNS("jingle", JINGLE_XMLNS);

		List<Element> contents = jingle.getChildren("content");
		// if (contents == null || contents.isEmpty()) {
		// // no point in parsing this any more
		// return;
		// }

		JID from = iq.getFrom();
		String sid = jingle.getAttribute("sid");

		String action = jingle.getAttribute("action");

		final MutableBoolean handled = new MutableBoolean();
		if ("session-terminate".equals(action)) {
			JingleSessionTerminateEvent event = new JingleSessionTerminateEvent(context.getSessionObject(), from, sid, handled);
			context.getEventBus().fire(event);
		} else if ("session-info".equals(action)) {
			JingleSessionInfoEvent event = new JingleSessionInfoEvent(context.getSessionObject(), from, sid,
					jingle.getChildren(), handled);
			context.getEventBus().fire(event);
		} else if ("transport-info".equals(action)) {
			JingleTransportInfoEvent event = new JingleTransportInfoEvent(context.getSessionObject(), from, sid,
					contents.get(0), handled);
			context.getEventBus().fire(event);
		} else {
			Element content = contents.get(0);
			List<Element> descriptions = content.getChildren("description");

			Element description = descriptions.get(0);
			List<Element> transportElems = content.getChildren("transport");
			List<Transport> transports = new ArrayList<Transport>();
			for (Element transElem : transportElems) {
				if ("transport".equals(transElem.getName())) {
					transports.add(new Transport(transElem));
				}
			}

			if ("session-initiate".equals(action)) {
				JingleSessionInitiationEvent event = new JingleSessionInitiationEvent(context.getSessionObject(), from, sid,
						description, transports, handled);
				context.getEventBus().fire(event);
			} else if ("session-accept".equals(action)) {
				JingleSessionAcceptEvent event = new JingleSessionAcceptEvent(context.getSessionObject(), from, sid,
						description, transports, handled);
				context.getEventBus().fire(event);
			}
		}

		if (handled.isValue()) {
			// sending result - here should be always ok
			IQ response = IQ.create();
			response.setTo(iq.getFrom());
			response.setId(iq.getId());
			response.setType(StanzaType.result);
			context.getWriter().write(response);
		} else {
			throw new XMPPException(XMPPException.ErrorCondition.feature_not_implemented);
		}
	}

	public void terminateSession(JID jid, String sid, JID initiator) throws JaxmppException {
		IQ iq = IQ.create();

		iq.setTo(jid);
		iq.setType(StanzaType.set);

		Element jingle = ElementFactory.create("jingle");
		jingle.setXMLNS(JINGLE_XMLNS);
		jingle.setAttribute("action", "session-terminate");
		jingle.setAttribute("sid", sid);

		jingle.setAttribute("initiator", initiator.toString());

		iq.addChild(jingle);

		Element reason = ElementFactory.create("result");
		jingle.addChild(reason);

		Element success = ElementFactory.create("success");
		reason.addChild(success);

		context.getWriter().write(iq);
	}

	public void transportInfo(JID recipient, JID initiator, String sid, Element content) throws JaxmppException {
		IQ iq = IQ.create();

		iq.setTo(recipient);
		iq.setType(StanzaType.set);

		Element jingle = ElementFactory.create("jingle");
		jingle.setXMLNS(JINGLE_XMLNS);
		jingle.setAttribute("action", "transport-info");
		jingle.setAttribute("sid", sid);
		jingle.setAttribute("initiator", initiator.toString());

		iq.addChild(jingle);

		jingle.addChild(content);

		context.getWriter().write(iq);
	}

}