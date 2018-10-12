/*
 * HttpFileUploadModule.java
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
package tigase.jaxmpp.core.client.xmpp.modules.httpfileupload;

import tigase.jaxmpp.core.client.*;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.forms.JabberDataElement;
import tigase.jaxmpp.core.client.xmpp.forms.TextSingleField;
import tigase.jaxmpp.core.client.xmpp.forms.XDataType;
import tigase.jaxmpp.core.client.xmpp.modules.ContextAware;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpFileUploadModule
		implements XmppModule, ContextAware {

	private static final Logger log = Logger.getLogger(HttpFileUploadModule.class.getCanonicalName());
	private static final String[] DISCO_FORM_PATH = {"iq", "query", "x"};
	private static final String[] IQ_SLOT_PATH = {"iq", "slot"};
	private static final String XMLNS = "urn:xmpp:http:upload:0";

	private Context context;

	public static void addFileInfoFormToStanza(Stanza stanza, String fileName, long size, String type)
			throws XMLException {
		JabberDataElement data = new JabberDataElement(XDataType.result);
		data.addFORM_TYPE("http://tigase.org/protocol#fileinfo");
		data.setInstructions("Information about a file");
		data.addTextSingleField("filename", fileName);
		data.addTextSingleField("size", String.valueOf(size));
		if (type != null) {
			data.addTextSingleField("type", type);
		}
		stanza.addChild(data);
	}

	public static void addOobLink(Stanza stanza, String url) throws XMLException {
		Element x = ElementFactory.create("x", null, "jabber:x:oob");
		x.addChild(ElementFactory.create("url", url, null));
		stanza.addChild(x);
	}

	public void findHttpUploadComponents(BareJID domain, final DiscoveryResultHandler handler) throws JaxmppException {
		context.getModuleProvider()
				.getModule(DiscoveryModule.class)
				.getItems(JID.jidInstance(domain), new DiscoveryModule.DiscoItemsAsyncCallback() {
					@Override
					public void onError(Stanza responseStanza, XMPPException.ErrorCondition error)
							throws JaxmppException {
						handler.onResult(Collections.<JID, Long>emptyMap());
					}

					@Override
					public void onInfoReceived(String attribute, ArrayList<DiscoveryModule.Item> items)
							throws XMLException {
						final DiscoveryResultCollector collector = new DiscoveryResultCollector(items.size(), handler);
						for (DiscoveryModule.Item item : items) {
							final JID jid = item.getJid();
							try {
								context.getModuleProvider()
										.getModule(DiscoveryModule.class)
										.getInfo(item.getJid(),
												 new DiscoveryModule.DiscoInfoAsyncCallback(item.getNode()) {
													 @Override
													 public void onError(Stanza responseStanza,
																		 XMPPException.ErrorCondition error)
															 throws JaxmppException {
														 collector.error();
													 }

													 @Override
													 protected void onInfoReceived(String node,
																				   Collection<DiscoveryModule.Identity> identities,
																				   Collection<String> features)
															 throws XMLException {
														 if (!features.contains(XMLNS)) {
															 collector.onResponse();
															 return;
														 }

														 long maxFileSize = Long.MAX_VALUE;
														 Element x = this.responseStanza.findChild(DISCO_FORM_PATH);
														 if (x != null && "jabber:x:data".equals(x.getXMLNS())) {
															 try {
																 TextSingleField field = ((TextSingleField) new JabberDataElement(
																		 x).getField("max-file-size"));
																 if (field != null && field.getFieldValue() != null) {
																	 maxFileSize = Long.parseLong(
																			 field.getFieldValue());
																 }
															 } catch (JaxmppException ex) {
																 log.log(Level.FINE, "Form retrieval failed", ex);
															 }
														 }
														 collector.found(jid, maxFileSize);
													 }

													 @Override
													 public void onTimeout() throws JaxmppException {
														 collector.error();
													 }
												 });
							} catch (JaxmppException ex) {
								log.log(Level.FINE, "Service discovery failed", ex);
								collector.error();
							}
						}
					}

					@Override
					public void onTimeout() throws JaxmppException {
						handler.onResult(Collections.<JID, Long>emptyMap());
					}
				});
	}

	@Override
	public Criteria getCriteria() {
		return null;
	}

	@Override
	public String[] getFeatures() {
		return new String[0];
	}

	@Override
	public void process(Element element) throws XMPPException, XMLException, JaxmppException {

	}

	public void requestUploadSlot(JID componentJid, String filename, Long size, String contentType,
								  RequestUploadSlotHandler callback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(componentJid);
		iq.setType(StanzaType.get);
		Element request = ElementFactory.create("request", null, XMLNS);
		request.setAttribute("filename", filename);
		request.setAttribute("size", String.valueOf(size));
		if (contentType != null) {
			request.setAttribute("content-type", contentType);
		}
		iq.addChild(request);

		context.getWriter().write(iq, callback);
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}

	public interface DiscoveryResultHandler {

		void onResult(Map<JID, Long> results);

	}

	private static final class DiscoveryResultCollector {

		private final DiscoveryResultHandler callback;
		private final Map<JID, Long> results = new HashMap<>();
		private int counter;

		public DiscoveryResultCollector(int count, DiscoveryResultHandler callback) {
			this.counter = count;
			this.callback = callback;
		}

		public synchronized void error() {
			onResponse();
		}

		public synchronized void found(JID jid, long maxFileSize) {
			results.put(jid, maxFileSize);
			onResponse();
		}

		public synchronized void onResponse() {
			counter--;
			if (counter <= 0) {
				this.callback.onResult(results);
			}
		}
	}

	public static abstract class RequestUploadSlotHandler
			implements AsyncCallback {

		@Override
		public void onSuccess(Stanza responseStanza) throws JaxmppException {
			Element slot = responseStanza.findChild(IQ_SLOT_PATH);
			if (slot != null || XMLNS.equals(slot.getXMLNS())) {
				Element putEl = slot.getFirstChild("put");
				Element getEl = slot.getFirstChild("get");
				if (putEl != null && getEl != null) {
					String get = getEl.getAttribute("url");
					String put = putEl.getAttribute("url");
					if (get != null && put != null) {
						Map<String, String> putHeaders = new HashMap<>();
						for (Element headerEl : putEl.getChildren("header")) {
							String name = headerEl.getAttribute("name");
							if (name != null) {
								putHeaders.put(name, headerEl.getValue());
							}
						}
						onSuccess(new Slot(put, putHeaders, get));
						return;
					}
				}
			}

			onError(responseStanza, XMPPException.ErrorCondition.undefined_condition);
		}

		public abstract void onSuccess(Slot slot) throws JaxmppException;
	}

	public static class Slot {

		private final String getUri;
		private final Map<String, String> putHeaders;
		private final String putUri;

		public Slot(String putUri, Map<String, String> putHeaders, String getUri) {
			this.putUri = putUri;
			this.putHeaders = putHeaders;
			this.getUri = getUri;
		}

		public String getGetUri() {
			return getUri;
		}

		public Map<String, String> getPutHeaders() {
			return putHeaders;
		}

		public String getPutUri() {
			return putUri;
		}
	}
}
