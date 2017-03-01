/*
 * AbstractJaxmppTest.java
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

package tigase.jaxmpp.core.client;

import junit.framework.TestCase;
import org.junit.Before;
import tigase.jaxmpp.core.client.eventbus.DefaultEventBus;
import tigase.jaxmpp.core.client.eventbus.EventBus;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.ModuleProvider;
import tigase.jaxmpp.core.client.xmpp.stream.XmppStreamsManager;

public abstract class AbstractJaxmppTest
		extends TestCase {

	protected Context context;
	private DefaultEventBus eventBus;
	private MockSessionObject sessionObject;
	private XmppStreamsManager streamsManager;

	private MockWriter writer;

	public AbstractJaxmppTest() {
		this.context = new Context() {

			@Override
			public EventBus getEventBus() {
				return eventBus;
			}

			@Override
			public ModuleProvider getModuleProvider() {
				return null;
			}

			@Override
			public SessionObject getSessionObject() {
				return sessionObject;
			}

			@Override
			public XmppStreamsManager getStreamsManager() {
				return streamsManager;
			}

			@Override
			public PacketWriter getWriter() {
				return writer;
			}
		};
	}

	public Element poll() {
		return ((MockWriter) context.getWriter()).poll();
	}

	@Override
	@Before
	protected void setUp() throws Exception {
		super.setUp();
		this.eventBus = new DefaultEventBus();
		this.sessionObject = new MockSessionObject(eventBus);
		this.writer = new MockWriter(sessionObject);
		this.streamsManager = new XmppStreamsManager();
		this.streamsManager.setContext(context);
	}

}
