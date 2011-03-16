package tigase.jaxmpp.core.client.xmpp.modules;

import java.util.List;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UIDGenerator;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.modules.chat.ChatManager;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;

public class MessageModule extends AbstractStanzaModule<Message> {

	public static class MessageEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private Chat chat;

		private Message message;

		public MessageEvent(EventType type) {
			super(type);
		}

		public Chat getChat() {
			return chat;
		}

		public Message getMessage() {
			return message;
		}

		public void setChat(Chat chat) {
			this.chat = chat;
		}

		public void setMessage(Message message) {
			this.message = message;
		}
	}

	public static final EventType ChatCreated = new EventType();

	public static final Criteria CRIT = ElementCriteria.name("message");

	public static final EventType MessageReceived = new EventType();

	private final ChatManager chatManager;

	private final Observable observable;

	public MessageModule(Observable parentObservable, SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);
		this.observable = new Observable(parentObservable);
		this.chatManager = new ChatManager(sessionObject, packetWriter, observable);
	}

	public void addListener(EventType eventType, Listener<MessageEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public ChatManager getChatManager() {
		return chatManager;
	}

	public List<Chat> getChats() {
		return this.chatManager.getChats();
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	@Override
	public void process(Message element) throws XMPPException, XMLException {
		MessageEvent event = new MessageEvent(MessageReceived);
		event.setMessage(element);
		Chat chat = chatManager.process(element);
		if (chat != null) {
			event.setChat(chat);
		}
		observable.fireEvent(event.getType(), event);
	}

	public void removeListener(EventType eventType, Listener<MessageEvent> listener) {
		observable.removeListener(eventType, listener);
	}

	public void sendMessage(JID toJID, String subject, String message) throws XMLException, JaxmppException {
		Message msg = Message.create();
		msg.setSubject(subject);
		msg.setBody(message);
		msg.setTo(toJID);
		msg.setId(UIDGenerator.next());

		writer.write(msg);
	}

}
