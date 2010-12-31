package tigase.jaxmpp.gwt.client.connectors;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.connector.BoshRequest;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.gwt.client.xml.GwtElement;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.xml.client.XMLParser;

public abstract class BoshWorker implements BoshRequest, ScheduledCommand {

	private final RequestCallback callback;

	private Element element;

	private final RequestBuilder requestBuilder;

	private final String rid;

	public BoshWorker(RequestBuilder requestBuilder, SessionObject sessionObject, Element element) throws XMLException {
		this.requestBuilder = requestBuilder;
		this.element = element;
		this.rid = element.getAttribute("rid");
		if (this.rid == null)
			throw new RuntimeException("rid must be defined");
		this.callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				try {
					BoshWorker.this.onError(-1, null, exception);
				} catch (JaxmppException e) {
				}
			}

			@Override
			public void onResponseReceived(Request request, Response $response) {
				try {
					int responseCode = $response.getStatusCode();
					if (responseCode != 200)
						BoshWorker.this.onError($response.getStatusCode(), null, null);
					String x = $response.getText().replaceAll("&semi;", ";");
					System.out.println("<< " + x);
					final GwtElement response = new GwtElement(XMLParser.parse(x).getDocumentElement());

					final String type = response.getAttribute("type");
					if (type != null && "terminate".equals(type)) {
						BoshWorker.this.onTerminate(responseCode, response);
					} else if (type != null && "error".equals(type)) {
						BoshWorker.this.onError(responseCode, response, null);
					} else if (type == null) {
						BoshWorker.this.onSuccess(responseCode, response);
					} else
						throw new RuntimeException("Unknown response type '" + type + "'");
				} catch (JaxmppException e) {
				} catch (XMLException e) {
				}
			}

		};
	}

	@Override
	public void execute() {
		try {
			String x = element.getAsString();
			System.out.println(">> " + x);
			requestBuilder.sendRequest(x, callback);
		} catch (Exception e) {
			try {
				onError(-1, null, e);
			} catch (JaxmppException e1) {
			}
		}
	}

	@Override
	public String getRid() {
		return rid;
	}

	protected abstract void onError(int responseCode, Element response, Throwable caught) throws JaxmppException;

	protected abstract void onSuccess(int responseCode, Element response) throws JaxmppException;

	protected abstract void onTerminate(int responseCode, Element response) throws JaxmppException;

	@Override
	public void run() {
		execute();
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub

	}

}
