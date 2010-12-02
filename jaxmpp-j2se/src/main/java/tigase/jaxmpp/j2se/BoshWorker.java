package tigase.jaxmpp.j2se;

//~--- non-JDK imports --------------------------------------------------------

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.j2se.BoshConnector.ConnectorData;
import tigase.jaxmpp.j2se.xml.J2seElement;

import tigase.xml.DomBuilderHandler;
import tigase.xml.SimpleParser;
import tigase.xml.SingletonFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;
import java.net.SocketException;

//~--- classes ----------------------------------------------------------------

/**
 * Class description
 *
 *
 * @version        5.1.0, 2010.12.02 at 01:25:43 GMT
 * @author         Artur Hefczyc <artur.hefczyc@tigase.org>
 */
public abstract class BoshWorker implements Runnable {
	private static final SimpleParser parser = SingletonFactory.getParserInstance();

	//~--- fields ---------------------------------------------------------------

	private final DomBuilderHandler domHandler = new DomBuilderHandler();
	private boolean terminated = false;
	private final Element body;
	private HttpURLConnection conn;
	private final ConnectorData data;
	private final String rid;

	//~--- constructors ---------------------------------------------------------

	/**
	 * Constructs ...
	 *
	 *
	 * @param connectorData
	 * @param body
	 *
	 * @throws XMLException
	 */
	public BoshWorker(ConnectorData connectorData, Element body) throws XMLException {
		this.data = connectorData;
		this.body = body;
		this.rid = body.getAttribute("rid");

		if (this.rid == null) {
			throw new RuntimeException("rid must be defined");
		}
	}

	//~--- methods --------------------------------------------------------------

	protected abstract void onError(int responseCode, Element response, Throwable caught);

	protected abstract void onSuccess(int responseCode, Element response);

	protected abstract void onTerminate(int responseCode, Element response);

	//~--- get methods ----------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	public String getRid() {
		return rid;
	}

	//~--- methods --------------------------------------------------------------

	/**
	 * Method description
	 *
	 */
	@Override
	public void run() {
		if (terminated) {
			return;
		}

		try {
			String b = body.getAsString();

			System.out.println("S: " + b);
			this.conn = (HttpURLConnection) data.url.openConnection();
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
				System.out.println("R: " + sb.toString());
				parser.parse(domHandler, sb.toString().toCharArray(), 0, sb.length());

				tigase.xml.Element x = domHandler.getParsedElements().poll();

				System.out.println("R: " + x.toString());

				final String type = x.getAttribute("type");
				Element response = new J2seElement(x);

				if ((type != null) && "terminate".equals(type)) {
					onTerminate(responseCode, response);
				} else {
					if ((type != null) && "error".equals(type)) {
						onError(responseCode, response, null);
					} else {
						if (type == null) {
							onSuccess(responseCode, response);
						} else {
							throw new RuntimeException("Unknown response type '" + type + "'");
						}
					}
				}
			}
		} catch (SocketException e) {
			if (terminated) {
				return;
			}

			onError(0, null, e);
		} catch (Exception e) {
			e.printStackTrace();
			onError(0, null, e);
		}
	}

	/**
	 * Method description
	 *
	 */
	public void terminate() {
		terminated = true;

		if (conn != null) {
			conn.disconnect();
		}
	}
}


//~ Formatted in Sun Code Convention


//~ Formatted by Jindent --- http://www.jindent.com
