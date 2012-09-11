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
package tigase.jaxmpp.j2se;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.logging.Level;

import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.DefaultSessionObject;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.Processor;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppSessionLogic.SessionListener;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.observer.ObservableFactory;
import tigase.jaxmpp.core.client.observer.ObservableFactory.FactorySpi;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindEvent;
import tigase.jaxmpp.core.client.xmpp.modules.capabilities.CapabilitiesModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;
import tigase.jaxmpp.j2se.connectors.bosh.BoshConnector;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;
import tigase.jaxmpp.j2se.observer.ThreadSafeObservable;

public class Jaxmpp extends JaxmppCore {

	public static final String CONNECTOR_TYPE = "connectorType";

	private static final Executor DEFAULT_EXECUTOR = new Executor() {

		@Override
		public void execute(Runnable command) {
			(new Thread(command)).start();
		}
	};

	public static final String EXCEPTION_KEY = "jaxmpp#ThrowedException";;

	public static final String SYNCHRONIZED_MODE = "jaxmpp#synchronized";

	static {
		ObservableFactory.setFactorySpi(new FactorySpi() {

			@Override
			public Observable create() {
				return create(null);
			}

			@Override
			public Observable create(Observable parent) {
				return new ThreadSafeObservable(parent);
			}
		});
		DateTimeFormat.setProvider(new DateTimeFormatProviderImpl());
	}

	private Executor executor;

	private final Timer timer = new Timer(true);

	public Jaxmpp() {
		this(new DefaultSessionObject());
		setExecutor(DEFAULT_EXECUTOR);
	}

	public Jaxmpp(SessionObject sessionObject) {
		super(sessionObject);
		setExecutor(DEFAULT_EXECUTOR);
		TimerTask checkTimeouts = new TimerTask() {

			@Override
			public void run() {
				try {
					checkTimeouts();
				} catch (JaxmppException e) {
					e.printStackTrace();
				}
			}
		};
		timer.schedule(checkTimeouts, 30 * 1000, 30 * 1000);

		this.processor = new Processor(this.modulesManager, this.sessionObject, this.writer);

		modulesInit();

		ResourceBinderModule r = this.modulesManager.getModule(ResourceBinderModule.class);
		r.addListener(ResourceBinderModule.ResourceBindSuccess, resourceBindListener);

	}

	protected void checkTimeouts() throws JaxmppException {
		sessionObject.checkHandlersTimeout();
	}

	@Override
	public void disconnect() throws JaxmppException {
		disconnect(false);
	}

	public void disconnect(boolean snc) throws JaxmppException {
		if (this.connector != null) {
			try {
				this.connector.stop();
			} catch (XMLException e) {
				throw new JaxmppException(e);
			}
			Boolean sync = (Boolean) this.sessionObject.getProperty(SYNCHRONIZED_MODE);
			if (sync != null && sync) {
				synchronized (Jaxmpp.this) {
					// Jaxmpp.this.wait();
				}
			}
		}
	}

	public Executor getExecutor() {
		return executor;
	}

	@Override
	public void login() throws JaxmppException {
		login(true);
	}

	public void login(boolean sync) throws JaxmppException {
		this.sessionObject.clear();

		if (this.sessionLogic != null) {
			this.sessionLogic.unbind();
			this.sessionLogic = null;
		}
		if (this.connector != null) {
			this.connector.removeAllListeners();
			this.connector = null;
		}

		if (sessionObject.getProperty(CONNECTOR_TYPE) == null || "socket".equals(sessionObject.getProperty(CONNECTOR_TYPE))) {
			log.info("Using SocketConnector");
			this.connector = new SocketConnector(observable, this.sessionObject);
		} else if ("bosh".equals(sessionObject.getProperty(CONNECTOR_TYPE))) {
			log.info("Using BOSHConnector");
			this.connector = new BoshConnector(observable, this.sessionObject);
		} else
			throw new JaxmppException("Unknown connector type");

		this.connector.addListener(Connector.StanzaReceived, this.stanzaReceivedListener);
		connector.addListener(Connector.StreamTerminated, this.streamTerminateListener);
		connector.addListener(Connector.Error, this.streamErrorListener);

		this.sessionLogic = connector.createSessionLogic(modulesManager, this.writer);
		this.sessionLogic.bind(new SessionListener() {

			@Override
			public void onException(JaxmppException e) throws JaxmppException {
				Jaxmpp.this.onException(e);
			}
		});

		try {
			this.sessionLogic.beforeStart();
			this.connector.start();
			this.sessionObject.setProperty(SYNCHRONIZED_MODE, Boolean.valueOf(sync));
			if (sync)
				synchronized (Jaxmpp.this) {
					Jaxmpp.this.wait();
					log.finest("Waked up");
				}
			if (sessionObject.getProperty(EXCEPTION_KEY) != null) {
				JaxmppException r = (JaxmppException) sessionObject.getProperty(EXCEPTION_KEY);
				JaxmppException e = new JaxmppException(r.getMessage(), r.getCause());
				throw e;
			}
		} catch (JaxmppException e) {
			// onException(e);
			throw e;
		} catch (Exception e1) {
			JaxmppException e = new JaxmppException(e1);
			// onException(e);
			throw e;
		}
	}

	@Override
	protected void modulesInit() {
		super.modulesInit();

		this.modulesManager.register(new CapabilitiesModule(sessionObject, writer,
				this.modulesManager.getModule(DiscoInfoModule.class), this.modulesManager.getModule(PresenceModule.class),
				this.modulesManager));
	}

	@Override
	protected void onException(JaxmppException e) throws JaxmppException {
		log.log(Level.FINE, "Catching exception", e);
		sessionObject.setProperty(EXCEPTION_KEY, e);
		try {
			connector.stop();
		} catch (Exception e1) {
			log.log(Level.FINE, "Disconnecting error", e1);
		}
		synchronized (Jaxmpp.this) {
			// (new Exception("DEBUG")).printStackTrace();
			Jaxmpp.this.notify();
		}
		JaxmppEvent event = new JaxmppEvent(Disconnected, sessionObject);
		observable.fireEvent(event);
	}

	@Override
	protected void onResourceBinded(ResourceBindEvent be) throws JaxmppException {
		synchronized (Jaxmpp.this) {
			// (new Exception("DEBUG")).printStackTrace();
			Jaxmpp.this.notify();
		}
		JaxmppEvent event = new JaxmppEvent(Connected, sessionObject);
		observable.fireEvent(event);
	}

	@Override
	protected void onStanzaReceived(Element stanza) {
		Runnable r = this.processor.process(stanza);
		if (r != null)
			executor.execute(r);
	}

	@Override
	protected void onStreamError(ConnectorEvent be) throws JaxmppException {
		synchronized (Jaxmpp.this) {
			// (new Exception("DEBUG")).printStackTrace();
			Jaxmpp.this.notify();
		}
		JaxmppEvent event = new JaxmppEvent(Disconnected, sessionObject);
		observable.fireEvent(event);
	}

	@Override
	protected void onStreamTerminated(ConnectorEvent be) throws JaxmppException {
		synchronized (Jaxmpp.this) {
			// (new Exception("DEBUG")).printStackTrace();
			Jaxmpp.this.notify();
		}
		JaxmppEvent event = new JaxmppEvent(Disconnected, sessionObject);
		observable.fireEvent(event);
	}

	public void setExecutor(Executor executor) {
		if (executor == null)
			this.executor = DEFAULT_EXECUTOR;
		else
			this.executor = executor;
	}

}