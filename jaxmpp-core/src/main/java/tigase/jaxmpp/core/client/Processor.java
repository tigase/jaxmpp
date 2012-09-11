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
package tigase.jaxmpp.core.client;

import java.util.List;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public class Processor {

	public static class FeatureNotImplementedResponse extends AbstractStanzaHandler {

		protected final Logger log = Logger.getLogger(this.getClass().getName());

		public FeatureNotImplementedResponse(Stanza stanza, PacketWriter writer, SessionObject sessionObject) {
			super(stanza, writer, sessionObject);
		}

		@Override
		protected void process() throws XMLException, XMPPException {
			log.fine(ErrorCondition.feature_not_implemented.name() + " " + stanza.getAsString());
			throw new XMPPException(ErrorCondition.feature_not_implemented);
		}

	}

	public static Element createError(final Element stanza, Throwable caught) {
		try {
			DefaultElement result = new DefaultElement(stanza.getName(), null, null);
			result.setAttribute("type", "error");
			result.setAttribute("to", stanza.getAttribute("from"));
			result.setAttribute("id", stanza.getAttribute("id"));

			DefaultElement error = new DefaultElement("error", null, null);
			if (caught instanceof XMPPException) {
				if (((XMPPException) caught).getCondition().getType() != null)
					error.setAttribute("type", ((XMPPException) caught).getCondition().getType());
				if (((XMPPException) caught).getCondition().getErrorCode() != 0)
					error.setAttribute("code", "" + ((XMPPException) caught).getCondition().getErrorCode());
				DefaultElement ed = new DefaultElement(((XMPPException) caught).getCondition().getElementName(), null, null);
				ed.setAttribute("xmlns", XMPPException.getXmlns());
				error.addChild(ed);
			} else {
				return null;
			}

			if (caught.getMessage() != null) {
				DefaultElement text = new DefaultElement("text", caught.getMessage(), null);
				text.setAttribute("xmlns", "urn:ietf:params:xml:ns:xmpp-stanzas");
				error.addChild(text);
			}
			result.addChild(error);

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private final SessionObject sessionObject;

	private final PacketWriter writer;

	private final XmppModulesManager xmppModulesManages;

	public Processor(XmppModulesManager xmppModulesManages, final SessionObject sessionObject, final PacketWriter writer) {
		this.sessionObject = sessionObject;
		this.writer = writer;
		this.xmppModulesManages = xmppModulesManages;
	}

	public XmppModulesManager getXmppModulesManages() {
		return xmppModulesManages;
	}

	public Runnable process(final Element stanza) {
		try {
			Runnable result = sessionObject.getResponseHandler(stanza, writer);
			if (result != null)
				return result;

			if (stanza.getName().equals("iq") && stanza.getAttribute("type") != null
					&& (stanza.getAttribute("type").equals("error") || stanza.getAttribute("type").equals("result")))
				return null;

			final List<XmppModule> modules = xmppModulesManages.findModules(stanza);
			if (modules == null)
				result = new FeatureNotImplementedResponse(Stanza.create(stanza), writer, sessionObject);
			else {
				result = new AbstractStanzaHandler(Stanza.create(stanza), writer, sessionObject) {

					@Override
					protected void process() throws XMLException, XMPPException, JaxmppException {
						for (XmppModule module : modules) {
							module.process(this.stanza);
						}
					}
				};
			}
			return result;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}