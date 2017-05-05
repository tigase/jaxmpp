/*
 * RegistrationForm.java
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

package tigase.jaxmpp.core.client.xmpp.modules.registration;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.forms.*;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;

/**
 * Created by bmalkow on 25.04.2017.
 */
public class UnifiedRegistrationForm
		extends JabberDataElement {

	public enum RegistrationFormType {
		classic,
		form;
	}

	private final IQ stanza;
	private RegistrationFormType registrationFormType;

	private static void fillForm(final JabberDataElement form, final Element query) throws JaxmppException {
		form.addFORM_TYPE("jabber:iq:register");

		for (final Element c : query.getChildren()) {
			switch (c.getName().toLowerCase()) {
				case "instructions":
					form.setInstructions(c.getValue());
					break;
				case "password":
					form.addTextPrivateField(c.getName(), c.getValue()).setLabel("Password");
					break;
				case "old_password":
					form.addTextPrivateField(c.getName(), c.getValue()).setLabel("Old password");
					break;
				case "username":
					form.addTextSingleField(c.getName(), c.getValue()).setLabel("Username");
					break;
				case "email":
					form.addTextSingleField(c.getName(), c.getValue()).setLabel("Email address");
					break;
				default:
					form.addTextSingleField(c.getName(), c.getValue()).setLabel(c.getName());
			}
		}
	}

	private static Element prepare(IQ responseStanza) throws JaxmppException {
		Element query = responseStanza.getChildrenNS("query", "jabber:iq:register");
		Element x = query.getChildrenNS("x", "jabber:x:data");
		if (x != null) {
			return x;
		} else {
			JabberDataElement r = new JabberDataElement(XDataType.form);
			fillForm(r, query);
			return r;
		}
	}

	public UnifiedRegistrationForm(IQ responseStanza) throws JaxmppException {
		super(prepare(responseStanza));
		this.stanza = responseStanza;
		Element query = responseStanza.getChildrenNS("query", "jabber:iq:register");
		Element x = query.getChildrenNS("x", "jabber:x:data");
		this.registrationFormType = x == null ? RegistrationFormType.classic : RegistrationFormType.form;
	}

	public RegistrationFormType getRegistrationFormType() {
		return registrationFormType;
	}

	/**
	 * Returns filled query element.
	 *
	 * @return query element
	 *
	 * @throws JaxmppException
	 */
	public Element getRegistrationQuery() throws JaxmppException {
		Element result = ElementFactory.create("query", null, "jabber:iq:register");
		if (registrationFormType == RegistrationFormType.form) {
			result.addChild(createSubmitableElement(XDataType.submit));
		} else if (registrationFormType == RegistrationFormType.classic) {
			for (AbstractField<?> f : getFields()) {
				switch (f.getVar()) {
					case "instructions":
					case "FORM_TYPE":
						break;
					default:
						String v = f.getFieldValue().toString();
						result.addChild(ElementFactory.create(f.getVar(), v, null));
				}
			}
		} else {
			throw new JaxmppException("Unsopported registration form type");
		}
		return result;
	}

	public void setEmail(String email) throws XMLException {
		TextSingleField f = getField("email");
		if (f != null) {
			f.setFieldValue(email);
		} else {
			addTextSingleField("email", email);
		}
	}

	public void setPassword(String password) throws XMLException {
		TextPrivateField f = getField("password");
		if (f != null) {
			f.setFieldValue(password);
		} else {
			addTextPrivateField("password", password);
		}
	}

	public void setUsername(String username) throws XMLException {
		TextSingleField f = getField("username");
		if (f != null) {
			f.setFieldValue(username);
		} else {
			addTextSingleField("username", username);
		}
	}

}
