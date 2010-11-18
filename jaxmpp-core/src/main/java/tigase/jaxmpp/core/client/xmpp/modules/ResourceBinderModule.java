package tigase.jaxmpp.core.client.xmpp.modules;

import java.util.List;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class ResourceBinderModule implements XmppModule {

	public static final class ResourceBindEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		public ResourceBindEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}
	}

	public static final EventType BIND_ERROR = new EventType();

	public static final EventType BIND_SUCCESSFULL = new EventType();

	public static final String BINDED_RESOURCE_JID = "jaxmpp#bindedResource";

	private final Observable observable = new Observable();

	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void bind(final SessionObject sessionObject, PacketWriter writer) throws XMLException {
		IQ iq = IQ.create();
		iq.setXMLNS("jabber:client");
		iq.setType(StanzaType.set);

		Element bind = new DefaultElement("bind", null, "urn:ietf:params:xml:ns:xmpp-bind");
		iq.addChild(bind);
		bind.addChild(new DefaultElement("resource", (String) sessionObject.getProperty(SessionObject.RESOURCE), null));

		sessionObject.registerResponseHandler(iq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error, PacketWriter writer) throws XMLException {
				ResourceBindEvent event = new ResourceBindEvent(BIND_ERROR, sessionObject);
				observable.fireEvent(BIND_ERROR, event);
			}

			@Override
			public void onSuccess(Stanza responseStanza, PacketWriter writer) throws XMLException {
				String name = null;
				List<Element> bind = responseStanza.getChildrenNS("bind", "urn:ietf:params:xml:ns:xmpp-bind");
				if (bind != null && bind.size() > 0) {
					Element r = bind.get(0).getFirstChild();
					if (r != null)
						name = r.getValue();
				}
				if (name != null) {
					sessionObject.setProperty(BINDED_RESOURCE_JID, JID.jidInstance(name));
					System.out.println("BINDED AS " + name);
					ResourceBindEvent event = new ResourceBindEvent(BIND_SUCCESSFULL, sessionObject);
					observable.fireEvent(BIND_SUCCESSFULL, event);
				} else {
					ResourceBindEvent event = new ResourceBindEvent(BIND_ERROR, sessionObject);
					observable.fireEvent(BIND_ERROR, event);
				}
			}

			@Override
			public void onTimeout(PacketWriter writer) throws XMLException {
				ResourceBindEvent event = new ResourceBindEvent(BIND_ERROR, sessionObject);
				observable.fireEvent(BIND_ERROR, event);
			}
		});
		writer.write(iq);
	}

	@Override
	public Criteria getCriteria() {
		return null;
	}

	@Override
	public String[] getFeatures() {
		return null;
	}

	@Override
	public void process(Element element, SessionObject sessionObject, PacketWriter writer) throws XMPPException, XMLException {
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

}
