/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc.
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

import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;

/**
 * Interface for classes with session logic.
 */
public interface XmppSessionLogic {

	/**
	 * Method executed just before login process is started. In this method
	 * implementation should register listeners.
	 */
	void beforeStart() throws JaxmppException;
	
	/**
	 * Set {@linkplain SessionListener}.
	 *
	 * @param listener
	 *            {@linkplain SessionListener}
	 */
	void setSessionListener(SessionListener listener) throws JaxmppException;

	/**
	 * In this method implementation must unregister all previously registered
	 * listeners.
	 */
	void unbind() throws JaxmppException;

	interface XmppSessionEstablishedHandler extends EventHandler {

		void onXmppSessionEstablished(SessionObject sessionObject) throws JaxmppException;

		class XmppSessionEstablishedEvent extends JaxmppEvent<XmppSessionEstablishedHandler> {

			public XmppSessionEstablishedEvent(SessionObject sessionObject) {
				super(sessionObject);
			}

			@Override
			public void dispatch(XmppSessionEstablishedHandler handler) throws Exception {
				handler.onXmppSessionEstablished(sessionObject);
			}

		}

	}

	/**
	 * Interface for session listener.
	 */
	interface SessionListener {

		void onException(JaxmppException e) throws JaxmppException;
	}
}