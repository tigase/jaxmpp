package tigase.jaxmpp.j2se;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.DefaultSessionObject;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.Processor;
import tigase.jaxmpp.core.client.XmppSessionLogic.SessionListener;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;
import tigase.jaxmpp.j2se.connectors.bosh.BoshConnector;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

public class Jaxmpp extends JaxmppCore {

	public static final String CONNECTOR_TYPE = "connectorType";

	public static final String EXCEPTION_KEY = "jaxmpp#ThrowedException";

	public static final String SYNCHRONIZED_MODE = "jaxmpp#synchronized";

	private final Timer timer = new Timer(true);

	{
		DateTimeFormat.setProvider(new DateTimeFormatProviderImpl());
	}

	public Jaxmpp() {
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

		this.sessionObject = new DefaultSessionObject();
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

	public void disconnect(boolean b) throws JaxmppException {
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
			this.connector.start();
			this.sessionObject.setProperty(SYNCHRONIZED_MODE, Boolean.valueOf(sync));
			if (sync)
				synchronized (Jaxmpp.this) {
					Jaxmpp.this.wait();
					log.finest("Waked up");
				}
		} catch (Exception e1) {
			throw new JaxmppException(e1);
		}
		if (sessionObject.getProperty(EXCEPTION_KEY) != null) {
			JaxmppException r = (JaxmppException) sessionObject.getProperty(EXCEPTION_KEY);
			JaxmppException e = new JaxmppException(r.getMessage(), r.getCause());
			throw e;
		}
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
		JaxmppEvent event = new JaxmppEvent(Disconnected);
		observable.fireEvent(event);
	}

	@Override
	protected void onResourceBinded(ResourceBindEvent be) throws JaxmppException {
		synchronized (Jaxmpp.this) {
			// (new Exception("DEBUG")).printStackTrace();
			Jaxmpp.this.notify();
		}
		JaxmppEvent event = new JaxmppEvent(Connected);
		observable.fireEvent(event);
	}

	@Override
	protected void onStanzaReceived(Element stanza) {
		Runnable r = this.processor.process(stanza);
		if (r != null)
			(new Thread(r)).start();
	}

	@Override
	protected void onStreamError(ConnectorEvent be) throws JaxmppException {
		synchronized (Jaxmpp.this) {
			// (new Exception("DEBUG")).printStackTrace();
			Jaxmpp.this.notify();
		}
		JaxmppEvent event = new JaxmppEvent(Disconnected);
		observable.fireEvent(event);
	}

	@Override
	protected void onStreamTerminated(ConnectorEvent be) throws JaxmppException {
		synchronized (Jaxmpp.this) {
			// (new Exception("DEBUG")).printStackTrace();
			Jaxmpp.this.notify();
		}
		JaxmppEvent event = new JaxmppEvent(Disconnected);
		observable.fireEvent(event);
	}

	@Override
	public void send(Stanza stanza) throws XMLException, JaxmppException {
		this.writer.write(stanza);
	}

	@Override
	public void send(Stanza stanza, AsyncCallback asyncCallback) throws XMLException, JaxmppException {
		this.sessionObject.registerResponseHandler(stanza, asyncCallback);
		this.writer.write(stanza);
	}

}
