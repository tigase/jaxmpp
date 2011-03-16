package tigase.jaxmpp.core.client.xmpp.forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
import tigase.jaxmpp.core.client.xml.XMLException;

public class JabberDataElement extends ElementWrapper {

	private static AbstractField<?> create(Element element) throws XMLException {
		final String type = element.getAttribute("type");
		if (type.equals("boolean")) {
			return new BooleanField(element);
		} else if (type.equals("fixed")) {
			return new FixedField(element);
		} else if (type.equals("hidden")) {
			return new HiddenField(element);
		} else if (type.equals("jid-multi")) {
			return new JidMultiField(element);
		} else if (type.equals("jid-single")) {
			return new JidSingleField(element);
		} else if (type.equals("list-multi")) {
			return new ListMultiField(element);
		} else if (type.equals("list-single")) {
			return new ListSingleField(element);
		} else if (type.equals("text-multi")) {
			return new TextMultiField(element);
		} else if (type.equals("text-private")) {
			return new TextPrivateField(element);
		} else {
			return new TextSingleField(element);
		}
	}

	private final ArrayList<AbstractField<?>> fields = new ArrayList<AbstractField<?>>();

	private final Map<String, AbstractField<?>> fieldsMap = new HashMap<String, AbstractField<?>>();

	public JabberDataElement(Element x) throws JaxmppException {
		super(x);
		try {
			if (!"x".equals(x.getName()) || !"jabber:x:data".equals(x.getXMLNS()))
				throw new JaxmppException("Invalid jabber:x:form element");

			List<Element> fs = x.getChildren("field");
			if (fs != null)
				for (Element element : fs) {
					AbstractField<?> af = create(element);
					if (af != null) {
						this.fields.add(af);
						String var = af.getVar();
						if (var != null)
							this.fieldsMap.put(var, af);
					}
				}

		} catch (XMLException e) {
			throw new JaxmppException(e);
		}
	}

	public JabberDataElement(XDataType type) throws XMLException {
		super(new DefaultElement("x", null, "jabber:x:data"));
		setAttribute("type", type.name());
	}

	public final BooleanField addBooleanField(String var, Boolean value) throws XMLException {
		BooleanField result = new BooleanField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	protected void addField(final AbstractField<?> f) throws XMLException {
		String var = f.getVar();
		if (var != null)
			this.fieldsMap.put(var, f);
		this.fields.add(f);
		addChild(f);
	}

	public final FixedField addFixedField(String value) throws XMLException {
		FixedField result = new FixedField(new DefaultElement("field"));
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	public void addFORM_TYPE(String value) throws XMLException {
		addHiddenField("FORM_TYPE", value);
	}

	public final HiddenField addHiddenField(String var, String value) throws XMLException {
		HiddenField result = new HiddenField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	public final JidMultiField addJidMultiField(String var, JID... value) throws XMLException {
		JidMultiField result = new JidMultiField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	public final JidSingleField addJidSingleField(String var, JID value) throws XMLException {
		JidSingleField result = new JidSingleField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	public final ListMultiField addListMultiField(String var, String... value) throws XMLException {
		ListMultiField result = new ListMultiField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	public final ListSingleField addListSingleField(String var, String value) throws XMLException {
		ListSingleField result = new ListSingleField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	public final TextMultiField addTextMultiField(String var, String... value) throws XMLException {
		TextMultiField result = new TextMultiField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	public final TextPrivateField addTextPrivateField(String var, String value) throws XMLException {
		TextPrivateField result = new TextPrivateField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	public final TextSingleField addTextSingleField(String var, String value) throws XMLException {
		TextSingleField result = new TextSingleField(new DefaultElement("field"));
		result.setVar(var);
		result.setFieldValue(value);
		addField(result);
		return result;
	}

	@SuppressWarnings("unchecked")
	public <X> AbstractField<X> getField(final String var) {
		return (AbstractField<X>) this.fieldsMap.get(var);
	}

	public ArrayList<AbstractField<?>> getFields() {
		return fields;
	}

	public String getInstructions() throws XMLException {
		return getAttribute("instructions");
	}

	public String getTitle() throws XMLException {
		return getAttribute("title");
	}

	public XDataType getType() throws XMLException {
		String x = getAttribute("type");
		return x == null ? null : XDataType.valueOf(x);
	}

	public void setInstructions(String instructions) throws XMLException {
		setAttribute("instructions", instructions);
	}

	public void setTitle(String title) throws XMLException {
		setAttribute("title", title);
	}

}
