/*
 * MessageDeliveryReceiptsExtension.java
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
package tigase.jaxmpp.core.client.xmpp.modules.chat.xep0184;

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UIDGenerator;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModuleExtension;
import tigase.jaxmpp.core.client.xmpp.modules.extensions.Extension;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;

public class MessageDeliveryReceiptsExtension
		implements MessageModuleExtension, Extension {

	public static final String XMLNS = "urn:xmpp:receipts";
	private final static String RECEIVED_NAME = "received";

	private final static String REQUEST_NAME = "request";
	private final Context context;

	public MessageDeliveryReceiptsExtension(Context context) {
		this.context = context;
	}

	@Override
	public Element afterReceive(Element received) throws JaxmppException {
		// TODO Auto-generated method stub
		return received;
	}

	@Override
	public Message beforeMessageProcess(Message message, Chat chat) throws JaxmppException {
		final Element request = message.getChildrenNS(REQUEST_NAME, XMLNS);
		if (request != null) {
			final String id = message.getId();

			Message response = Message.create();
			response.setId(UIDGenerator.next());
			response.setTo(message.getFrom());

			Element received = ElementFactory.create(RECEIVED_NAME, null, XMLNS);
			if (id != null) {
				received.setAttribute("id", id);
			}
			response.addChild(received);

			context.getWriter().write(response);
		}
		final Element received = message.getChildrenNS(RECEIVED_NAME, XMLNS);
		if (received != null) {
			String id = received.getAttribute("id");
			context.getEventBus()
					.fire(new ReceiptReceivedHandler.ReceiptReceivedEvent(context.getSessionObject(), chat, message,
																		  id));
		}
		return message;
	}

	@Override
	public Element beforeSend(Element element) throws JaxmppException {
		if ("message".equals(element.getName())) {
			if (element.getAttribute("id") == null) {
				element.setAttribute("id", UIDGenerator.next());
			}
			element.addChild(ElementFactory.create(REQUEST_NAME, null, XMLNS));
		}
		return element;
	}

	@Override
	public String[] getFeatures() {
		return new String[]{XMLNS};
	}

	public interface ReceiptReceivedHandler
			extends EventHandler {

		/**
		 * Called when Message Delivery Receipt is received.
		 *
		 * @param sessionObject session object.
		 * @param chat chat related to confirmed message.
		 * @param message received message with confirmation (this is not confirmed message!).
		 * @param confirmedId identifier of confirmed message. May be <code>null</code> if confirming client supports
		 * older version of Receipts.
		 */
		void onReceiptReceived(SessionObject sessionObject, Chat chat, Message message, String confirmedId);

		class ReceiptReceivedEvent
				extends JaxmppEvent<ReceiptReceivedHandler> {

			private Chat chat;

			private String confirmedId;

			private Message message;

			public ReceiptReceivedEvent(SessionObject sessionObject, Chat chat, Message msg, String confirmedId) {
				super(sessionObject);
				this.chat = chat;
				this.message = msg;
				this.confirmedId = confirmedId;
			}

			@Override
			public void dispatch(ReceiptReceivedHandler handler) {
				handler.onReceiptReceived(sessionObject, chat, message, confirmedId);
			}

			public Chat getChat() {
				return chat;
			}

			public void setChat(Chat chat) {
				this.chat = chat;
			}

			/**
			 * @return the confirmedId
			 */
			public String getConfirmedId() {
				return confirmedId;
			}

			/**
			 * @param confirmedId the confirmedId to set
			 */
			public void setConfirmedId(String confirmedId) {
				this.confirmedId = confirmedId;
			}

			/**
			 * @return the message
			 */
			public Message getMessage() {
				return message;
			}

			/**
			 * @param message the message to set
			 */
			public void setMessage(Message message) {
				this.message = message;
			}

		}
	}

}
