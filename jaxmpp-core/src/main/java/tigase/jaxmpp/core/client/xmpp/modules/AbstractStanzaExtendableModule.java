/*
 * AbstractStanzaExtendableModule.java
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
package tigase.jaxmpp.core.client.xmpp.modules;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.extensions.ExtendableModule;
import tigase.jaxmpp.core.client.xmpp.modules.extensions.Extension;
import tigase.jaxmpp.core.client.xmpp.modules.extensions.ExtensionsChain;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class AbstractStanzaExtendableModule<T extends Stanza>
		extends AbstractStanzaModule<T>
		implements ExtendableModule {

	private final ExtensionsChain extensionsChain = new ExtensionsChain();

	@Override
	public void addExtension(Extension e) {
		extensionsChain.addExtension(e);
		if (e instanceof ContextAware) {
			((ContextAware) e).setContext(context);
		}
	}

	@Override
	public ExtensionsChain getExtensionChain() {
		return extensionsChain;
	}

	public String[] getFeaturesWithExtensions(String[] superFeatures) {
		ArrayList<String> features = new ArrayList<String>();
		if (superFeatures != null) {
			features.addAll(Arrays.asList(superFeatures));
		}
		for (Extension e : extensionsChain.getExtension()) {
			String[] f = e.getFeatures();
			if (f != null && f.length > 0) {
				features.addAll(Arrays.asList(f));
			}
		}
		return features.toArray(new String[features.size()]);
	}

	@Override
	public void removeExtension(Extension e) {
		extensionsChain.removeExtension(e);
	}

	@Override
	protected Element write(Element stanza) throws JaxmppException {
		Element s = extensionsChain.executeBeforeSendChain(stanza);
		if (s != null) {
			context.getWriter().write(s);
		}
		return s;
	}

	@Override
	protected Element write(Element stanza, AsyncCallback asyncCallback) throws JaxmppException {
		Element s = extensionsChain.executeBeforeSendChain(stanza);
		if (s != null) {
			context.getWriter().write(s, asyncCallback);
		}
		return s;
	}

	@Override
	protected Element write(Element stanza, Long timeout, AsyncCallback asyncCallback) throws JaxmppException {
		Element s = extensionsChain.executeBeforeSendChain(stanza);
		if (s != null) {
			context.getWriter().write(s, timeout, asyncCallback);
		}
		return s;
	}

}