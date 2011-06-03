package tigase.jaxmpp.core.client;

import java.util.logging.Logger;

import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public class Processor {

	public static class FeatureNotImplementedResponse extends AbstractStanzaHandler {

		protected final Logger log = Logger.getLogger(this.getClass().getName());

		public FeatureNotImplementedResponse(Stanza stanza, PacketWriter writer, SessionObject sessionObject) {
			super(stanza, writer, sessionObject);
		}

		@Override
		protected void process() throws XMLException, XMPPException {
			log.fine(ErrorCondition.feature_not_implemented.name() + " " + stanza.getAsString());
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

	private final XmppModulesManager xmppModulesManages;

	public Processor(XmppModulesManager xmppModulesManages, final SessionObject sessionObject, final PacketWriter writer) {
		this.sessionObject = sessionObject;
		this.writer = writer;
		this.xmppModulesManages = xmppModulesManages;
	}

	public XmppModulesManager getXmppModulesManages() {
		return xmppModulesManages;
	}

	public Runnable process(final Element stanza) {
		try {
			Runnable result = sessionObject.getResponseHandler(stanza, writer, sessionObject);
			if (result != null)
				return result;

			if (stanza.getName().equals("iq") && stanza.getAttribute("type") != null
					&& (stanza.getAttribute("type").equals("error") || stanza.getAttribute("type").equals("result")))
				return null;

			final XmppModule module = xmppModulesManages.findModule(stanza);
			if (module == null)
				result = new FeatureNotImplementedResponse(Stanza.create(stanza), writer, sessionObject);
			else {
				result = new AbstractStanzaHandler(Stanza.create(stanza), writer, sessionObject) {

					@Override
					protected void process() throws XMLException, XMPPException, JaxmppException {
						module.process(this.stanza);
					}
				};
			}
			return result;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
