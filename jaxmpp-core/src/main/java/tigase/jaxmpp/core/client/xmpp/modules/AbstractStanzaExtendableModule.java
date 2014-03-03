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
package tigase.jaxmpp.core.client.xmpp.modules;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.extensions.ExtendableModule;
import tigase.jaxmpp.core.client.xmpp.modules.extensions.Extension;
import tigase.jaxmpp.core.client.xmpp.modules.extensions.ExtensionsChain;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public abstract class AbstractStanzaExtendableModule<T extends Stanza> extends AbstractStanzaModule<T> implements
		ExtendableModule {

	private final ExtensionsChain extensionsChain = new ExtensionsChain();

	@Override
	public void addExtension(Extension e) {
		extensionsChain.addExtension(e);
	}

	@Override
	public ExtensionsChain getExtensionChain() {
		return extensionsChain;
	}

	@Override
	public void removeExtension(Extension e) {
		extensionsChain.removeExtension(e);
	}

	@Override
	protected void write(Element stanza) throws JaxmppException {
		Element s = extensionsChain.executeBeforeSendChain(stanza);
		if (s != null)
			context.getWriter().write(s);
	}

	@Override
	protected void write(Element stanza, AsyncCallback asyncCallback) throws JaxmppException {
		Element s = extensionsChain.executeBeforeSendChain(stanza);
		if (s != null)
			context.getWriter().write(s, asyncCallback);
	}

	@Override
	protected void write(Element stanza, Long timeout, AsyncCallback asyncCallback) throws JaxmppException {
		Element s = extensionsChain.executeBeforeSendChain(stanza);
		if (s != null)
			context.getWriter().write(s, timeout, asyncCallback);
	}

}