package tigase.jaxmpp.core.client.xmpp.modules.chat;

import java.util.List;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class MessageCarbonsModule extends AbstractStanzaModule<Message> {

	public static enum CarbonEventType {
		received,
		sent
	}

	public interface CarbonReceivedHandler extends EventHandler {

		public static class CarbonReceivedEvent extends JaxmppEvent<CarbonReceivedHandler> {

			private CarbonEventType carbonType;
			private Chat chat;
			private Message encapsulatedMessage;

			public CarbonReceivedEvent(SessionObject sessionObject, CarbonEventType carbonType, Message encapsulatedMessage,
					Chat chat) {
				super(sessionObject);
				this.carbonType = carbonType;
				this.encapsulatedMessage = encapsulatedMessage;
				this.chat = chat;
			}

			@Override
			protected void dispatch(CarbonReceivedHandler handler) {
				handler.onCarbonReceived(sessionObject, carbonType, encapsulatedMessage, chat);
			}

			public CarbonEventType getCarbonType() {
				return carbonType;
			}

			public Chat getChat() {
				return chat;
			}

			public Message getEncapsulatedMessage() {
				return encapsulatedMessage;
			}

			public void setCarbonType(CarbonEventType carbonType) {
				this.carbonType = carbonType;
			}

			public void setChat(Chat chat) {
				this.chat = chat;
			}

			public void setEncapsulatedMessage(Message encapsulatedMessage) {
				this.encapsulatedMessage = encapsulatedMessage;
			}

		}

		void onCarbonReceived(SessionObject sessionObject, CarbonEventType carbonType, Message encapsulatedMessage, Chat chat);
	}

	/**
	 * XMLNS of <a href='http://xmpp.org/extensions/xep-0280.html'>Message
	 * Carbons</a>.
	 */
	public static final String XMLNS_MC = "urn:xmpp:carbons:2";

	/**
	 * XMLNS of <a href='http://xmpp.org/extensions/xep-0297.html'>Stanza
	 * Forwarding</a>.
	 */
	static final String XMLNS_SF = "urn:xmpp:forward:0";

	private final Criteria criteria;

	private MessageModule messageModule;

	public MessageCarbonsModule() throws JaxmppException {
		criteria = ElementCriteria.name("message").add(ElementCriteria.xmlns(XMLNS_MC));
	}

	@Override
	public void beforeRegister() {
		super.beforeRegister();

		this.messageModule = context.getModuleProvider().getModule(MessageModule.class);
		if (this.messageModule == null) {
			throw new RuntimeException("Required module: MessageModule not available.");
		}
	}

	/**
	 * Disable carbons.
	 * 
	 * @param callback
	 *            callback
	 */
	public void disable(AsyncCallback callback) throws JaxmppException {
		final IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		iq.addChild(ElementFactory.create("disable", null, XMLNS_MC));
		write(iq, callback);
	}

	/**
	 * Enable carbons.
	 * 
	 * @param callback
	 *            callback
	 */
	public void enable(AsyncCallback callback) throws JaxmppException {
		final IQ iq = IQ.create();
		iq.setType(StanzaType.set);
		iq.addChild(ElementFactory.create("enable", null, XMLNS_MC));
		write(iq, callback);
	}

	@Override
	public Criteria getCriteria() {
		return criteria;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	@Override
	public void process(Message message) throws JaxmppException {
		for (Element carb : message.getChildrenNS(XMLNS_MC)) {
			if ("received".equals(carb.getName())) {
				processReceivedCarbon(message, carb);
			} else if ("sent".equals(carb.getName())) {
				processSentCarbon(message, carb);
			} else
				throw new XMPPException(ErrorCondition.bad_request);
		}
	}

	protected void processReceivedCarbon(final Message message, final Element carb) throws JaxmppException {
		final Element forwarded = carb.getChildrenNS("forwarded", XMLNS_SF);
		List<Element> c = forwarded.getChildren("message");
		for (Element element : c) {
			Message encapsulatedMessage = (Message) Stanza.create(element);

			JID interlocutorJid = encapsulatedMessage.getFrom();
			Chat chat = this.messageModule.getChatManager().process(encapsulatedMessage, interlocutorJid);

			CarbonReceivedHandler.CarbonReceivedEvent event = new CarbonReceivedHandler.CarbonReceivedEvent(
					context.getSessionObject(), CarbonEventType.received, encapsulatedMessage, chat);
			fireEvent(event);
		}
	}

	protected void processSentCarbon(final Message message, final Element carb) throws JaxmppException {
		final Element forwarded = carb.getChildrenNS("forwarded", XMLNS_SF);
		List<Element> c = forwarded.getChildren("message");
		for (Element element : c) {
			Message encapsulatedMessage = (Message) Stanza.create(element);

			JID interlocutorJid = encapsulatedMessage.getTo();
			Chat chat = this.messageModule.getChatManager().process(encapsulatedMessage, interlocutorJid);

			CarbonReceivedHandler.CarbonReceivedEvent event = new CarbonReceivedHandler.CarbonReceivedEvent(
					context.getSessionObject(), CarbonEventType.sent, encapsulatedMessage, chat);

			fireEvent(event);
		}
	}

}
