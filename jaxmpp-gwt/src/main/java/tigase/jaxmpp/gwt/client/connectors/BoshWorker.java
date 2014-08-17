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
package tigase.jaxmpp.gwt.client.connectors;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	private final BoshConnector connector;

	private Element element;

	private Logger log;

	private Request request;

	private final RequestBuilder requestBuilder;

	private final String rid;

	private boolean terminated = false;

	public BoshWorker(BoshConnector connector, RequestBuilder requestBuilder, SessionObject sessionObject, Element element)
			throws XMLException {
		this.connector = connector;
		// set current worker to this instance
		this.connector.setCurrentWorker(this);
		this.log = Logger.getLogger(this.getClass().getName());

		this.requestBuilder = requestBuilder;
		this.element = element;
		this.rid = element.getAttribute("rid");
		if (this.rid == null)
			throw new RuntimeException("rid must be defined");
		this.callback = new RequestCallback() {

			@Override
			public void onError(Request request, Throwable exception) {
				try {
					BoshWorker.this.onError(-1, null, null, exception);
				} catch (JaxmppException e) {
				}
			}

			@Override
			public void onResponseReceived(Request request, Response $response) {
				String t = $response == null ? null : $response.getText();
				if (log.isLoggable(Level.FINEST))
					log.finest("Received: " + t);

				try {
					int responseCode = $response.getStatusCode();
					GwtElement response;
					String x = t == null || t.length() == 0 ? null : t.replaceAll("&semi;", ";");
					// System.out.println("<< " + x);
					if (x == null)
						response = null;
					else
						try {
							response = new GwtElement(XMLParser.parse(x).getDocumentElement());
						} catch (Exception e) {
							response = null;
							if (responseCode == 200) {
								BoshWorker.this.onError($response.getStatusCode(), t, null, null);
								return;
							}
						}

					if (responseCode != 200) {
						BoshWorker.this.onError($response.getStatusCode(), t, response, null);
						return;
					}
					final String type = response == null ? null : response.getAttribute("type");
					final List<Element> streamError = response == null ? null : response.getChildren("stream:error");
					if (type != null && "terminate".equals(type)) {
						BoshWorker.this.onTerminate(responseCode, t, response);
					} else if (type != null && "error".equals(type)) {
						BoshWorker.this.onError(responseCode, t, response, null);
					} else if (streamError != null && !streamError.isEmpty()) {
						BoshWorker.this.onError(responseCode, t, response, null);
					} else if (type == null) {
						BoshWorker.this.onSuccess(responseCode, t, response);
					} else
						throw new RuntimeException("Unknown response type '" + type + "'");
				} catch (Exception e) {
					try {
						BoshWorker.this.onError(-1, t, null, e);
					} catch (JaxmppException e1) {
					}
				}
			}

		};
	}
	
	public void appendToBody(Element stanza) throws XMLException {
		this.element.addChild(stanza);
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
	public void execute() {
		// we are sending data so nothing more can be added to request after now
		this.connector.setCurrentWorker(null);

		if (terminated)
			return;
		try {
			String x = element.getAsString();
			// System.out.println(">> " + x);
			request = requestBuilder.sendRequest(x, callback);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				onError(-1, null, null, e);
			} catch (JaxmppException e1) {
			}
		}
	}

	@Override
	public String getRid() {
		return rid;
	}

	@Override
	public int hashCode() {
		return rid.hashCode();
	}

	protected abstract void onError(int responseCode, String data, Element response, Throwable caught) throws JaxmppException;

	protected abstract void onSuccess(int responseCode, String data, Element response) throws JaxmppException;

	protected abstract void onTerminate(int responseCode, String data, Element response) throws JaxmppException;

	@Override
	public void run() {
		execute();
	}

	@Override
	public void terminate() {
		this.terminated = true;
		if (request != null)
			request.cancel();
	}

	@Override
	public String toString() {
		if (rid != null)
			return "rid=" + rid;
		return super.toString();
	}
}