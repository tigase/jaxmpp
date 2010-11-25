package tigase.jaxmpp.core.client.xmpp.stanzas;

import java.util.List;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementWrapper;
import tigase.jaxmpp.core.client.xml.XMLException;

public abstract class Stanza extends ElementWrapper {

	public static final Stanza create(final Element element) throws XMLException {
		final String name = element.getName();
		if ("iq".equals(name))
			return new IQ(element);
		else if ("message".equals(name))
			return new Message(element);
		else if ("presence".equals(name))
			return new Presence(element);
		else
			return new Stanza(element) {
			};
	}

	public Stanza(Element element) {
		super(element);
	}

	protected String getChildElementValue(final String elemName) throws XMLException {
		Element b = getFirstChild(elemName);
		return b == null ? null : b.getValue();
	}

	public ErrorCondition getErrorCondition() throws XMLException {
		List<Element> es = getChildren("error");
		final Element error;
		if (es != null && es.size() > 0)
			error = es.get(0);
		else
			error = null;

		ErrorCondition errorCondition = null;
		if (error != null) {
			List<Element> conds = error.getChildrenNS(XMPPException.XMLNS);
			if (conds != null && conds.size() > 0) {
				errorCondition = ErrorCondition.getByElementName(conds.get(0).getName());
			}
		}
		return errorCondition;
	}

	protected Element getFirstChild(String name) throws XMLException {
		List<Element> l = getChildren(name);
		return l != null && !l.isEmpty() ? l.get(0) : null;
	}

	public JID getFrom() throws XMLException {
		String t = getAttribute("from");
		return t == null ? null : JID.jidInstance(t);
	}

	public String getId() throws XMLException {
		return getAttribute("id");
	}

	public JID getTo() throws XMLException {
		String t = getAttribute("to");
		return t == null ? null : JID.jidInstance(t);
	}

	public StanzaType getType() throws XMLException {
		return getType(null);
	}

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

	protected void setChildElementValue(final String elemName, final String value) throws XMLException {
		Element b = getFirstChild(elemName);
		if (value == null && b != null)
			removeChild(b);
		else if (value != null && b == null) {
			b = new DefaultElement(elemName);
			addChild(b);
			b.setValue(value);
		} else if (value != null && b != null) {
			b.setValue(value);
		}
	}

	public void setFrom(JID jid) throws XMLException {
		if (jid == null)
			removeAttribute("from");
		else
			setAttribute("from", jid.toString());
	}

	public void setId(String id) throws XMLException {
		setAttribute("id", id);
	}

	public void setTo(JID jid) throws XMLException {
		if (jid == null)
			removeAttribute("to");
		else
			setAttribute("to", jid.toString());
	}

	public void setType(StanzaType type) throws XMLException {
		if (type != null)
			setAttribute("type", type.name());
		else
			removeAttribute("type");
	}

}
