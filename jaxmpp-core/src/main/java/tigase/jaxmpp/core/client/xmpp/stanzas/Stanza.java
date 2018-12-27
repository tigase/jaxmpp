/*
 * Stanza.java
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
package tigase.jaxmpp.core.client.xmpp.stanzas;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;

import java.util.List;

/**
 * Abstract representation of Stanza.
 */
public abstract class Stanza
		extends StreamPacket {

	public static boolean canBeConverted(final Element element) throws XMLException {
		if (element instanceof Stanza) {
			return true;
		} else {
			final String name = element.getName();
			return ("presence".equals(name) || "message".equals(name) || "iq".equals(name));
		}
	}

	/**
	 * Creates new stanza.
	 *
	 * @param element element containing stanza.
	 *
	 * @return specific implementation od Stanza: {@linkplain IQ}, {@linkplain Message} or {@linkplain Presence}.
	 */
	public static final Stanza create(final Element element) throws JaxmppException {
		if (element instanceof Stanza) {
			return (Stanza) element;
		}
		final String name = element.getName();
		if ("iq".equals(name)) {
			return new IQ(element);
		} else if ("message".equals(name)) {
			return new Message(element);
		} else if ("presence".equals(name)) {
			return new Presence(element);
		} else {
			JaxmppException e = new UnkownStanzaTypeException("Unkown stanza type '" + name + "'");
			throw e;
		}
	}

	public static final IQ createIQ() throws JaxmppException {
		return (IQ) create(ElementFactory.create("iq"));
	}

	public static final Message createMessage() throws JaxmppException {
		return (Message) create(ElementFactory.create("message"));
	}

	public static final Presence createPresence() throws JaxmppException {
		return (Presence) create(ElementFactory.create("presence"));
	}

	Stanza(Element element) {
		super(element);
	}

	/**
	 * Returns {@linkplain ErrorCondition} element.
	 *
	 * @return {@linkplain ErrorCondition}. <code>null</code> is element not present.
	 */
	public ErrorCondition getErrorCondition() throws XMLException {
		List<Element> es = getChildren("error");
		final Element error;
		if (es != null && es.size() > 0) {
			error = es.get(0);
		} else {
			error = null;
		}

		ErrorCondition errorCondition = null;
		if (error != null) {
			List<Element> conds = error.getChildrenNS(XMPPException.XMLNS);
			if (conds != null && conds.size() > 0) {
				errorCondition = ErrorCondition.getByElementName(conds.get(0).getName());
			}
		}
		return errorCondition;
	}

	public String getErrorMessage() throws XMLException {
		Element errorEl = getFirstChild("error");
		if (errorEl == null) {
			return null;
		}

		Element textEl = errorEl.getChildrenNS("text", XMPPException.XMLNS);
		return textEl == null ? null : textEl.getValue();
	}

	/**
	 * Returns 'from' attribute.
	 *
	 * @return {@linkplain JID}
	 */
	public JID getFrom() throws XMLException {
		String t = getAttribute("from");
		return t == null ? null : JID.jidInstance(t);
	}

	/**
	 * Sets 'from' attribute.
	 *
	 * @param jid {@linkplain JID}
	 */
	public void setFrom(JID jid) throws XMLException {
		if (jid == null) {
			removeAttribute("from");
		} else {
			setAttribute("from", jid.toString());
		}
	}

	/**
	 * Returns id of stanza.
	 *
	 * @return id of stanza
	 */
	public String getId() throws XMLException {
		return getAttribute("id");
	}

	/**
	 * Sets id of stanza
	 *
	 * @param id id
	 *
	 * @throws XMLException
	 */
	public void setId(String id) throws XMLException {
		setAttribute("id", id);
	}

	/**
	 * Returns 'to' attribute.
	 *
	 * @return {@linkplain JID}
	 */
	public JID getTo() throws XMLException {
		String t = getAttribute("to");
		return t == null ? null : JID.jidInstance(t);
	}

	/**
	 * Sets 'to' attribute.
	 *
	 * @param jid {@linkplain JID}
	 */
	public void setTo(JID jid) throws XMLException {
		if (jid == null) {
			removeAttribute("to");
		} else {
			setAttribute("to", jid.toString());
		}
	}

	/**
	 * Returns type of stanza.
	 *
	 * @return {@linkplain StanzaType}. <code>null</code> if type not present.
	 */
	public StanzaType getType() throws XMLException {
		return getType(null);
	}

	/**
	 * Sets type of stanza.
	 *
	 * @param type {@linkplain StanzaType}
	 */
	public void setType(StanzaType type) throws XMLException {
		if (type != null) {
			setAttribute("type", type.name());
		} else {
			removeAttribute("type");
		}
	}

	/**
	 * Returns type of stanza.
	 *
	 * @param defaultValue default value. Will be returned if type of stanza id <code>null</code>.
	 *
	 * @return {@linkplain StanzaType}. <code>defaultValue</code> if type not present.
	 */
	public StanzaType getType(StanzaType defaultValue) throws XMLException {
		try {
			String x = getAttribute("type");
			return x == null ? defaultValue : StanzaType.valueOf(x);
		} catch (XMLException e) {
			throw e;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static class UnkownStanzaTypeException
			extends JaxmppException {

		private static final long serialVersionUID = 1L;

		public UnkownStanzaTypeException() {
			super();
		}

		public UnkownStanzaTypeException(String message) {
			super(message);
		}

		public UnkownStanzaTypeException(String message, Throwable cause) {
			super(message, cause);
		}

		public UnkownStanzaTypeException(Throwable cause) {
			super(cause);
		}

	}

}