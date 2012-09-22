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
package tigase.jaxmpp.core.client.xmpp.forms;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.xml.XMLException;

public class JabberDataElementTest {

	@Test
	public void testCreate01() throws XMLException {
		JabberDataElement x = new JabberDataElement(XDataType.form);

		assertEquals("<x xmlns=\"jabber:x:data\" type=\"form\"/>", x.getAsString());

		x.addFORM_TYPE("jabber:bot");

		assertEquals(
				"<x xmlns=\"jabber:x:data\" type=\"form\"><field var=\"FORM_TYPE\" type=\"hidden\"><value>jabber:bot</value></field></x>",
				x.getAsString());

	}

	@Test
	public void testFieldBoolean01() throws XMLException {
		JabberDataElement x = new JabberDataElement(XDataType.form);
		BooleanField field = x.addBooleanField("public", true);
		assertEquals("boolean", field.getAttribute("type"));
		assertEquals("boolean", field.getType());

		assertEquals("<field var=\"public\" type=\"boolean\"><value>1</value></field>", field.getAsString());
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

}