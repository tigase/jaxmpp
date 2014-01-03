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
package tigase.jaxmpp.core.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import tigase.jaxmpp.core.client.eventbus.Event;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.EventListener;
import tigase.jaxmpp.core.client.eventbus.MultiEventBus;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;

/**
 * Class for keeping many instances of {@linkplain JaxmppCore}
 * 
 */
public class MultiJaxmpp {

	private final ArrayList<Chat> chats = new ArrayList<Chat>();

	private final MultiEventBus eventBus = new MultiEventBus();

	private final HashMap<BareJID, JaxmppCore> jaxmpps = new HashMap<BareJID, JaxmppCore>();

	public MultiJaxmpp() {
		eventBus.addHandler(MessageModule.ChatCreatedHandler.ChatCreatedEvent.class, new MessageModule.ChatCreatedHandler() {

			@Override
			public void onChatCreated(SessionObject sessionObject, Chat chat, Message message) {
				chats.add(chat);
			}
		});

		eventBus.addHandler(MessageModule.ChatClosedHandler.ChatClosedEvent.class, new MessageModule.ChatClosedHandler() {

			@Override
			public void onChatClosed(SessionObject sessionObject, Chat chat) {
				chats.remove(chat);
			}
		});
	}

	/**
	 * Register implementation of {@linkplain JaxmppCore}
	 * 
	 * @param jaxmpp
	 *            {@linkplain JaxmppCore} instance
	 */
	public <T extends JaxmppCore> void add(final T jaxmpp) {
		synchronized (jaxmpps) {
			eventBus.addEventBus(jaxmpp.getEventBus());
			jaxmpps.put(jaxmpp.getSessionObject().getUserBareJid(), jaxmpp);
			this.chats.addAll(jaxmpp.getModule(MessageModule.class).getChatManager().getChats());
		}
	}

	public <H extends EventHandler> void addHandler(Class<? extends Event<H>> type, H handler) {
		eventBus.addHandler(type, handler);
	}

	public <H extends EventHandler> void addHandler(Class<? extends Event<H>> type, Object source, H handler) {
		eventBus.addHandler(type, source, handler);
	}

	public <H extends EventHandler> void addListener(Class<? extends Event<H>> type, EventListener listener) {
		eventBus.addListener(type, listener);
	}

	public <H extends EventHandler> void addListener(Class<? extends Event<H>> type, Object source, EventListener listener) {
		eventBus.addListener(type, source, listener);
	}

	public <H extends EventHandler> void addListener(EventListener listener) {
		eventBus.addListener(listener);
	}

	/**
	 * Returns collection of registered instances of {@linkplain JaxmppCore}
	 * 
	 * @return collection
	 */
	public Collection<JaxmppCore> get() {
		return Collections.unmodifiableCollection(jaxmpps.values());
	}

	/**
	 * Return instance of {@linkplain JaxmppCore} connected registered for
	 * specific user account.
	 * 
	 * @param userJid
	 *            user account
	 * @return {@linkplain JaxmppCore}
	 */
	@SuppressWarnings("unchecked")
	public <T extends JaxmppCore> T get(final BareJID userJid) {
		synchronized (jaxmpps) {
			return (T) jaxmpps.get(userJid);
		}
	}

	/**
	 * Returns instance of {@linkplain JaxmppCore} connected registered for
	 * specific user account represented by {@linkplain SessionObject}.
	 * 
	 * @param sessionObject
	 *            {@linkplain SessionObject} related to user account
	 * @return {@linkplain JaxmppCore}
	 */
	public <T extends JaxmppCore> T get(final SessionObject sessionObject) {
		return get(sessionObject.getUserBareJid());
	}

	/**
	 * Returns collection of all known {@linkplain Chat} from all registered
	 * {@linkplain JaxmppCore}.
	 * 
	 * @return collection of chats
	 */
	public List<Chat> getChats() {
		return Collections.unmodifiableList(chats);
	}

	public void remove(Class<? extends Event<?>> type, EventHandler handler) {
		eventBus.remove(type, handler);
	}

	public void remove(Class<? extends Event<?>> type, Object source, EventHandler handler) {
		eventBus.remove(type, source, handler);
	}

	public void remove(EventHandler handler) {
		eventBus.remove(handler);
	}

	/**
	 * Unregisters {@linkplain JaxmppCore}.
	 * 
	 * @param jaxmpp
	 *            {@linkplain JaxmppCore} to unregister.
	 */
	public <T extends JaxmppCore> void remove(final T jaxmpp) {
		synchronized (jaxmpps) {
			this.chats.removeAll(jaxmpp.getModule(MessageModule.class).getChatManager().getChats());
			eventBus.removeEventBus(jaxmpp.getEventBus());
			jaxmpps.remove(jaxmpp.getSessionObject().getUserBareJid());
		}
	}

}