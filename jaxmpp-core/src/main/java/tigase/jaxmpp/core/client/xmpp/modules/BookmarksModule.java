/*
 * BookmarksModule.java
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
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import java.util.List;

/**
 * Implementation of <a
 * href='http://xmpp.org/extensions/xep-0049.html'>XEP-0049: Private XML
 * Storage</a>.
 *
 * @author andrzej
 */
public class BookmarksModule
		extends AbstractIQModule {

	private static final String BOOKMARKS_XMLNS = "storage:bookmarks";
	private static final Criteria CRIT = ElementCriteria.name("storage", BOOKMARKS_XMLNS);
	private static final String[] FEATURES = {BOOKMARKS_XMLNS};

	public BookmarksModule() {
		super();
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
	}

	@Override
	protected void processGet(IQ element) throws JaxmppException {
		throw new XMPPException(XMPPException.ErrorCondition.not_allowed);
	}

	@Override
	protected void processSet(IQ element) throws JaxmppException {
		throw new XMPPException(XMPPException.ErrorCondition.not_allowed);
	}

	/**
	 * Send list of bookmarks to private storage.
	 *
	 * @param bookmarks collections of elements with bookmarks.
	 * @param callback callback
	 */
	public void publishBookmarks(List<? extends Element> bookmarks, AsyncCallback callback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);

		Element query = ElementFactory.create("query");
		query.setXMLNS("jabber:iq:private");
		iq.addChild(query);

		Element storage = ElementFactory.create("storage");
		storage.setXMLNS(BOOKMARKS_XMLNS);
		query.addChild(storage);

		if (bookmarks != null) {
			for (Element bookmark : bookmarks) {
				storage.addChild(bookmark);
			}
		}

		write(iq, callback);
	}

	/**
	 * Retrieve bookmarks from private storage.
	 *
	 * @param callback callback to handle response.
	 */
	public void retrieveBookmarks(AsyncCallback callback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.get);

		Element query = ElementFactory.create("query");
		query.setXMLNS("jabber:iq:private");
		iq.addChild(query);

		Element storage = ElementFactory.create("storage");
		storage.setXMLNS(BOOKMARKS_XMLNS);
		query.addChild(storage);

		write(iq, callback);
	}

	/**
	 * Bookmarks callback.
	 */
	public static abstract class BookmarksAsyncCallback
			implements AsyncCallback {

		public abstract void onBookmarksReceived(List<Element> bookmarks);

		@Override
		public void onSuccess(final Stanza stanza) throws XMLException {
			Element query = stanza.getChildrenNS("query", "jabber:iq:private");
			Element storage = query.getChildrenNS("storage", BOOKMARKS_XMLNS);
			onBookmarksReceived(storage.getChildren());
		}
	}

}