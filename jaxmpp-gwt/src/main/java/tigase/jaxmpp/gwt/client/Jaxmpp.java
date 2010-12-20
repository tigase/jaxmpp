package tigase.jaxmpp.gwt.client;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.DefaultSessionObject;
import tigase.jaxmpp.core.client.JaxmppCore;
import tigase.jaxmpp.core.client.Processor;
import tigase.jaxmpp.core.client.XmppSessionLogic.SessionListener;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.logger.LogLevel;
import tigase.jaxmpp.core.client.logger.LoggerSpiFactory;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.gwt.client.connectors.BoshConnector;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

public class Jaxmpp extends JaxmppCore {

	public Jaxmpp(LoggerSpiFactory defaultLoggerSpi) {
		super(defaultLoggerSpi);

		this.sessionObject = new DefaultSessionObject();
		this.processor = new Processor(this.modulesManager, this.sessionObject, this.writer);

		modulesInit();

		ResourceBinderModule r = this.modulesManager.getModule(ResourceBinderModule.class);
		r.addListener(ResourceBinderModule.ResourceBindSuccess, resourceBindListener);
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
	public void login() throws JaxmppException {
		this.sessionObject.clear();

		if (this.sessionLogic != null) {
			this.sessionLogic.unbind();
			this.sessionLogic = null;
		}
		if (this.connector != null) {
			this.connector.removeAllListeners();
			this.connector = null;
		}

		this.connector = new BoshConnector(this.sessionObject);

		this.connector.addListener(Connector.StanzaReceived, this.stanzaReceivedListener);
		connector.addListener(Connector.StreamTerminated, this.streamTerminateListener);
		connector.addListener(Connector.Error, this.streamErrorListener);

		this.sessionLogic = connector.createSessionLogic(modulesManager, this.writer);
		this.sessionLogic.bind(new SessionListener() {

			@Override
			public void onException(JaxmppException e) {
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
	protected void onException(JaxmppException e) {
		log.log(LogLevel.FINE, "Catching exception", e);
		try {
			connector.stop();
		} catch (Exception e1) {
			log.log(LogLevel.FINE, "Disconnecting error", e1);
		}
		synchronized (Jaxmpp.this) {
			// (new Exception("DEBUG")).printStackTrace();
			Jaxmpp.this.notify();
		}
		JaxmppEvent event = new JaxmppEvent(Disconnected);
		observable.fireEvent(event);
	}

	@Override
	protected void onResourceBinded(ResourceBindEvent be) {
		JaxmppEvent event = new JaxmppEvent(Connected);
		observable.fireEvent(event);
	}

	@Override
	protected void onStanzaReceived(Element stanza) {
		final Runnable r = this.processor.process(stanza);
		if (r != null)
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {

				@Override
				public void execute() {
					r.run();
				}
			});
	}

	@Override
	protected void onStreamError(ConnectorEvent be) {
		JaxmppEvent event = new JaxmppEvent(Disconnected);
		observable.fireEvent(event);
	}

	@Override
	protected void onStreamTerminated(ConnectorEvent be) {
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
