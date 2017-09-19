/*
 * MessageArchiveManagementModule.java
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
package tigase.jaxmpp.core.client.xmpp.modules.mam;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.forms.JabberDataElement;
import tigase.jaxmpp.core.client.xmpp.forms.XDataType;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.modules.PacketWriterAware;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.extensions.Extension;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.core.client.xmpp.utils.DateTimeFormat;
import tigase.jaxmpp.core.client.xmpp.utils.RSM;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * MessageArchiveManagementModule class implements support for XEP-0313 Message Archive Management.
 */
public class MessageArchiveManagementModule
		extends AbstractStanzaModule
		implements PacketWriterAware, Extension {

	private static final String MAM_XMLNS = "urn:xmpp:mam:1";

	private static final Criteria CRIT = ElementCriteria.name("iq").add(ElementCriteria.xmlns(MAM_XMLNS));

	private static final DateTimeFormat format = new DateTimeFormat();

	public enum DefaultValue {
		always,
		roster,
		never
	}

	private PacketWriter writer = null;

	public static List<JID> mapChildrenToListOfJids(Element elem) throws XMLException {
		List<Element> children = elem.getChildren();
		List<JID> results = new LinkedList<>();
		for (Element child : children) {
			results.add(JID.jidInstance(child.getValue()));
		}
		return results;
	}

	public MessageArchiveManagementModule() {
	}

	@Override
	public Element afterReceive(Element received) throws JaxmppException {
		Element resultEl = received.getChildrenNS("result", MAM_XMLNS);
		if (resultEl == null) {
			return received;
		}

		Element forwarded = resultEl.getChildrenNS("forwarded", "urn:xmpp:forward:0");
		if (forwarded == null) {
			return received;
		}

		Element timestampEl = forwarded.getChildrenNS("delay", "urn:xmpp:delay");
		Element forwardedMessageEl = forwarded.getFirstChild("message");

		String queryid = resultEl.getAttribute("queryid");
		String messageId = resultEl.getAttribute("id");

		Date timestamp = format.parse(timestampEl.getAttribute("stamp"));
		Message forwarededMessage = (Message) Stanza.create(forwardedMessageEl);

		fireEvent(new MessageArchiveItemReceivedEventHandler.MessageArchiveItemReceivedEvent(context.getSessionObject(),
																							 queryid, messageId,
																							 timestamp,
																							 forwarededMessage));
		return null;
	}

	@Override
	public void afterRegister() {
		super.afterRegister();

		MessageModule messageModule = context.getModuleProvider().getModule(MessageModule.class);
		messageModule.addExtension(this);
	}

	@Override
	public Element beforeSend(Element received) throws JaxmppException {
		return received;
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	@Override
	public void process(Stanza stanza) throws JaxmppException {

	}

	public void queryItems(Query query, String queryid, RSM rsm, ResultCallback callback) throws JaxmppException {
		queryItems(query, null, queryid, rsm, callback);
	}

	public void queryItems(Query query, JID componentJid, String queryid, RSM rsm, ResultCallback callback)
			throws JaxmppException {
		queryItems(query, componentJid, null, queryid, rsm, callback);
	}

	public void queryItems(Query query, JID componentJid, String node, String queryid, RSM rsm, ResultCallback callback)
			throws JaxmppException {
		queryItems(query != null ? query.toJabberDataElement(format) : null, componentJid, node, queryid, rsm,
				   callback);
	}

	public void queryItems(JabberDataElement form, String queryid, RSM rsm, ResultCallback callback)
			throws JaxmppException {
		queryItems(form, null, queryid, rsm, callback);
	}

	public void queryItems(JabberDataElement form, JID componentJid, String queryid, RSM rsm, ResultCallback callback)
			throws JaxmppException {
		queryItems(form, componentJid, null, queryid, rsm, callback);
	}

	public void queryItems(JabberDataElement form, JID componentJid, String node, String queryid, RSM rsm,
						   ResultCallback callback) throws JaxmppException {
		IQ iq = IQ.createIQ();
		iq.setType(StanzaType.set);
		if (componentJid != null) {
			iq.setTo(componentJid);
		}

		callback.setQueryid(queryid);
		Element query = ElementFactory.create("query", null, MAM_XMLNS);
		iq.addChild(query);

		if (queryid != null) {
			query.setAttribute("queryid", queryid);
		}
		if (node != null) {
			query.setAttribute("node", node);
		}

		if (form != null) {
			query.addChild(form);
		}

		if (rsm != null) {
			query.addChild(rsm.toElement());
		}

		writer.write(iq, callback);
	}

	public void retrieveQueryForm(QueryFormCallback callback) throws JaxmppException {
		retrieveQueryForm(null, callback);
	}

	public void retrieveQueryForm(JID componentJid, QueryFormCallback callback) throws JaxmppException {
		IQ iq = IQ.createIQ();
		iq.setType(StanzaType.get);
		if (componentJid != null) {
			iq.setTo(componentJid);
		}

		Element query = ElementFactory.create("query", null, MAM_XMLNS);
		iq.addChild(query);

		writer.write(iq, callback);
	}

	public void retrieveSettings(SettingsCallback callback) throws JaxmppException {
		IQ iq = IQ.createIQ();
		iq.setType(StanzaType.get);
		iq.addChild(ElementFactory.create("prefs", null, MAM_XMLNS));
		writer.write(iq, callback);
	}

	@Override
	public void setPacketWriter(PacketWriter packetWriter) {
		this.writer = packetWriter;
	}

	public void updateSetttings(DefaultValue defValue, List<JID> always, List<JID> never, SettingsCallback callback)
			throws JaxmppException {
		if (defValue == null) {
			throw new JaxmppException("Default value may not be NULL!");
		}
		IQ iq = IQ.createIQ();
		iq.setType(StanzaType.set);
		Element prefs = ElementFactory.create("prefs", null, MAM_XMLNS);
		prefs.setAttribute("default", defValue.name());
		iq.addChild(prefs);

		if (always != null) {
			Element alwaysEl = ElementFactory.create("always");
			for (JID jid : always) {
				alwaysEl.addChild(ElementFactory.create("jid", jid.toString(), null));
			}
			prefs.addChild(alwaysEl);
		}
		if (never != null) {
			Element neverEl = ElementFactory.create("never");
			for (JID jid : never) {
				neverEl.addChild(ElementFactory.create("jid", jid.toString(), null));
			}
			prefs.addChild(neverEl);
		}

		writer.write(iq, callback);
	}

	public interface MessageArchiveItemReceivedEventHandler
			extends EventHandler {

		void onArchiveItemReceived(SessionObject sessionObject, String queryid, String messageId, Date timestamp,
								   Message message) throws JaxmppException;

		class MessageArchiveItemReceivedEvent
				extends JaxmppEvent<MessageArchiveItemReceivedEventHandler> {

			private final Message message;
			private final String messageId;
			private final String queryid;
			private final Date timestamp;

			MessageArchiveItemReceivedEvent(SessionObject sessionObject, String queryid, String messageId,
											Date timestamp, Message message) {
				super(sessionObject);
				this.queryid = queryid;
				this.messageId = messageId;
				this.timestamp = timestamp;
				this.message = message;
			}

			@Override
			public void dispatch(MessageArchiveItemReceivedEventHandler handler) throws Exception {
				handler.onArchiveItemReceived(sessionObject, queryid, messageId, timestamp, message);
			}
		}

	}

	public static class Query {

		private Date end;
		private Date start;
		private JID with;

		public Query() {

		}

		public Date getEnd() {
			return end;
		}

		public void setEnd(Date end) {
			this.end = end;
		}

		public Date getStart() {
			return start;
		}

		public void setStart(Date start) {
			this.start = start;
		}

		public JID getWith() {
			return with;
		}

		public void setWith(JID with) {
			this.with = with;
		}

		public JabberDataElement toJabberDataElement(DateTimeFormat df) throws XMLException {
			JabberDataElement data = new JabberDataElement(XDataType.submit);

			data.addFORM_TYPE(MAM_XMLNS);

			if (with != null) {
				data.addJidSingleField("with", with);
			}
			if (start != null) {
				String startStr = df.format(start);
				data.addTextSingleField("start", startStr);
			}
			if (end != null) {
				String endStr = df.format(end);
				data.addTextSingleField("end", endStr);
			}

			return data;
		}
	}

	public static abstract class QueryFormCallback
			implements AsyncCallback {

		@Override
		public void onSuccess(Stanza responseStanza) throws JaxmppException {
			Element queryEl = responseStanza.getChildrenNS("query", MAM_XMLNS);
			Element x = queryEl.getChildrenNS("x", "jabber:x:data");
			JabberDataElement form = null;

			if (x != null) {
				form = new JabberDataElement(x);
			}

			onSuccess(form);
		}

		public abstract void onSuccess(JabberDataElement form) throws JaxmppException;
	}

	public static abstract class ResultCallback
			implements AsyncCallback {

		private String queryid;

		@Override
		public void onSuccess(Stanza responseStanza) throws JaxmppException {
			Element fin = responseStanza.getChildrenNS("fin", MAM_XMLNS);
			RSM rsm = new RSM();
			rsm.fromElement(fin);
			boolean complete = "true".equals(fin.getAttribute("complete"));

			onSuccess(queryid, complete, rsm);
		}

		public abstract void onSuccess(String queryid, boolean complete, RSM rsm) throws JaxmppException;

		protected void setQueryid(String queryid) {
			this.queryid = queryid;
		}
	}

	public static abstract class SettingsCallback
			implements AsyncCallback {

		@Override
		public void onSuccess(Stanza responseStanza) throws JaxmppException {
			Element prefs = responseStanza.getWrappedElement().getChildrenNS("prefs", MAM_XMLNS);
			DefaultValue defValue = DefaultValue.valueOf(prefs.getAttribute("default"));
			List<JID> always = mapChildrenToListOfJids(prefs.getFirstChild("always"));
			List<JID> never = mapChildrenToListOfJids(prefs.getFirstChild("never"));
			onSuccess(defValue, always, never);
		}

		public abstract void onSuccess(DefaultValue defValue, List<JID> always, List<JID> never) throws JaxmppException;

	}

}
