package tigase.jaxmpp.j2se.connectors.bosh;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.Queue;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.connector.BoshRequest;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.j2se.xml.J2seElement;
import tigase.xml.DomBuilderHandler;
import tigase.xml.SimpleParser;

public abstract class BoshWorker implements BoshRequest {

	private final Element body;

	private HttpURLConnection conn;

	private final DomBuilderHandler domHandler;

	private final SimpleParser parser;

	private final String rid;

	private final SessionObject sessionObject;

	private boolean terminated = false;

	public BoshWorker(DomBuilderHandler domHandler, SimpleParser parser, SessionObject sessionObject, Element body)
			throws XMLException, JaxmppException {
		this.domHandler = domHandler;
		this.parser = parser;
		this.sessionObject = sessionObject;

		this.body = body;
		this.rid = body.getAttribute("rid");
		if (this.rid == null)
			throw new RuntimeException("rid must be defined");
	}

	@Override
	public String getRid() {
		return rid;
	}

	protected abstract void onError(int responseCode, String responseData, Element response, Throwable caught)
			throws JaxmppException;

	protected abstract void onSuccess(int responseCode, String responseData, Element response) throws JaxmppException;

	protected abstract void onTerminate(int responseCode, String responseData, Element response) throws JaxmppException;

	@Override
	public void run() {
		if (terminated)
			return;
		try {
			try {
				URL url = sessionObject.getProperty(BoshConnector.URL_KEY);
				this.conn = (HttpURLConnection) (url.openConnection());
				String b = body.getAsString();
				// System.out.println("S: " + b);

				if (!conn.getDoOutput())
					conn.setDoOutput(true);
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(b);
				wr.flush();

				final int responseCode = conn.getResponseCode();

				StringBuilder sb = new StringBuilder();
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				while ((line = rd.readLine()) != null) {
					sb.append(line);
				}

				final String responseData = sb.toString();

				if (responseCode != 200) {
					onError(responseCode, responseData, null, null);
					return;
				}
				// wr.close();
				// rd.close();

				// System.out.println("R: " + sb.toString());

				parser.parse(domHandler, responseData.toCharArray(), 0, responseData.length());

				Queue<tigase.xml.Element> elems = domHandler.getParsedElements();

				tigase.xml.Element elem;
				while ((elem = elems.poll()) != null) {
					final String type = elem.getAttribute("type");
					Element response = new J2seElement(elem);
					if (type != null && "terminate".equals(type)) {
						onTerminate(responseCode, responseData, response);
					} else if (type != null && "error".equals(type)) {
						onError(responseCode, responseData, response, null);
					} else if (type == null) {
						onSuccess(responseCode, responseData, response);
					} else
						throw new RuntimeException("Unknown response type '" + type + "'");
				}

			} catch (SocketException e) {
				if (terminated)
					return;
				onError(0, null, null, e);
			} catch (Exception e) {
				e.printStackTrace();
				onError(0, null, null, e);
			}
		} catch (JaxmppException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void terminate() {
		terminated = true;
		if (conn != null)
			conn.disconnect();
	}

}
