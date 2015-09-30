/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.jaxmpp.j2se.connectors.bosh;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	private Logger log;

	private final SimpleParser parser;

	private final String rid;

	private final SessionObject sessionObject;

	private boolean terminated = false;

	public BoshWorker(DomBuilderHandler domHandler, SimpleParser parser, SessionObject sessionObject, Element body)
			throws XMLException, JaxmppException {
		this.domHandler = domHandler;
		this.parser = parser;
		this.sessionObject = sessionObject;
		this.log = Logger.getLogger(this.getClass().getName());

		this.body = body;
		this.rid = body.getAttribute("rid");
		if (this.rid == null)
			throw new RuntimeException("rid must be defined");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof BoshWorker))
			return false;

		return ((BoshWorker) obj).rid.equals(rid);
	}

	@Override
	public String getRid() {
		return rid;
	}

	@Override
	public int hashCode() {
		return rid.hashCode();
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

				if (log.isLoggable(Level.FINEST))
					log.finest("Received: " + responseData);

				if (responseCode != 200) {
					onError(responseCode, responseData, null, null);
					return;
				}
				// wr.close();
				// rd.close();

				// System.out.println("R: " + sb.toString());

				synchronized (domHandler) {
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
						} else {
							throw new RuntimeException("Unknown response type '" + type + "'");
						}
					}
				}

			} catch (SocketException e) {
				if (terminated)
					return;
				onError(0, null, null, e);
			} catch (Exception e) {
				log.log(Level.WARNING, "Connection error ", e);
				onError(0, null, null, e);
			}
		} catch (JaxmppException e1) {
			log.log(Level.SEVERE, "What a Terrible Failure?", e1);
		}
	}

	@Override
	public void terminate() {
		terminated = true;
		if (conn != null)
			conn.disconnect();
	}

	@Override
	public String toString() {
		return "rid=" + rid;
	}

}