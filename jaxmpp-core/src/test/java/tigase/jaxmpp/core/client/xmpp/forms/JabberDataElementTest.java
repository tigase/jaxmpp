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
