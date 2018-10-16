/*
 * ChatStateExtension.java
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.jaxmpp.core.client.xmpp.modules.chat.xep0085;

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.ContextAware;
import tigase.jaxmpp.core.client.xmpp.modules.chat.AbstractChatManager;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.chat.xep0085.ChatStateExtension.ChatStateChangedHandler.ChatStateChangedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.extensions.Extension;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author andrzej
 */
public class ChatStateExtension
		implements Extension, ContextAware, MessageModule.ChatClosedHandler, JaxmppCore.LoggedOutHandler {

	public static final String CHAT_STATE_DISABLED_KEY = "xep-0085#disabled";
	private static final Logger log = Logger.getLogger(ChatStateExtension.class.getCanonicalName());
	private static final String[] FEATURES = {ChatState.XMLNS};
	private final AbstractChatManager chatManager;
	private final Map<Long, StateHolder> chatStates = new HashMap<Long, StateHolder>();
	private Context context;

	public static boolean isDisabled(SessionObject sessionObject) {
		Boolean value = sessionObject.getProperty(CHAT_STATE_DISABLED_KEY);
		return (value != null && value);
	}

	public ChatStateExtension(AbstractChatManager chatManager) {
		this.chatManager = chatManager;
	}

	@Override
	public Element afterReceive(Element received) throws JaxmppException {
		Message msg = (Message) Stanza.create(received);
		if (msg.getType() == StanzaType.chat && !isDisabled(context.getSessionObject())) {
			Chat chat = chatManager.getChat(msg.getFrom(), msg.getThread());
			if (chat != null) {
				List<Element> elems = received.getChildrenNS(ChatState.XMLNS);
				if (elems != null && !elems.isEmpty()) {
					ChatState state = ChatState.fromElement(elems.get(0));
					StateHolder holder;
					synchronized (chatStates) {
						holder = chatStates.get(chat.getId());
						if (holder == null) {
							holder = new StateHolder();
							chatStates.put(chat.getId(), holder);
						}
					}
					boolean changed = holder.setRecipientChatState(state);
					if (changed) {
						context.getEventBus().fire(new ChatStateChangedEvent(context.getSessionObject(), chat, state));
					}
				}
			}
		}
		return received;
	}

	@Override
	public Element beforeSend(Element received) throws JaxmppException {
		Message msg = (Message) Stanza.create(received);
		if (msg.getType() == StanzaType.chat && !isDisabled(context.getSessionObject())) {
			Chat chat = chatManager.getChat(msg.getTo(), msg.getThread());
			if (chat != null) {
				StateHolder holder;
				synchronized (chatStates) {
					holder = chatStates.get(chat.getId());
				}
				if (holder != null) {
					holder.setOwnChatState(ChatState.active);
					received.addChild(ChatState.active.toElement());
				}
			}
		}
		return received;
	}

	@Override
	public String[] getFeatures() {
		if (!isDisabled(context.getSessionObject())) {
			return FEATURES;
		}
		return null;
	}

	public ChatState getRecipientChatState(Chat chat) {
		StateHolder holder;
		synchronized (chatStates) {
			holder = chatStates.get(chat.getId());
		}
		return holder == null ? null : holder.getRecipientChatState();
	}

	@Override
	public void onChatClosed(SessionObject sessionObject, Chat chat) {
		try {
			if (!isDisabled(sessionObject)) {
				setOwnChatState(chat, ChatState.gone);
			}
		} catch (JaxmppException ex) {
			log.log(Level.FINE, "Exception while sending gone notification on chat close", ex);
		}
		synchronized (chatStates) {
			chatStates.remove(chat.getId());
		}
	}

	@Override
	public void onLoggedOut(SessionObject sessionObject) {
		for (Chat chat : chatManager.getChats()) {
			StateHolder holder;
			synchronized (chatStates) {
				holder = chatStates.remove(chat.getId());
			}
			if (holder != null && holder.getRecipientChatState() != null) {
				context.getEventBus().fire(new ChatStateChangedEvent(context.getSessionObject(), chat, null));
			}
		}
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
		context.getEventBus().addHandler(MessageModule.ChatClosedHandler.ChatClosedEvent.class, this);
		context.getEventBus().addHandler(LoggedOutEvent.class, this);
	}

	public void setDisabled(boolean disabled) {
		context.getSessionObject().setProperty(CHAT_STATE_DISABLED_KEY, disabled);
		if (disabled) {
			for (Chat chat : chatManager.getChats()) {
				try {
					StateHolder holder;
					synchronized (chatStates) {
						holder = chatStates.remove(chat.getId());
					}
					if (holder != null) {
						if (holder.getRecipientChatState() != null) {
							context.getEventBus().fire(new ChatStateChangedEvent(context.getSessionObject(), chat, null));
						}
						if (holder.getOwnChatState() != null && (holder.getOwnChatState() != ChatState.active ||
								holder.getOwnChatState() != ChatState.gone)) {
							setOwnChatState(chat, ChatState.active, true);
						}
					}
				} catch (JaxmppException ex) {
					log.log(Level.FINE, "Exception sending chat state on disabling chat state support", ex);
				}
			}
		}
	}

	public void setOwnChatState(Chat chat, ChatState state) throws JaxmppException {
		setOwnChatState(chat, state, false);
	}

	private void setOwnChatState(Chat chat, ChatState state, boolean force) throws JaxmppException {
		if (!force && isDisabled(chat.getSessionObject())) {
			return;
		}

		PresenceStore presenceStore = PresenceModule.getPresenceStore(context.getSessionObject());
		if (presenceStore == null || !presenceStore.isAvailable(chat.getJid().getBareJid())) {
			return;
		}

		StateHolder holder;
		synchronized (chatStates) {
			holder = chatStates.get(chat.getId());
			if (holder == null) {
				holder = new StateHolder();
				chatStates.put(chat.getId(), holder);
			}
		}
		boolean changed = holder.setOwnChatState(state);
		if (changed) {
			Message msg = Message.create();
			msg.setTo(chat.getJid());
			msg.setType(StanzaType.chat);
			msg.addChild(state.toElement());
			context.getWriter().write(msg);
		}
	}

	public interface ChatStateChangedHandler
			extends EventHandler {

		void onChatStateChanged(SessionObject sessionObject, Chat chat, ChatState state);

		class ChatStateChangedEvent
				extends JaxmppEvent<ChatStateChangedHandler> {

			private final Chat chat;
			private final ChatState chatState;

			public ChatStateChangedEvent(SessionObject sessionObject, Chat chat, ChatState state) {
				super(sessionObject);
				this.chat = chat;
				this.chatState = state;
			}

			@Override
			public void dispatch(ChatStateChangedHandler handler) throws Exception {
				handler.onChatStateChanged(sessionObject, chat, chatState);
			}
		}
	}

	private class StateHolder {

		private ChatState ownChatState;
		private ChatState recipientChatState;

		public ChatState getOwnChatState() {
			return ownChatState;
		}

		public ChatState getRecipientChatState() {
			return recipientChatState;
		}

		public boolean setOwnChatState(ChatState state) {
			boolean changed = ownChatState != state;
			ownChatState = state;
			return changed;
		}

		public boolean setRecipientChatState(ChatState state) {
			boolean changed = recipientChatState != state;
			recipientChatState = state;
			return changed;
		}

	}
}
