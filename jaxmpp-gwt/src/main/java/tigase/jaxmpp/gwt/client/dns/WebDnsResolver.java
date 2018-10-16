/*
 * WebDnsResolver.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
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
import com.google.gwt.user.client.Window;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.xmpp.modules.ContextAware;
import tigase.jaxmpp.gwt.client.connectors.WebSocket;

/**
 * @author andrzej
 */
public class WebDnsResolver
		implements ContextAware {

	public static final String WEB_DNS_RESOLVER_URL_KEY = "WebDnsResolver#URL_KEY";

	private Context context;
	private DnsResult dnsResult;

	public void getHostForDomain(final String domain, String hostname,
								 final DomainResolutionCallback hostResolutionCallback) {

		if (dnsResult != null && dnsResult.getDomain().equals(domain) && dnsResult.hasMore()) {
			selectHost(domain, hostname, hostResolutionCallback);
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

				selectHost(domain, null, hostResolutionCallback);
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

	private void selectHost(final String domain, final String hostname,
							final DomainResolutionCallback hostResolutionCallback) {
		String boshUrl = null;
		if (hostname != null) {
			boshUrl = dnsResult.getUrlForHost(hostname);
			if (boshUrl == null) {
				resolveDomain(hostname, new com.google.gwt.user.client.rpc.AsyncCallback<DnsResult>() {

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
				return;
			}
		}
		boolean secure = "https:".equals(Window.Location.getProtocol());
		while (boshUrl == null && dnsResult.hasMore()) {
			boshUrl = dnsResult.next();

			boolean secureUrl = boshUrl.startsWith("https://") || boshUrl.startsWith("wss://");
//			if (secure != secureUrl) {
//				dnsResult.connectionFailed(boshUrl);
//				boshUrl = null;
//				continue;
//			}

			if (!WebSocket.isSupported() && (boshUrl.startsWith("ws://") || boshUrl.startsWith("wss://"))) {
				dnsResult.connectionFailed(boshUrl);
				boshUrl = null;
			}
		}

		if (boshUrl != null) {
			hostResolutionCallback.onUrlResolved(domain, boshUrl);
		}
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}

	public interface DomainResolutionCallback {

		void onUrlFailed();

		void onUrlResolved(String domain, String url);

	}
}
