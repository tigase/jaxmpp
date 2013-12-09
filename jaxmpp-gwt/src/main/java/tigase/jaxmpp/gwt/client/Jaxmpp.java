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
package tigase.jaxmpp.gwt.client;

import java.util.Date;
import java.util.logging.Level;

import tigase.jaxmpp.core.client.AbstractSessionObject;
import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.JaxmppCore.ConnectedHandler.ConnectedEvent;
import tigase.jaxmpp.core.client.JaxmppCore.DisconnectedHandler.DisconnectedEvent;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.Processor;
import tigase.jaxmpp.core.client.ResponseManager;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppSessionLogic.SessionListener;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.core.client.connector.ConnectorWrapper;
import tigase.jaxmpp.core.client.connector.StreamError;
import tigase.jaxmpp.core.client.eventbus.DefaultEventBus;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.PingModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterStore;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat.DateTimeFormatProvider;
import tigase.jaxmpp.gwt.client.connectors.BoshConnector;
import tigase.jaxmpp.gwt.client.connectors.WebSocket;
import tigase.jaxmpp.gwt.client.connectors.WebSocketConnector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Cookies;

public class Jaxmpp extends JaxmppCore {

	private final static String COOKIE_RID_KEY = "jaxmpp-rid";

	private final ConnectorWrapper connectorWrapper = new ConnectorWrapper();;

	private Object lastRid;

	private RepeatingCommand timeoutChecker;

	{
		DateTimeFormat.setProvider(new DateTimeFormatProvider() {

			private final com.google.gwt.i18n.client.DateTimeFormat df1 = com.google.gwt.i18n.client.DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

			private final com.google.gwt.i18n.client.DateTimeFormat df2 = com.google.gwt.i18n.client.DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ssZ");

			@Override
			public String format(Date date) {
				return df1.format(date);
			}

			@Override
			public Date parse(String t) {
				try {
					return df1.parse(t);
				} catch (Exception e) {
					try {
						return df2.parse(t);
					} catch (Exception e1) {
						return null;
					}
				}
			}
		});
	}

	public Jaxmpp() {
		super();
		this.eventBus = new DefaultEventBus();
		this.sessionObject = new GwtSessionObject();
		init();
	}

	public Jaxmpp(SessionObject sessionObject) {
		super();
		this.eventBus = new DefaultEventBus();
		this.sessionObject = (AbstractSessionObject) sessionObject;
		init();

	}

	protected void checkTimeouts() throws JaxmppException {
		ResponseManager.getResponseManager(sessionObject).checkTimeouts();
		if (isConnected()) {
			Object r = sessionObject.getProperty(AbstractBoshConnector.RID_KEY);
			if (lastRid != null && lastRid.equals(r)) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {

					@Override
					public void execute() {
						JID jid = sessionObject.getProperty(ResourceBinderModule.BINDED_RESOURCE_JID);
						try {
							GWT.log("Checking if server lived");
							modulesManager.getModule(PingModule.class).ping(JID.jidInstance(jid.getDomain()),
									new AsyncCallback() {

										@Override
										public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
										}

										@Override
										public void onSuccess(Stanza responseStanza) throws XMLException {
										}

										@Override
										public void onTimeout() throws JaxmppException {
											try {
												disconnect();
											} catch (JaxmppException e) {
												onException(e);
											}
										}
									});
						} catch (Exception e) {
							try {
								onException(new JaxmppException(e));
							} catch (JaxmppException e1) {
								e1.printStackTrace();
							}
						}
					}
				});
			}
			lastRid = r;
		}
	}

	protected Connector createConnector() {
		String url = sessionObject.getProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY);
		if (url.startsWith("ws:") || url.startsWith("wss:")) {
			if (!WebSocket.isSupported()) {
				throw new RuntimeException("WebSocket protocol is not supported by browser");
			}
			return new WebSocketConnector(context);
		}

		return new BoshConnector(context);
	}

	@Override
	public void disconnect() throws JaxmppException {
		try {
			this.connector.stop();
		} catch (XMLException e) {
			throw new JaxmppException(e);
		}
	}

	@Override
	public void execute(final Runnable r) {
		if (r != null)
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {

				@Override
				public void execute() {
					r.run();
				}
			});
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ConnectionConfiguration getConnectionConfiguration() {
		return new ConnectionConfiguration(sessionObject);
	}

	public PacketWriter getWriter() {
		return writer;
	}

	@Override
	protected void init() {
		if (PresenceModule.getPresenceStore(sessionObject) == null)
			PresenceModule.setPresenceStore(sessionObject, new GWTPresenceStore());

		if (RosterModule.getRosterStore(sessionObject) == null)
			RosterModule.setRosterStore(sessionObject, new RosterStore());

		if (ResponseManager.getResponseManager(sessionObject) == null)
			ResponseManager.setResponseManager(sessionObject, new ResponseManager());

		super.init();

		this.timeoutChecker = new RepeatingCommand() {

			@Override
			public boolean execute() {
				try {
					checkTimeouts();
				} catch (Exception e) {
				}
				return true;
			}
		};
		Scheduler.get().scheduleFixedDelay(timeoutChecker, 1000 * 31);

		this.connector = this.connectorWrapper;

		this.processor = new Processor(this.modulesManager, context);

		sessionObject.setProperty(DiscoveryModule.IDENTITY_TYPE_KEY, "web");

		modulesInit();
	}

	private void intLogin() throws JaxmppException {
		if (this.sessionLogic != null) {
			this.sessionLogic.unbind();
			this.sessionLogic = null;
		}

		this.connectorWrapper.setConnector(createConnector());

		this.sessionLogic = connector.createSessionLogic(modulesManager, this.writer);
		this.sessionLogic.setSessionListener(new SessionListener() {

			@Override
			public void onException(JaxmppException e) throws JaxmppException {
				Jaxmpp.this.onException(e);
			}
		});

		try {
			this.connector.start();
		} catch (XMLException e1) {
			throw new JaxmppException(e1);
		}
	}

	@Override
	public void login() throws JaxmppException {
		if (this.isConnected()) {
			this.connector.stop(true);
		}

		lastRid = null;
		this.sessionObject.clear();
		intLogin();
	}

	@Override
	protected void onException(JaxmppException e) throws JaxmppException {
		log.log(Level.FINE, "Catching exception", e);
		try {
			connector.stop();
		} catch (Exception e1) {
			log.log(Level.FINE, "Disconnecting error", e1);
		}

		eventBus.fire(new DisconnectedEvent(sessionObject));
	}

	@Override
	protected void onResourceBindSuccess(JID bindedJID) throws JaxmppException {
		eventBus.fire(new ConnectedEvent(sessionObject));
	}

	@Override
	protected void onStreamError(StreamError condition, Throwable caught) throws JaxmppException {
		eventBus.fire(new DisconnectedEvent(sessionObject));
	}

	@Override
	protected void onStreamResumed(Long h, String previd) throws JaxmppException {
		eventBus.fire(new ConnectedEvent(sessionObject));
	}

	@Override
	protected void onStreamTerminated() throws JaxmppException {
		eventBus.fire(new DisconnectedEvent(sessionObject));
	}

	public void storeSession() {
		String s = ((GwtSessionObject) sessionObject).serialize();
		Cookies.setCookie(COOKIE_RID_KEY, s);
	}

}