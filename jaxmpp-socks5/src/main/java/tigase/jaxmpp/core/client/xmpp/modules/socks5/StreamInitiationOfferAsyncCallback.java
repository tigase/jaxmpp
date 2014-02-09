/*
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2013 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
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
package tigase.jaxmpp.core.client.xmpp.modules.socks5;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public abstract class StreamInitiationOfferAsyncCallback implements AsyncCallback {

	private String sid = null;

	public StreamInitiationOfferAsyncCallback(String sid) {
		this.sid = sid;
	}
	
	public abstract void onAccept(String sid);

	public abstract void onError();

	@Override
	public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
		if (error == ErrorCondition.forbidden) {
			onReject();
		} else {
			onError();
		}
	}

	public abstract void onReject();

	@Override
	public void onSuccess(Stanza stanza) throws JaxmppException {
		boolean ok = false;
		String sid = null;

		Element si = stanza.getChildrenNS("si", "http://jabber.org/protocol/si");
		if (si != null) {
			sid = si.getAttribute("id");
			Element feature = si.getChildrenNS("feature", "http://jabber.org/protocol/feature-neg");
			if (feature != null) {
				Element x = feature.getChildrenNS("x", "jabber:x:data");
				if (x != null) {
					Element field = x.getFirstChild();
					if (field != null) {
						Element value = field.getFirstChild();
						if (value != null) {
							ok = Socks5BytestreamsModule.XMLNS_BS.equals(value.getValue());
						}
					}
				}
			}
		}

		if (sid == null) {
			sid = this.sid;
		}

		if (ok) {
			onAccept(sid);
		} else {
			onError();
		}
	}

	@Override
	public void onTimeout() {
		onError();
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

}