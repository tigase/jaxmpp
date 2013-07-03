package tigase.jaxmpp.core.client.xmpp.modules.chat;

import java.util.List;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractStanzaModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class MessageCarbonsModule extends AbstractStanzaModule<Message> {

	public static enum CarbonEventType {
		received,
		sent
	}

	public static abstract class MessageCarbonEvent extends MessageModule.MessageEvent {

		private static final long serialVersionUID = 1L;

		protected final CarbonEventType carbonEventType;

		protected MessageCarbonEvent(EventType type, SessionObject sessionObject, CarbonEventType carbonEventType) {
			super(type, sessionObject);
			this.carbonEventType = carbonEventType;
		}

		public CarbonEventType getCarbonEventType() {
			return carbonEventType;
		}
	}

	public static class MessageReceivedCarbonEvent extends MessageCarbonEvent {

		private static final long serialVersionUID = 1L;

		protected MessageReceivedCarbonEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject, CarbonEventType.received);
		}
	}

	public static class MessageSentCarbonEvent extends MessageCarbonEvent {

		private static final long serialVersionUID = 1L;

		protected MessageSentCarbonEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject, CarbonEventType.sent);
		}
	}

	public static final EventType Carbon = new EventType();

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

	private final MessageModule messageModule;

	public MessageCarbonsModule(SessionObject sessionObject, MessageModule messageModule, PacketWriter packetWriter) {
		super(messageModule.getObservable(), sessionObject, packetWriter);
		this.messageModule = messageModule;
		criteria = ElementCriteria.name("message").add(ElementCriteria.xmlns(XMLNS_MC));
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
		iq.addChild(new DefaultElement("disable", null, XMLNS_MC));
		writer.write(iq, callback);
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
		iq.addChild(new DefaultElement("enable", null, XMLNS_MC));
		writer.write(iq, callback);
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
			MessageReceivedCarbonEvent event = new MessageReceivedCarbonEvent(Carbon, sessionObject);
			Message encapsulatedMessage = new Message(element);
			event.setMessage(encapsulatedMessage);

			JID interlocutorJid = encapsulatedMessage.getFrom();
			Chat chat = this.messageModule.getChatManager().process(encapsulatedMessage, interlocutorJid, observable);
			if (chat != null)
				event.setChat(chat);

			observable.fireEvent(event);
		}
	}

	protected void processSentCarbon(final Message message, final Element carb) throws JaxmppException {
		final Element forwarded = carb.getChildrenNS("forwarded", XMLNS_SF);
		List<Element> c = forwarded.getChildren("message");
		for (Element element : c) {
			MessageSentCarbonEvent event = new MessageSentCarbonEvent(Carbon, sessionObject);
			Message encapsulatedMessage = new Message(element);
			event.setMessage(encapsulatedMessage);

			JID interlocutorJid = encapsulatedMessage.getTo();
			Chat chat = this.messageModule.getChatManager().process(encapsulatedMessage, interlocutorJid, observable);
			if (chat != null)
				event.setChat(chat);

			observable.fireEvent(event);
		}
	}

}
