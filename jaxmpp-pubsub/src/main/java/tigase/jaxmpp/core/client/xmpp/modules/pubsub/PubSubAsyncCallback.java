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
package tigase.jaxmpp.core.client.xmpp.modules.pubsub;

import java.util.List;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public abstract class PubSubAsyncCallback implements AsyncCallback {

	protected Logger log = Logger.getLogger(this.getClass().getName());

	protected abstract void onEror(IQ response, ErrorCondition errorCondition, PubSubErrorCondition pubSubErrorCondition)
			throws JaxmppException;

	@Override
	public final void onError(Stanza responseStanza, ErrorCondition errorCondition) throws JaxmppException {
		List<Element> errors = responseStanza.getChildren("error");
		Element error = errors == null || errors.isEmpty() ? null : errors.get(0);
		PubSubErrorCondition pubSubErrorCondition = null;

		List<Element> perrors = error.getChildrenNS("http://jabber.org/protocol/pubsub#errors");
		Element perror = perrors == null || perrors.isEmpty() ? null : perrors.get(0);

		if (perror != null) {
			String c = perror.getName();
			String feature = perror.getAttribute("feature");

			if (feature != null)
				c = c + "_" + feature;

			try {
				pubSubErrorCondition = PubSubErrorCondition.valueOf(c.replace("-", "_"));
			} catch (Exception e) {
				log.warning("Unrecognized PubSubErrorCondition: " + c);
			}
		}

		onEror((IQ) responseStanza, errorCondition, pubSubErrorCondition);
	}

}