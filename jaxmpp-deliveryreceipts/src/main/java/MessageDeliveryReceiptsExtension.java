import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.UIDGenerator;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModuleExtension;
import tigase.jaxmpp.core.client.xmpp.modules.extensions.Extension;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;

public class MessageDeliveryReceiptsExtension implements MessageModuleExtension, Extension {

	public interface ReceiptReceivedHandler extends EventHandler {

		public static class ReceiptReceivedEvent extends JaxmppEvent<ReceiptReceivedHandler> {

			private Chat chat;

			private String confirmedId;

			private Message message;

			public ReceiptReceivedEvent(SessionObject sessionObject, Chat chat, Message msg, String confirmedId) {
				super(sessionObject);
				this.chat = chat;
				this.message = msg;
				this.confirmedId = confirmedId;
			}

			@Override
			protected void dispatch(ReceiptReceivedHandler handler) {
				handler.onReceiptReceived(sessionObject, chat, message, confirmedId);
			}

			public Chat getChat() {
				return chat;
			}

			/**
			 * @return the confirmedId
			 */
			public String getConfirmedId() {
				return confirmedId;
			}

			/**
			 * @return the message
			 */
			public Message getMessage() {
				return message;
			}

			public void setChat(Chat chat) {
				this.chat = chat;
			}

			/**
			 * @param confirmedId
			 *            the confirmedId to set
			 */
			public void setConfirmedId(String confirmedId) {
				this.confirmedId = confirmedId;
			}

			/**
			 * @param message
			 *            the message to set
			 */
			public void setMessage(Message message) {
				this.message = message;
			}

		}

		/**
		 * Called when Message Delivery Receipt is received.
		 * 
		 * @param sessionObject
		 *            session object.
		 * @param chat
		 *            chat related to confirmed message.
		 * @param message
		 *            received message with confirmation (this is not confirmed
		 *            message!).
		 * @param confirmedId
		 *            identifier of confirmed message. May be <code>null</code>
		 *            if confirming client supports older version of Receipts.
		 */
		void onReceiptReceived(SessionObject sessionObject, Chat chat, Message message, String confirmedId);
	}

	private final static String RECEIVED_NAME = "received";

	private final static String REQUEST_NAME = "request";

	public static final String XMLNS = "urn:xmpp:receipts";

	private final Context context;

	public MessageDeliveryReceiptsExtension(Context context) {
		this.context = context;
	}

	@Override
	public Element afterReceive(Element received) throws JaxmppException {
		// TODO Auto-generated method stub
		return received;
	}

	@Override
	public Message beforeMessageProcess(Message message, Chat chat) throws JaxmppException {
		final Element request = message.getChildrenNS(REQUEST_NAME, XMLNS);
		if (request != null) {
			final String id = message.getId();

			Message response = Message.create();
			response.setId(UIDGenerator.next());
			response.setTo(message.getFrom());

			Element received = ElementFactory.create(RECEIVED_NAME, null, XMLNS);
			if (id != null)
				received.setAttribute("id", id);
			response.addChild(received);

			context.getWriter().write(response);
		}
		final Element received = message.getChildrenNS(RECEIVED_NAME, XMLNS);
		if (received != null) {
			String id = received.getAttribute("id");
			context.getEventBus().fire(
					new ReceiptReceivedHandler.ReceiptReceivedEvent(context.getSessionObject(), chat, message, id));
		}
		return message;
	}

	@Override
	public Element beforeSend(Element element) throws JaxmppException {
		if ("message".equals(element.getName())) {
			if (element.getAttribute("id") == null) {
				element.setAttribute("id", UIDGenerator.next());
			}
			element.addChild(ElementFactory.create(REQUEST_NAME, null, XMLNS));
		}
		return element;
	}

	@Override
	public String[] getFeatures() {
		return new String[] { XMLNS };
	}

}
