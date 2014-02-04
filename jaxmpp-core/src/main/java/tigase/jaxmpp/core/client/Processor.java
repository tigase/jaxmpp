/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc.
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
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

/**
 * Class for process incoming stanza. This clas produces {@linkplain Runnable}
 * that can be started by {@linkplain Executor}.
 * 
 */
public class Processor {

	public static class FeatureNotImplementedResponse extends AbstractStanzaHandler {

		protected final Logger log = Logger.getLogger(this.getClass().getName());

		public FeatureNotImplementedResponse(Element element, Context context) {
			super(element, context);
		}

		@Override
		protected void process() throws XMLException, XMPPException {
			log.fine(ErrorCondition.feature_not_implemented.name() + " " + element.getAsString());
			throw new XMPPException(ErrorCondition.feature_not_implemented);
		}

	}

	public static Element createError(final Element stanza, Throwable caught) {
		try {
			Element result = ElementFactory.create(stanza.getName(), null, null);
			result.setAttribute("type", "error");
			result.setAttribute("to", stanza.getAttribute("from"));
			result.setAttribute("id", stanza.getAttribute("id"));

			Element error = ElementFactory.create("error", null, null);
			if (caught instanceof XMPPException) {
				if (((XMPPException) caught).getCondition().getType() != null)
					error.setAttribute("type", ((XMPPException) caught).getCondition().getType());
				if (((XMPPException) caught).getCondition().getErrorCode() != 0)
					error.setAttribute("code", "" + ((XMPPException) caught).getCondition().getErrorCode());
				Element ed = ElementFactory.create(((XMPPException) caught).getCondition().getElementName(), null, null);
				ed.setAttribute("xmlns", XMPPException.getXmlns());
				error.addChild(ed);
			} else {
				return null;
			}

			if (caught.getMessage() != null) {
				Element text = ElementFactory.create("text", caught.getMessage(), null);
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

	private final Context context;

	private final XmppModulesManager xmppModulesManages;

	public Processor(XmppModulesManager xmppModulesManages, Context context) {
		this.context = context;
		this.xmppModulesManages = xmppModulesManages;
	}

	public XmppModulesManager getXmppModulesManages() {
		return xmppModulesManages;
	}

	/**
	 * Produces {@link Runnable} that must be run to fully process received
	 * stanza.
	 * 
	 * @param element
	 *            received stanza
	 * @return {@linkplain Runnable}
	 */
	public Runnable process(final Element receivedElement) {
		try {
			final Element element = Stanza.canBeConverted(receivedElement) ? Stanza.create(receivedElement) : receivedElement;
			Runnable result = ResponseManager.getResponseHandler(context, element);
			if (result != null)
				return result;

			if (element.getName().equals("iq") && element.getAttribute("type") != null
					&& (element.getAttribute("type").equals("error") || element.getAttribute("type").equals("result")))
				return null;

			final List<XmppModule> modules = xmppModulesManages.findModules(element);

			if (modules == null)
				result = new FeatureNotImplementedResponse(element, context);
			else {
				result = new AbstractStanzaHandler(element, context) {

					@Override
					protected void process() throws XMLException, XMPPException, JaxmppException {
						for (XmppModule module : modules) {
							module.process(this.element);
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