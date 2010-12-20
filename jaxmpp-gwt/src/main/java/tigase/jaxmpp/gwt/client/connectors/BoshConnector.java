package tigase.jaxmpp.gwt.client.connectors;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.RequestBuilder;

public class BoshConnector extends AbstractBoshConnector {

	private final RequestBuilder requestBuilder;

	public BoshConnector(SessionObject sessionObject) {
		super(sessionObject);

		String u = sessionObject.getProperty(AbstractBoshConnector.BOSH_SERVICE_URL);

		requestBuilder = new RequestBuilder(RequestBuilder.POST, u);
		requestBuilder.setHeader("Connection", "close");
	}

	@Override
	protected void processSendData(Element element) throws XMLException, JaxmppException {
		BoshWorker worker = new BoshWorker(requestBuilder, sessionObject, element) {

			@Override
			protected void onError(int responseCode, Element response, Throwable caught) {
				BoshConnector.this.onError(responseCode, response, caught);
			}

			@Override
			protected void onSuccess(int responseCode, Element response) throws JaxmppException {
				BoshConnector.this.onResponse(responseCode, response);
			}

			@Override
			protected void onTerminate(int responseCode, Element response) {
				BoshConnector.this.onTerminate(responseCode, response);
			}

		};
		addToRequests(worker);

		Scheduler.get().scheduleDeferred(worker);
	}

}
