package tigase.jaxmpp.j2se;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.core.client.connector.BoshRequest;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.j2se.xml.J2seElement;
import tigase.xml.DomBuilderHandler;
import tigase.xml.SimpleParser;
import tigase.xml.SingletonFactory;

public abstract class BoshWorker implements BoshRequest {

	private final Element body;

	private HttpURLConnection conn;

	private final DomBuilderHandler domHandler = new DomBuilderHandler();

	private final SimpleParser parser = SingletonFactory.getParserInstance();

	private final String rid;

	private boolean terminated = false;

	public BoshWorker(SessionObject sessionObject, Element body) throws XMLException, JaxmppException {
		try {
			this.conn = (HttpURLConnection) (new URL((String) sessionObject.getProperty(AbstractBoshConnector.BOSH_SERVICE_URL)).openConnection());
		} catch (Exception e) {
			throw new JaxmppException(e);
		}
		this.body = body;
		this.rid = body.getAttribute("rid");
		if (this.rid == null)
			throw new RuntimeException("rid must be defined");
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
		if (terminated)
			return;
		try {
			try {
				String b = body.getAsString();
				// System.out.println("S: " + b);

				conn.setDoOutput(true);
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(b);
				wr.flush();

				final int responseCode = conn.getResponseCode();
				if (responseCode != 200) {
					onError(responseCode, null, null);
				} else {
					StringBuilder sb = new StringBuilder();
					BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String line;
					while ((line = rd.readLine()) != null) {
						sb.append(line);
					}
					wr.close();
					rd.close();

					// System.out.println("R: " + sb.toString());

					parser.parse(domHandler, sb.toString().toCharArray(), 0, sb.length());
					tigase.xml.Element x = domHandler.getParsedElements().poll();

					// System.out.println("R: " + x.toString());

					final String type = x.getAttribute("type");

					Element response = new J2seElement(x);
					if (type != null && "terminate".equals(type)) {
						onTerminate(responseCode, response);
					} else if (type != null && "error".equals(type)) {
						onError(responseCode, response, null);
					} else if (type == null) {
						onSuccess(responseCode, response);
					} else
						throw new RuntimeException("Unknown response type '" + type + "'");

				}
			} catch (SocketException e) {
				if (terminated)
					return;
				onError(0, null, e);
			} catch (Exception e) {
				e.printStackTrace();
				onError(0, null, e);
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
