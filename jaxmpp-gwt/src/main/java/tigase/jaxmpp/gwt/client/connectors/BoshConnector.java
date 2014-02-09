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

import java.util.logging.Level;

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import java.util.List;
import tigase.jaxmpp.core.client.connector.BoshRequest;
import tigase.jaxmpp.core.client.xmpp.utils.MutableBoolean;

public class BoshConnector extends AbstractBoshConnector {

	/** 
	 * property used to disable usage of <bosh/> host attribute to redirect 
	 * next requests to other hostname 
	 */
	public static final String BOSH_IGNORE_SERVER_HOST = "BOSH#IGNORE_SERVER_HOST";
			
	private static final String HOST_ATTR = "host";
	
	private static RegExp URL_PARSER = RegExp.compile("^([a-z]+)://([^:/]+)(:[0-9]+)*([^#]+)$");

	private String host = null;
	private RequestBuilder requestBuilder;

    private BoshWorker currentWorker = null;
        
	public BoshConnector(Context context) {
		super(context);

		String u = context.getSessionObject().getProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY);

		Boolean ignoreServerHost = context.getSessionObject().getProperty(BOSH_IGNORE_SERVER_HOST);
		if (ignoreServerHost == null || !ignoreServerHost) {
			// if we support change of destination by host attribute sent by server
			// then parse url to get current hostname
			MatchResult result = URL_PARSER.exec(u);
			host = result.getGroup(2);
		}
		requestBuilder = new RequestBuilder(RequestBuilder.POST, u);
		// in Chrome following line causes error (Connection: close is not
		// allowed in new spec)
		// requestBuilder.setHeader("Connection", "close");
	}

	@Override
	protected Element prepareBody(Element payload) throws XMLException {
		// trying to reuse BoshWorker if data is not sent yet
		if (currentWorker != null) {
			currentWorker.appendToBody(payload);
			return null;
		}

		return super.prepareBody(payload);
	}

	@Override
	protected void onResponse(BoshRequest request, int responseCode, String responseData, Element response) throws JaxmppException {
		if (response != null && this.host != null) {
			String host = response.getAttribute(HOST_ATTR);
			if (host != null && !this.host.equals(host)) {
				// if host of current request does not match host sent as attribute
				// from server, then we need to recreate request builder to use
				// new host for next request
				this.host = host;
				
				MatchResult result = URL_PARSER.exec(requestBuilder.getUrl());
				String url = result.getGroup(1) + "://" + host 
						+ (result.getGroup(3) != null ? result.getGroup(3) : "") 
						+ result.getGroup(4);
				requestBuilder = new RequestBuilder(RequestBuilder.POST, url.toString());
			}
		}
		super.onResponse(request, responseCode, responseData, response);
	}
	
	@Override
	protected void processSendData(Element element) throws XMLException, JaxmppException {
		if (element == null) {
			return;
		}

		BoshWorker worker = new BoshWorker(this, requestBuilder, context.getSessionObject(), element) {

			@Override
			protected void onError(int responseCode, String responseData, Element response, Throwable caught)
					throws JaxmppException {
				BoshConnector.this.onError(this, responseCode, responseData, response, caught);
			}

			@Override
			protected void onSuccess(int responseCode, String responseData, Element response) throws JaxmppException {
				BoshConnector.this.onResponse(this, responseCode, responseData, response);
			}

			@Override
			protected void onTerminate(int responseCode, String responseData, Element response) throws JaxmppException {
				BoshConnector.this.onTerminate(this, responseCode, responseData, response);
			}

		};

		addToRequests(worker);

		BoshPacketSendingHandler.BoshPacketSendingEvent event = new BoshPacketSendingHandler.BoshPacketSendingEvent(
				context.getSessionObject(), element);
		context.getEventBus().fire(event, this);

		if (log.isLoggable(Level.FINEST))
			log.finest("Send: " + element.getAsString());

		Scheduler.get().scheduleDeferred(worker);
	}

	/**
	 * Keep handle to current BoshWorker instance until stanza is sent
	 * 
	 * @param worker
	 */
	protected void setCurrentWorker(BoshWorker worker) {
		this.currentWorker = worker;
	}

	@Override
	protected void onError(BoshRequest request, int responseCode, String responseData, Element response, Throwable caught) throws JaxmppException {
		if (response != null) {
			if (handleSeeOtherHost(response)) 
				return;
		}
		super.onError(request, responseCode, responseData, response, caught);
	}
	
	@Override
	protected void onTerminate(BoshRequest request, int responseCode, String responseData, Element response) throws JaxmppException {
		if (handleSeeOtherHost(response)) 
			return;
		super.onTerminate(request, responseCode, responseData, response); 
	}
	
	protected boolean handleSeeOtherHost(Element response) throws JaxmppException {
		if (response == null) 
			return false;
		
		List<Element> streamErrors = response.getChildren("stream:error");
		if (streamErrors == null || streamErrors.isEmpty())
			return false;
		for (Element streamError : streamErrors) {
			Element seeOtherHost = streamError.getChildrenNS("see-other-host", "urn:ietf:params:xml:ns:xmpp-streams");
			if (seeOtherHost != null) {
				String seeHost = seeOtherHost.getValue();
				if (log.isLoggable(Level.FINE)) {
					log.fine("Received see-other-host=" + seeHost);
				}
				MutableBoolean handled = new MutableBoolean();
				context.getEventBus().fire(
						new SeeOtherHostHandler.SeeOtherHostEvent(context.getSessionObject(), seeHost, handled));

				return false;//handled.isValue();
			}
		}
		return false;
	}
}