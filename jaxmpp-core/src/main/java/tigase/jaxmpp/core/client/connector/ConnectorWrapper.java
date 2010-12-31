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
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class ConnectorWrapper implements Connector {

	private Connector connector;

	protected final Observable observable = new Observable();

	@Override
	public void addListener(EventType eventType, Listener<ConnectorEvent> listener) {
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
		return connector.getState();
	}

	@Override
	public boolean isSecure() {
		return connector.isSecure();
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

}
