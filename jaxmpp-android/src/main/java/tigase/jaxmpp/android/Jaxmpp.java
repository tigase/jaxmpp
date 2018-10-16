/*
 * Jaxmpp.java
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
package tigase.jaxmpp.android;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.factory.UniversalFactory;
import tigase.jaxmpp.core.client.factory.UniversalFactory.FactorySpi;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector.DnsResolver;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Jaxmpp
		extends tigase.jaxmpp.j2se.Jaxmpp {

	static {
		UniversalFactory.setSpi(DnsResolver.class.getName(), new FactorySpi<DnsResolver>() {

			@Override
			public DnsResolver create() {
				return new AndroidDNSResolver();
			}
		});
	}

	private final Executor stanzaSender = Executors.newSingleThreadExecutor();

	public Jaxmpp() {
		super();
		writer = new ThreadBasedPacketWriter();
	}

	public Jaxmpp(SessionObject sessionObject) {
		super(sessionObject);
		writer = new ThreadBasedPacketWriter();
	}

	private class ThreadBasedPacketWriter
			extends DefaultPacketWriter {

		private void wakeUp() {
			synchronized (ThreadBasedPacketWriter.this) {
				this.notify();
			}
		}

		@Override
		public void write(final Element stanza) throws JaxmppException {
			final JaxmppException[] exception = new JaxmppException[]{null};
			stanzaSender.execute(new Runnable() {
				@Override
				public void run() {
					try {
						ThreadBasedPacketWriter.super.write(stanza);
					} catch (JaxmppException e) {
						exception[0] = e;
						e.printStackTrace();
					} finally {
						wakeUp();
					}
				}
			});
			synchronized (ThreadBasedPacketWriter.this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (exception[0] != null) {
				throw exception[0];
			}
		}
	}

}
