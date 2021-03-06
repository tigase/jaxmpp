/*
 * ClientStateIndicationModule.java
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

import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Created by andrzej on 27.06.2016.
 */
public class ClientStateIndicationModule
		implements XmppModule, ContextAware {

	private static final String CSI_XMLNS = "urn:xmpp:csi:0";

	private Context context;

	public void active() {
		setState(true);
	}

	@Override
	public Criteria getCriteria() {
		return null;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	public void inactive() {
		setState(false);
	}

	public boolean isAvailable() throws XMLException {
		Element streamFeaturesElem = StreamFeaturesModule.getStreamFeatures(context.getSessionObject());
		return streamFeaturesElem != null && streamFeaturesElem.getChildrenNS("csi", "urn:xmpp:csi:0") != null;
	}

	@Override
	public void process(Element element) throws XMPPException, XMLException, JaxmppException {
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}

	public boolean setState(boolean active) {
		try {
			if (isAvailable()) {
				context.getWriter().write(ElementFactory.create(active ? "active" : "inactive", null, CSI_XMLNS));
				return true;
			}
		} catch (JaxmppException ex) {
		}
		return false;
	}
}
