/*
 * MessageArchivingModule.java
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
package tigase.jaxmpp.core.client.xmpp.modules.xep0136;

import tigase.jaxmpp.core.client.*;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.PacketWriterAware;
import tigase.jaxmpp.core.client.xmpp.modules.xep0136.ChatItem.Type;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MessageArchivingModule class implements support for XEP-0136 Message
 * Archiving.
 */
public class MessageArchivingModule
		implements XmppModule, PacketWriterAware {

	private static final String ARCHIVE_XMLNS = "urn:xmpp:archive";
	private static final Criteria CRIT = ElementCriteria.name("iq").add(ElementCriteria.xmlns(ARCHIVE_XMLNS));
	private static final DateTimeFormat format = new DateTimeFormat();
	private PacketWriter writer = null;

	public MessageArchivingModule() {
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	public void getSettings(final SettingsAsyncCallback callback) throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.get);

		Element prefEl = ElementFactory.create("pref", null, ARCHIVE_XMLNS);
		iq.addChild(prefEl);

		writer.write(iq, null, callback);
	}

	@Deprecated
	public void listCollections(final JID withJid, final Date startTime, final Date endTime, final String afterId,
								final CollectionAsyncCallback callback) throws XMLException, JaxmppException {
		tigase.jaxmpp.core.client.xmpp.modules.xep0136.Criteria crit = new tigase.jaxmpp.core.client.xmpp.modules.xep0136.Criteria()
				.setWith(withJid)
				.setStart(startTime)
				.setEnd(endTime)
				.setAfter(afterId);
		listCollections(crit, callback);
	}

	public void listCollections(final tigase.jaxmpp.core.client.xmpp.modules.xep0136.Criteria crit,
								final CollectionAsyncCallback callback) throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.get);

		Element retrieve = ElementFactory.create("list", null, ARCHIVE_XMLNS);
		iq.addChild(retrieve);
		crit.toElement(retrieve);

		writer.write(iq, null, callback);
	}

	@Override
	public void process(Element element) throws XMPPException, XMLException, JaxmppException {
		// nothing to do
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void retrieveCollection(final tigase.jaxmpp.core.client.xmpp.modules.xep0136.Criteria crit,
								   final ItemsAsyncCallback callback) throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.get);

		Element retrieve = ElementFactory.create("retrieve", null, ARCHIVE_XMLNS);
		iq.addChild(retrieve);
		crit.toElement(retrieve);

		writer.write(iq, null, callback);
	}

	@Deprecated
	public void retriveCollection(final JID withJid, final Date startTime, final Date endTime, String afterId,
								  Integer index, Integer maxCount, final ItemsAsyncCallback callback)
			throws XMLException, JaxmppException {
		tigase.jaxmpp.core.client.xmpp.modules.xep0136.Criteria crit = new tigase.jaxmpp.core.client.xmpp.modules.xep0136.Criteria()
				.setWith(withJid)
				.setStart(startTime)
				.setEnd(endTime)
				.setAfter(afterId);
		retrieveCollection(crit, callback);
	}

	public void setAutoArchive(boolean enable, final AsyncCallback callback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.get);

		Element autoEl = ElementFactory.create("auto", null, ARCHIVE_XMLNS);
		autoEl.setAttribute("save", String.valueOf(enable));
		iq.addChild(autoEl);

		writer.write(iq, null, callback);
	}

	@Override
	public void setPacketWriter(PacketWriter packetWriter) {
		this.writer = packetWriter;
	}

	public void setSettings(Settings settings, final AsyncCallback callback) throws JaxmppException {
		IQ iq = IQ.create();
		iq.setType(StanzaType.set);

		iq.addChild(settings);

		writer.write(iq, null, callback);
	}

	public static abstract class CollectionAsyncCallback
			implements AsyncCallback {

		protected abstract void onCollectionReceived(final ResultSet<Chat> vcard) throws XMLException;

		@Override
		public void onSuccess(final Stanza stanza) throws XMLException {
			ResultSet<Chat> rs = new ResultSet<Chat>();

			Element rsm = stanza.getChildrenNS("set", "http://jabber.org/protocol/rsm");
			if (rsm != null) {
				rs.process(rsm);
			}

			for (Element it : stanza.getChildrenNS("list", ARCHIVE_XMLNS).getChildren()) {
				if ("chat".equals(it.getName())) {
					Chat chat = new Chat();
					chat.process(it, format);
					rs.getItems().add(chat);
				}
			}
			onCollectionReceived(rs);
		}
	}

	public static abstract class ItemsAsyncCallback
			implements AsyncCallback {

		private String getChildValue(Element packet, String name) throws XMLException {
			List<Element> vlist = packet.getChildren(name);
			if (vlist == null || vlist.isEmpty()) {
				return null;
			}

			Element v = vlist.get(0);
			if (v != null) {
				return v.getValue();
			} else {
				return null;
			}
		}

		protected abstract void onItemsReceived(final ChatResultSet chat) throws XMLException;

		@Override
		public void onSuccess(final Stanza iq) throws XMLException {
			ChatResultSet rs = new ChatResultSet();

			Element chat = iq.getChildrenNS("chat", ARCHIVE_XMLNS);
			rs.getChat().process(chat, format);

			Element rsm = chat.getChildrenNS("set", "http://jabber.org/protocol/rsm");
			if (rsm != null) {
				rs.process(rsm);
			}

			List<ChatItem> resultItems = new ArrayList<ChatItem>();
			rs.setItems(resultItems);

			Date cd = rs.getChat().getStart();

			for (Element item : chat.getChildren()) {
				String body = getChildValue(item, "body");
				String secsTmp = item.getAttribute("secs");
				String utcTmp = item.getAttribute("utc");
				Date time = null;

				if (secsTmp != null) {
					long msecs = Long.parseLong(secsTmp) * 1000;
					time = new Date(cd.getTime() + msecs);
				} else if (utcTmp != null) {
					try {
						time = format.parse(utcTmp);
					} catch (Exception e) {
					}
				}

				if ("from".equals(item.getName())) {
					ChatItem it = new ChatItem(Type.FROM, time, body, item);
					resultItems.add(it);
				} else if ("to".equals(item.getName())) {
					ChatItem it = new ChatItem(Type.TO, time, body, item);
					resultItems.add(it);
				}
			}

			onItemsReceived(rs);
		}
	}

	public static class Settings
			extends ElementWrapper {

		public Settings() throws XMLException {
			this(ElementFactory.create("pref", null, ARCHIVE_XMLNS));
		}

		public Settings(Element element) {
			super(element);
		}

		public boolean getAutoSave() throws XMLException {
			return Boolean.parseBoolean(getChildAttr("auto", "save"));
		}

		public Settings setAutoSave(boolean value) throws XMLException {
			return setChildAttr("auto", "save", Boolean.toString(value));
		}

		public String getChildAttr(String childName, String attr) throws XMLException {
			Element def = getFirstChild(childName);
			return def == null ? null : def.getAttribute(attr);
		}

		public Long getExpire() throws XMLException {
			return Long.parseLong(getChildAttr("default", "expire"));
		}

		public Settings setExpire(Long value) throws XMLException {
			return setChildAttr("default", "expire", value.toString());
		}

		public SaveMode getSaveMode() throws XMLException {
			return SaveMode.valueof(getChildAttr("default", "save"));
		}

		public Settings setSaveMode(SaveMode mode) throws XMLException {
			return setChildAttr("default", "save", mode.toString());
		}

		public Settings setChildAttr(String childName, String attr, String value) throws XMLException {
			Element child = getFirstChild(childName);

			if (child == null) {
				child = ElementFactory.create(childName);
				addChild(child);
			}
			child.setAttribute(attr, value);

			return this;
		}
	}

	public static abstract class SettingsAsyncCallback
			implements AsyncCallback {

		public abstract void onSuccess(boolean autoArchive);

		public void onSuccess(Settings pref) throws JaxmppException {
			onSuccess(pref.getAutoSave());
		}

		@Override
		public void onSuccess(Stanza stanza) throws JaxmppException {
			Element pref = stanza.getChildrenNS("pref", ARCHIVE_XMLNS);
			onSuccess(new Settings(pref));
		}
	}
}
