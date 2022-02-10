/*
 * WebDnsResolver.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
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

import com.google.gwt.http.client.URL;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.gwt.client.ConnectionManager;
import tigase.jaxmpp.gwt.client.connectors.WebSocket;

/**
 * @author andrzej
 */
public class WebDnsResolver
		implements ConnectionManager.Resolver {

	public static final String WEB_DNS_RESOLVER_URL_KEY = "WebDnsResolver#URL_KEY";

	private Context context;
	private DnsResult dnsResult;

	public void urlsForDomain(String domain, ConnectionManager.ResolutionCallback hostResolutionCallback) {

		if (dnsResult != null && dnsResult.getDomain().equals(domain) && dnsResult.hasMore()) {
			selectHost(domain, hostResolutionCallback);
			return;
		}

		resolveDomain(domain, new com.google.gwt.user.client.rpc.AsyncCallback<DnsResult>() {
			@Override
			public void onFailure(Throwable caught) {
				dnsResult = null;
				hostResolutionCallback.onUrlResolved(domain, null);
			}

			@Override
			public void onSuccess(DnsResult result) {
				dnsResult = result;

				selectHost(domain, hostResolutionCallback);
			}
		});
	}

	public String getResolverUrl() {
		return (String) context.getSessionObject().getProperty(WEB_DNS_RESOLVER_URL_KEY);
	}

	public void hostFailure(String url) {
		if (dnsResult != null) {
			dnsResult.connectionFailed(url);
		}
	}

	public boolean isEnabled() {
		return context.getSessionObject().getProperty(WEB_DNS_RESOLVER_URL_KEY) != null;
	}

	private void resolveDomain(String domain, com.google.gwt.user.client.rpc.AsyncCallback<DnsResult> callback) {
		String url = getResolverUrl();
		// adding parameters for resolver to process
		url += "?version=2";
		url += "&domain=" + URL.encodeQueryString(domain);
		JsonpRequestBuilder builder = new JsonpRequestBuilder();
		builder.requestObject(url, callback);
	}

	private void selectHost(final String domain,
							final ConnectionManager.ResolutionCallback hostResolutionCallback) {
		resolveDomain(domain, new com.google.gwt.user.client.rpc.AsyncCallback<DnsResult>() {

			@Override
			public void onFailure(Throwable caught) {
				hostResolutionCallback.onUrlFailed();
			}

			@Override
			public void onSuccess(DnsResult result) {
				String url = null;
				if (WebSocket.isSupported()) {
					int wsCount = result.getWebSocket().length();
					if (wsCount > 0) {
						url = result.getWebSocket().get(0).getUrl();
					}
				}
				if (url == null) {
					int boshCount = result.getBosh().length();
					if (boshCount > 0) {
						url = result.getBosh().get(0).getUrl();
					}
				}
				if (url != null) {
					hostResolutionCallback.onUrlResolved(domain, url);
				} else {
					hostResolutionCallback.onUrlFailed();
				}
			}

		});
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}
	
}
