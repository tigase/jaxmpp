package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public class Processor {

	public static class FeatureNotImplementedResponse extends AbstractStanzaHandler {

		public FeatureNotImplementedResponse(Stanza stanza, PacketWriter writer, SessionObject sessionObject) {
			super(stanza, writer, sessionObject);
		}

		@Override
		protected void process() throws XMLException, XMPPException {
			throw new XMPPException(ErrorCondition.feature_not_implemented);
		}

	}

	public static Element createError(final Element stanza, Throwable caught) {
		try {
			DefaultElement result = new DefaultElement(stanza.getName(), null, null);
			result.setAttribute("type", "error");
			result.setAttribute("to", stanza.getAttribute("from"));
			result.setAttribute("id", stanza.getAttribute("id"));

			DefaultElement error = new DefaultElement("error", null, null);
			if (caught instanceof XMPPException) {
				if (((XMPPException) caught).getCondition().getType() != null)
					error.setAttribute("type", ((XMPPException) caught).getCondition().getType());
				if (((XMPPException) caught).getCondition().getErrorCode() != 0)
					error.setAttribute("code", "" + ((XMPPException) caught).getCondition().getErrorCode());
				DefaultElement ed = new DefaultElement(((XMPPException) caught).getCondition().getElementName(), null, null);
				ed.setAttribute("xmlns", XMPPException.getXmlns());
				error.addChild(ed);
			} else {
				return null;
			}

			if (caught.getMessage() != null) {
				DefaultElement text = new DefaultElement("text", caught.getMessage(), null);
				text.setAttribute("xmlns", "urn:ietf:params:xml:ns:xmpp-stanzas");
				error.addChild(text);
			}
			result.addChild(error);

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private final SessionObject sessionObject;

	private final PacketWriter writer;

	private final XmppModulesManages xmppModulesManages;

	public Processor(XmppModulesManages xmppModulesManages, final SessionObject sessionObject, final PacketWriter writer) {
		this.sessionObject = sessionObject;
		this.writer = writer;
		this.xmppModulesManages = xmppModulesManages;
	}

	public XmppModulesManages getXmppModulesManages() {
		return xmppModulesManages;
	}

	public Runnable process(final Element stanza) {
		try {
			Runnable result = sessionObject.getResponseHandler(stanza, writer, sessionObject);
			if (result != null)
				return result;

			if (stanza.getName().equals("iq") && stanza.getAttribute("type") != null
					&& stanza.getAttribute("type").equals("error"))
				return null;

			final XmppModule module = xmppModulesManages.findModule(stanza);
			if (module == null)
				result = new FeatureNotImplementedResponse(Stanza.create(stanza), writer, sessionObject);
			else {
				result = new AbstractStanzaHandler(Stanza.create(stanza), writer, sessionObject) {

					@Override
					protected void process() throws XMLException, XMPPException {
						module.process(this.stanza, this.sessionObject, this.writer);
					}
				};
			}
			return result;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
