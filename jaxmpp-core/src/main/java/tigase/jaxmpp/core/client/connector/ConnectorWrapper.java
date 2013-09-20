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
package tigase.jaxmpp.core.client.connector;

import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class ConnectorWrapper implements Connector {

	private Connector connector;


	public ConnectorWrapper(Observable parentObservable) {
		this.observable = ObservableFactory.instance(parentObservable);
	}

	@Override
	public void addListener(EventType eventType, Listener<? extends ConnectorEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<? extends BaseEvent> listener) {
		observable.addListener(listener);
	}

	@Override
	public XmppSessionLogic createSessionLogic(XmppModulesManager modulesManager, PacketWriter writer) {
		return connector.createSessionLogic(modulesManager, writer);
	}

	public Connector getConnector() {
		return connector;
	}

	@Override
	public Observable getObservable() {
		return null;
	}

	@Override
	public State getState() {
		return connector == null ? State.disconnected : connector.getState();
	}

	@Override
	public boolean isSecure() {
		return connector.isSecure();
	}

	/**
	 * Returns true when stream is compressed
	 * 
	 * @return
	 */
	@Override
	public boolean isCompressed() {
		return connector.isCompressed();
	}

	@Override
	public void keepalive() throws JaxmppException {
		connector.keepalive();
	}

	@Override
	@Deprecated
	public void removeAllListeners() {
		connector.removeAllListeners();
	}

	@Override
	public void removeListener(EventType eventType, Listener<ConnectorEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	@Override
	public void restartStream() throws XMLException, JaxmppException {
		connector.restartStream();
	}

	@Override
	public void send(Element stanza) throws XMLException, JaxmppException {
		connector.send(stanza);
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
		if (this.connector != null) {
			this.connector.setObservable(observable);
		}
	}

	@Override
	public void setObservable(Observable observable) {
	}

	@Override
	public void start() throws XMLException, JaxmppException {
		connector.start();
	}

	@Override
	public void stop() throws XMLException, JaxmppException {
		connector.stop();
	}

	@Override
	public void stop(boolean terminate) throws XMLException, JaxmppException {
		connector.stop(terminate);
	}

}