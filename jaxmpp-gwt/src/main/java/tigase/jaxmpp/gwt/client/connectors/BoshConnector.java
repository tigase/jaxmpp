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

		String u = sessionObject.getProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY);

		requestBuilder = new RequestBuilder(RequestBuilder.POST, u);
		requestBuilder.setHeader("Connection", "close");
	}

	@Override
	protected void processSendData(Element element) throws XMLException, JaxmppException {
		BoshWorker worker = new BoshWorker(requestBuilder, sessionObject, element) {

			@Override
			protected void onError(int responseCode, String responseData, Element response, Throwable caught) {
				BoshConnector.this.onError(responseCode, responseData, response, caught);
			}

			@Override
			protected void onSuccess(int responseCode, String responseData, Element response) throws JaxmppException {
				BoshConnector.this.onResponse(responseCode, responseData, response);
			}

			@Override
			protected void onTerminate(int responseCode, String responseData, Element response) {
				BoshConnector.this.onTerminate(responseCode, responseData, response);
			}

		};
		addToRequests(worker);

		BoshConnectorEvent event = new BoshConnectorEvent(StanzaSending);
		event.setBody(element);
		observable.fireEvent(event);

		Scheduler.get().scheduleDeferred(worker);
	}

}
