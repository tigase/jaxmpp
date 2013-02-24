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
package tigase.jaxmpp.core.client.xmpp.modules.adhoc;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UIDGenerator;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xml.XmlTools;
import tigase.jaxmpp.core.client.xmpp.forms.JabberDataElement;
import tigase.jaxmpp.core.client.xmpp.forms.XDataType;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule.DiscoInfoEvent;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule.Identity;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoItemsModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoItemsModule.DiscoItemEvent;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoItemsModule.Item;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 * Module to handle ad-hoc commands.
 * 
 * @author bmalkow
 */

public class AdHocCommansModule extends AbstractIQModule {

	/**
	 * Callback to handle result of ad-hoc command.
	 * 
	 */
	public static abstract class AdHocCommansAsyncCallback implements AsyncCallback {

		private Element command;

		private IQ response;

		/**
		 * Return &lt;command xmlns='http://jabber.org/protocol/commands' /&gt;
		 * element of returned stanza.
		 * 
		 * @return return command element.
		 */
		protected Element getCommand() {
			return command;
		}

		/**
		 * Reutrns IQ stanza with response.
		 * 
		 * @return IQ stanza
		 */
		protected IQ getResponse() {
			return response;
		}

		/**
		 * Method called when response of ad-hoc command is received.
		 * 
		 * @param sessionid
		 *            ID of session. May be <code>null</code>.
		 * @param node
		 *            node
		 * @param status
		 *            status of command execution
		 * @param data
		 *            Data Form
		 */
		protected abstract void onResponseReceived(String sessionid, String node, State status, JabberDataElement data)
				throws JaxmppException;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onSuccess(final Stanza responseStanza) throws JaxmppException {
			this.response = (IQ) responseStanza;
			this.command = responseStanza.getChildrenNS("command", "http://jabber.org/protocol/commands");

			if (this.command != null) {
				String sessionid = this.command.getAttribute("sessionid");
				String node = this.command.getAttribute("node");
				State status = this.command.getAttribute("status") == null ? null
						: State.valueOf(this.command.getAttribute("status"));

				JabberDataElement data = new JabberDataElement(this.command.getChildrenNS("x", "jabber:x:data"));

				onResponseReceived(sessionid, node, status, data);
			}

		}
	}

	public static final Criteria CRIT = ElementCriteria.name("iq").add(
			ElementCriteria.name("command", new String[] { "xmlns" }, new String[] { "http://jabber.org/protocol/commands" }));

	static String generateSessionId() {
		return UIDGenerator.next() + UIDGenerator.next();
	}

	private final Map<String, AdHocCommand> commands = new HashMap<String, AdHocCommand>();

	private final String[] FEATURES = { "http://jabber.org/protocol/commands" };

	private final Map<String, Session> sessions = new HashMap<String, Session>();

	public AdHocCommansModule(SessionObject sessionObject, PacketWriter packetWriter, DiscoItemsModule discoItemsModule,
			DiscoInfoModule discoInfoModule) {
		super(sessionObject, packetWriter);
		discoItemsModule.addListener(DiscoItemsModule.ItemsRequested, new Listener<DiscoItemsModule.DiscoItemEvent>() {

			@Override
			public void handleEvent(DiscoItemEvent be) {
				try {
					if (be.getNode() != null && be.getNode().equals("http://jabber.org/protocol/commands"))
						processDiscoItemEvent(be);
				} catch (XMLException e) {
				}
			}
		});
		discoInfoModule.addListener(DiscoInfoModule.InfoRequested, new Listener<DiscoInfoModule.DiscoInfoEvent>() {

			@Override
			public void handleEvent(DiscoInfoEvent be) throws JaxmppException {
				try {
					if (be.getNode() != null && be.getNode().equals("http://jabber.org/protocol/commands")) {
						be.setIdentity(new Identity());
						be.getIdentity().setCategory("automation");
						be.getIdentity().setName("Ad-Hoc Commands");
						be.getIdentity().setType("command-list");
					} else if (be.getNode() != null && commands.containsKey(be.getNode())) {
						processDiscoInfoEvent(be);
					}
				} catch (XMLException e) {
				}
			}
		});
	}

	/**
	 * Calls ad-hoc command on remote resource.
	 * 
	 * @param toJID
	 *            remote ad-hoc command executor.
	 * @param node
	 *            node
	 * @param action
	 *            action
	 * @param data
	 *            Data Form
	 * @param asyncCallback
	 *            callback
	 */
	public void execute(JID toJID, String node, Action action, JabberDataElement data, AsyncCallback asyncCallback)
			throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		iq.setTo(toJID);

		Element command = new DefaultElement("command", null, "http://jabber.org/protocol/commands");
		command.setAttribute("node", node);
		if (action != null)
			command.setAttribute("action", action.name());

		if (data != null) {
			command.addChild(data.createSubmitableElement(XDataType.submit));
		}

		iq.addChild(command);

		writer.write(iq, asyncCallback);
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
	}

	protected void processDiscoInfoEvent(final DiscoInfoEvent be) throws XMLException, XMPPException {
		final AdHocCommand command = this.commands.get(be.getNode());

		if (!command.isAllowed(be.getRequestStanza().getFrom()))
			throw new XMPPException(ErrorCondition.forbidden);

		be.setIdentity(new Identity());
		be.getIdentity().setCategory("automation");
		be.getIdentity().setName(command.getName());
		be.getIdentity().setType("command-node");

		be.setFeatures(command.getFeatures());
	}

	protected void processDiscoItemEvent(final DiscoItemEvent be) throws XMLException {
		JID jid = be.getRequestStanza().getTo();
		for (AdHocCommand command : this.commands.values()) {
			if (!command.isAllowed(be.getRequestStanza().getFrom()))
				continue;
			Item it = new Item();
			it.setJid(jid);
			it.setName(command.getName());
			it.setNode(command.getNode());
			be.getItems().add(it);
		}
	}

	@Override
	protected void processGet(IQ element) throws XMPPException, XMLException, JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	@Override
	protected void processSet(IQ element) throws XMPPException, XMLException, JaxmppException {
		Element command = element.getChildrenNS("command", "http://jabber.org/protocol/commands");
		final String requestedNode = command.getAttribute("node");
		final String sessionid = command.getAttribute("sessionid");
		final AdHocCommand commandHandler = this.commands.get(requestedNode);
		if (commandHandler == null)
			throw new XMPPException(ErrorCondition.item_not_found, "Ad-Hoc Command not found");

		if (!commandHandler.isAllowed(element.getFrom()))
			throw new XMPPException(ErrorCondition.forbidden);

		final Action action;
		{
			final String t = command.getAttribute("action");
			final Session s = this.sessions.get(sessionid);
			if (s != null) {
				s.setLastRequest(new Date());
			}
			if (t == null && s == null) {
				action = Action.execute;
			} else if (t == null && s != null) {
				action = s.getDefaultAction();
			} else {
				action = Action.valueOf(t);
			}

		}

		final AdHocRequest request = new AdHocRequest(action, requestedNode, sessionid, element, this.sessions);
		final AdHocResponse response = new AdHocResponse(writer);

		Element xData = command.getChildrenNS("x", "jabber:x:data");
		if (xData != null) {
			request.setForm(new JabberDataElement(xData));
		}

		commandHandler.handle(request, response);

		final State state = response.getState();

		Session session = request.getSession(false);

		switch (state) {
		case canceled:
		case completed:
			if (session != null) {
				if (log.isLoggable(Level.FINE))
					log.fine("Session " + session.getSessionId() + " is removed");
				this.sessions.remove(session.getSessionId());
			}
			break;
		case executing:
			if (session != null)
				session.setDefaultAction(response.getDefaultAction());
			break;
		}

		final Element result = XmlTools.makeResult(element);
		final Element commandResult = new DefaultElement("command", null, "http://jabber.org/protocol/commands");
		commandResult.setAttribute("node", requestedNode);
		commandResult.setAttribute("sessionid", request.getSessionId());
		commandResult.setAttribute("status", state.name());
		result.addChild(commandResult);

		if (!response.getAvailableActions().isEmpty()) {
			Element actions = new DefaultElement("actions");
			commandResult.addChild(actions);
			if (response.getDefaultAction() != null) {
				actions.setAttribute("execute", response.getDefaultAction().name());
			}
			for (Action a : response.getAvailableActions()) {
				actions.addChild(new DefaultElement(a.name()));
			}
		}

		if (response.getForm() != null) {
			commandResult.addChild(response.getForm());
		}

		writer.write(result);
	}

	/**
	 * Registers new ad-hoc command.
	 * 
	 * @param command
	 *            command to register
	 */
	public void register(final AdHocCommand command) {
		final String node = command.getNode();
		this.commands.put(node, command);
	}
}