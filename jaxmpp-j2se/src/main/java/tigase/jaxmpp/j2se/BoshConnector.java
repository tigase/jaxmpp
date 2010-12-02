package tigase.jaxmpp.j2se;

import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.core.client.connector.BoshRequest;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class BoshConnector extends AbstractBoshConnector {

	@Override
	protected void processSendData(final Element element) throws XMLException, JaxmppException {
		BoshRequest worker = new BoshWorker(sessionObject, element) {

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

		(new Thread(worker)).start();
	}
}
