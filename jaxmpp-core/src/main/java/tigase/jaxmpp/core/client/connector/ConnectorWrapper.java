/*
 * ConnectorWrapper.java
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
package tigase.jaxmpp.core.client.connector;

import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;

/**
 * Class for wrapping {@linkplain Connector Connector}.
 */
public class ConnectorWrapper
		implements Connector {

	private Connector connector;

	public ConnectorWrapper() {
	}

	@Override
	public XmppSessionLogic createSessionLogic(XmppModulesManager modulesManager, PacketWriter writer) {
		return connector.createSessionLogic(modulesManager, writer);
	}

	/**
	 * Returns wrapped connector.
	 *
	 * @return wrapped connector.
	 */
	public Connector getConnector() {
		return connector;
	}

	/**
	 * Sets connector to be wrapped.
	 *
	 * @param connector connector.
	 */
	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	@Override
	public State getState() {
		return connector == null ? State.disconnected : connector.getState();
	}

	@Override
	public boolean isCompressed() {
		return connector.isCompressed();
	}

	@Override
	public boolean isSecure() {
		return connector.isSecure();
	}

	@Override
	public void keepalive() throws JaxmppException {
		connector.keepalive();
	}

	@Override
	public void restartStream() throws JaxmppException {
		connector.restartStream();
	}

	@Override
	public void send(Element stanza) throws JaxmppException {
		connector.send(stanza);
	}

	@Override
	public void start() throws JaxmppException {
		connector.start();
	}

	@Override
	public void stop() throws JaxmppException {
		connector.stop();
	}

	@Override
	public void stop(boolean terminate) throws JaxmppException {
		connector.stop(terminate);
	}

}