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

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule.MessageEvent;

public class MultiJaxmpp {

	private final ArrayList<Chat> chats = new ArrayList<Chat>();

	private final HashMap<BareJID, JaxmppCore> jaxmpps = new HashMap<BareJID, JaxmppCore>();

	private final Listener<BaseEvent> listener;

	private final Observable observable = ObservableFactory.instance();

	public MultiJaxmpp() {
		this.listener = new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) throws JaxmppException {
				if (be.getType() == MessageModule.ChatCreated) {
					chats.add(((MessageEvent) be).getChat());
				} else if (be.getType() == MessageModule.ChatClosed) {
					chats.remove(((MessageEvent) be).getChat());
				}
				observable.fireEvent(be);
			}
		};
	}

	public <T extends JaxmppCore> void add(final T jaxmpp) {
		synchronized (jaxmpps) {
			jaxmpp.addListener(listener);
			jaxmpps.put(jaxmpp.getSessionObject().getUserBareJid(), jaxmpp);
			this.chats.addAll(jaxmpp.getModule(MessageModule.class).getChatManager().getChats());
		}
	}

	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<? extends BaseEvent> listener) {
		observable.addListener(listener);
	}

	public Collection<JaxmppCore> get() {
		return Collections.unmodifiableCollection(jaxmpps.values());
	}

	public <T extends JaxmppCore> T get(final BareJID userJid) {
		synchronized (jaxmpps) {
			return (T) jaxmpps.get(userJid);
		}
	}

	public <T extends JaxmppCore> T get(final SessionObject sessionObject) {
		return get(sessionObject.getUserBareJid());
	}

	public List<Chat> getChats() {
		return Collections.unmodifiableList(chats);
	}

	public <T extends JaxmppCore> void remove(final T jaxmpp) {
		synchronized (jaxmpps) {
			this.chats.removeAll(jaxmpp.getModule(MessageModule.class).getChatManager().getChats());
			jaxmpp.removeListener(listener);
			jaxmpps.remove(jaxmpp.getSessionObject().getUserBareJid());
		}
	}

	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	public void removeListener(Listener<? extends BaseEvent> listener) {
		observable.removeListener(listener);
	}

}