/*
 * WellKnownHostMetaResolver.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2022 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
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
package tigase.jaxmpp.gwt.client.dns;

import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.gwt.client.ConnectionManager;
import tigase.jaxmpp.gwt.client.connectors.WebSocket;
import tigase.jaxmpp.gwt.client.xml.GwtElement;

import java.util.ArrayList;
import java.util.List;

public class WellKnownHostMetaResolver implements ConnectionManager.Resolver {

	public static final String ENABLED = "WellKnownHostMetaResolver#ENABLED";
	public static final String USE_HTTPS = "WellKnownHostMetaResolver#USE_HTTPS";
	private static final String BOSH_METHOD = "urn:xmpp:alt-connections:xbosh";
	private static final String WS_METHOD = "urn:xmpp:alt-connections:websocket";
	
	private String domain;
	private List<String> results = new ArrayList<>();

	private Context context;

	@Override
	public void setContext(Context context) {
		this.context = context;
	}
	
	public void setUseHttps(boolean value) {
		context.getSessionObject().setUserProperty(USE_HTTPS, value);
	}

	@Override
	public boolean isEnabled() {
		return !Boolean.FALSE.equals(context.getSessionObject().getProperty(ENABLED));
	}

	public boolean useHttps() {
		return !Boolean.FALSE.equals(context.getSessionObject().getUserProperty(USE_HTTPS));
	}

	public void urlsForDomain(String domain, ConnectionManager.ResolutionCallback callback) {
		if (domain.equals(this.domain) && !results.isEmpty()) {
			selectUrl(domain, callback);
			return;
		}
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
													(useHttps() ? "https" : "http") + "://" + domain +
															"/.well-known/host-meta");
		try {
			builder.sendRequest(null, new RequestCallback() {
				@Override
				public void onResponseReceived(Request request, Response response) {
					if (response.getStatusCode() == 200) {
						try {
							Element root = GwtElement.parse(response.getText());
							List<Element> children = root.getChildren("Link");
							List<String> urls = new ArrayList<>();
							if (children != null) {
								boolean isSecure = "https:".equals(Window.Location.getProtocol());
								if (WebSocket.isSupported()) {
									for (Element linkEl : children) {
										if (WS_METHOD.equals(linkEl.getAttribute("rel"))) {
											String href = linkEl.getAttribute("href");
											if (href != null) {
												if (isSecure && !href.startsWith("wss://")) {
													continue;
												}
												urls.add(href);
											}
										}
									}
								}
								for (Element linkEl : children) {
									if (BOSH_METHOD.equals(linkEl.getAttribute("rel"))) {
										String href = linkEl.getAttribute("href");
										if (href != null) {
											if (isSecure && !href.startsWith("https://")) {
												continue;
											}
											urls.add(href);
										}
									}
								}
							}
							WellKnownHostMetaResolver.this.results = urls;
							selectUrl(domain, callback);
						} catch (XMLException ex) {
							callback.onUrlFailed();
						}
					} else {
						callback.onUrlResolved(domain, null);
					}
				}

				@Override
				public void onError(Request request, Throwable throwable) {
					callback.onUrlResolved(domain, null);
				}
			});
		} catch (RequestException ex) {
			callback.onUrlFailed();
		}
	}

	protected void selectUrl(String domain, ConnectionManager.ResolutionCallback callback) {
		if (results.isEmpty()) {
			callback.onUrlFailed();
			return;
		}
		String url = results.get(0);
		callback.onUrlResolved(domain, url);
	}

	public void hostFailure(String url) {
		results.remove(url);
	}

}
