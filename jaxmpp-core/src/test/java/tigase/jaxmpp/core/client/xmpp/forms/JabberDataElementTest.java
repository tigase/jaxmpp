/*
 * JabberDataElementTest.java
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
package tigase.jaxmpp.core.client.xmpp.forms;

import org.junit.Test;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.ElementBuilder;
import tigase.jaxmpp.core.client.xml.XMLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JabberDataElementTest {

	@Test
	public void testCreate01() throws XMLException {
		JabberDataElement x = new JabberDataElement(XDataType.form);

		ElementBuilder expected = ElementBuilder.create("x").setXMLNS("jabber:x:data").setAttribute("type", "form");
		assertEquals(expected.getElement(), x);

		x.addFORM_TYPE("jabber:bot");

		expected = ElementBuilder.create("x")
				.setXMLNS("jabber:x:data")
				.setAttribute("type", "form")
				.child("field")
				.setAttribute("var", "FORM_TYPE")
				.setAttribute("type", "hidden")
				.setValue("jabber:bot");
		assertEquals(expected.getElement(), x);
	}

	@Test
	public void testCreate02() throws XMLException {
		JabberDataElement x = new JabberDataElement(XDataType.form);

		ElementBuilder expected = ElementBuilder.create("x").setXMLNS("jabber:x:data").setAttribute("type", "form");
		assertEquals(expected.getElement(), x);

		x.addFORM_TYPE("jabber:bot");

		expected = ElementBuilder.create("x")
				.setXMLNS("jabber:x:data")
				.setAttribute("type", "form")
				.child("field")
				.setAttribute("var", "FORM_TYPE")
				.setAttribute("type", "hidden")
				.setValue("jabber:bot");
		assertEquals(expected.getElement(), x);

		x.setInstructions("in");
		expected = ElementBuilder.create("x")
				.setXMLNS("jabber:x:data")
				.setAttribute("type", "form")
				.child("field")
				.setAttribute("var", "FORM_TYPE")
				.setAttribute("type", "hidden")
				.setValue("jabber:bot")
				.up()
				.child("instructions")
				.setValue("in");
		assertEquals(expected.getElement(), x);

		x.setTitle("tt");
		expected = ElementBuilder.create("x")
				.setXMLNS("jabber:x:data")
				.setAttribute("type", "form")
				.child("field")
				.setAttribute("var", "FORM_TYPE")
				.setAttribute("type", "hidden")
				.setValue("jabber:bot")
				.up()
				.child("instructions")
				.setValue("in")
				.up()
				.child("title")
				.setValue("tt");
		assertEquals(expected.getElement(), x);
	}

	@Test
	public void testFieldBoolean01() throws XMLException {
		JabberDataElement x = new JabberDataElement(XDataType.form);
		BooleanField field = x.addBooleanField("public", true);
		assertEquals("boolean", field.getAttribute("type"));
		assertEquals("boolean", field.getType());

		ElementBuilder expected = ElementBuilder.create("field")
				.setAttribute("type", "boolean")
				.setAttribute("var", "public")
				.child("value")
				.setValue("1");

		assertEquals(expected.getElement(), field);
	}

	@Test
	public void testFieldFixed() throws XMLException {
		JabberDataElement x = new JabberDataElement(XDataType.form);
		FixedField field = x.addFixedField("tralala");
		assertEquals("fixed", field.getAttribute("type"));
		assertEquals("fixed", field.getType());
	}

	@Test
	public void testFieldHidden() throws XMLException {
		JabberDataElement x = new JabberDataElement(XDataType.form);
		HiddenField field = x.addHiddenField("public", "maybe");
		assertEquals("hidden", field.getAttribute("type"));
		assertEquals("hidden", field.getType());
	}

	@Test
	public void testFieldJidMulti() throws XMLException {
		JabberDataElement x = new JabberDataElement(XDataType.form);
		JidMultiField field = x.addJidMultiField("jids", JID.jidInstance("a@b"), JID.jidInstance("b@c"));
		assertEquals("jid-multi", field.getAttribute("type"));
		assertEquals("jid-multi", field.getType());
	}

	@Test
	public void testFieldJidSingle() throws XMLException {
		JabberDataElement x = new JabberDataElement(XDataType.form);
		JidSingleField field = x.addJidSingleField("jid", JID.jidInstance("a@b"));
		assertEquals("jid-single", field.getAttribute("type"));
		assertEquals("jid-single", field.getType());
	}

	@Test
	public void testFieldListMulti() throws XMLException {
		JabberDataElement x = new JabberDataElement(XDataType.form);
		ListMultiField field = x.addListMultiField("public", "1", "2");
		assertEquals("list-multi", field.getAttribute("type"));
		assertEquals("list-multi", field.getType());
	}

	@Test
	public void testFieldListSingle() throws XMLException {
		JabberDataElement x = new JabberDataElement(XDataType.form);
		ListSingleField field = x.addListSingleField("public", "1");
		assertEquals("list-single", field.getAttribute("type"));
		assertEquals("list-single", field.getType());
	}

	@Test
	public void testFieldTextMulti() throws XMLException {
		JabberDataElement x = new JabberDataElement(XDataType.form);
		TextMultiField field = x.addTextMultiField("public", "1", "2", "3");
		assertEquals("text-multi", field.getAttribute("type"));
		assertEquals("text-multi", field.getType());
	}

	@Test
	public void testFieldTextPrivate() throws XMLException {
		JabberDataElement x = new JabberDataElement(XDataType.form);
		TextPrivateField field = x.addTextPrivateField("public", "test");
		assertEquals("text-private", field.getAttribute("type"));
		assertEquals("text-private", field.getType());
	}

	@Test
	public void testFieldTextSingle() throws XMLException {
		JabberDataElement x = new JabberDataElement(XDataType.form);
		TextSingleField field = x.addTextSingleField("public", "once");
		assertEquals("text-single", field.getAttribute("type"));
		assertEquals("text-single", field.getType());
	}

	@Test
	public void testReplacingField() throws JaxmppException {
		JabberDataElement x = new JabberDataElement(XDataType.form);
		x.addTextSingleField("public", "1");
		x.addTextSingleField("public", "2");
		x.addTextPrivateField("public", "3");

		System.out.println(x.createSubmitableElement(XDataType.submit).getAsString());

		assertTrue(x.getField("public") instanceof TextPrivateField);
		assertEquals("3", ((TextPrivateField) x.getField("public")).getFieldValue());
	}

}