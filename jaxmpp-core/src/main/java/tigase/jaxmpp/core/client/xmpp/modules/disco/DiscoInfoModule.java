package tigase.jaxmpp.core.client.xmpp.modules.disco;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.EventType;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xml.XmlTools;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.modules.SoftwareVersionModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class DiscoInfoModule extends AbstractIQModule {

	public static abstract class DiscoInfoAsyncCallback implements AsyncCallback {

		private String requestedNode;

		public DiscoInfoAsyncCallback(final String requestedNode) {
			this.requestedNode = requestedNode;
		}

		protected abstract void onInfoReceived(String node, Collection<Identity> identities, Collection<String> features)
				throws XMLException;

		@Override
		public void onSuccess(Stanza responseStanza) throws XMLException {
			Element query = responseStanza.getChildrenNS("query", "http://jabber.org/protocol/disco#info");
			List<Element> identities = query.getChildren("identity");
			ArrayList<Identity> idres = new ArrayList<DiscoInfoModule.Identity>();
			for (Element id : identities) {
				Identity t = new Identity();
				t.setName(id.getAttribute("name"));
				t.setType(id.getAttribute("type"));
				t.setCategory(id.getAttribute("category"));
				idres.add(t);
			}

			List<Element> features = query.getChildren("feature");
			ArrayList<String> feres = new ArrayList<String>();
			for (Element element : features) {
				String v = element.getAttribute("var");
				if (v != null)
					feres.add(v);
			}

			String n = query.getAttribute("node");
			onInfoReceived(n == null ? requestedNode : n, idres, feres);
		}
	}

	public static class DiscoInfoEvent extends BaseEvent {

		private static final long serialVersionUID = 1L;

		private String[] features;

		private Identity identity;

		private String node;

		private IQ requestStanza;

		public DiscoInfoEvent(EventType type, SessionObject sessionObject) {
			super(type, sessionObject);
		}

		public String[] getFeatures() {
			return features;
		}

		public Identity getIdentity() {
			return identity;
		}

		public String getNode() {
			return node;
		}

		public IQ getRequestStanza() {
			return requestStanza;
		}

		public void setFeatures(String[] features) {
			this.features = features;
		}

		public void setIdentity(Identity identity) {
			this.identity = identity;
		}

		public void setNode(String node) {
			this.node = node;
		}

		public void setRequestStanza(IQ requestStanza) {
			this.requestStanza = requestStanza;
		}

	}

	public static class Identity {
		private String category;

		private String name;

		private String type;

		public String getCategory() {
			return category;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setType(String type) {
			this.type = type;
		}
	}

	public static final Criteria CRIT = ElementCriteria.name("iq").add(
			ElementCriteria.name("query", new String[] { "xmlns" }, new String[] { "http://jabber.org/protocol/disco#info" }));

	public final static String IDENTITY_CATEGORY_KEY = "IDENTITY_CATEGORY_KEY";

	public final static String IDENTITY_TYPE_KEY = "IDENTITY_TYPE_KEY";

	public final static EventType InfoRequested = new EventType();

	private final String[] FEATURES = { "http://jabber.org/protocol/disco#info" };

	private final XmppModulesManager modulesManager;

	private final Observable observable;

	public DiscoInfoModule(Observable parentObservable, SessionObject sessionObject, PacketWriter packetWriter,
			XmppModulesManager modulesManager) {
		super(sessionObject, packetWriter);
		this.modulesManager = modulesManager;
		this.observable = new Observable(parentObservable);
		this.observable.addListener(new Listener<DiscoInfoEvent>() {

			@Override
			public void handleEvent(DiscoInfoEvent be) {
				if (be.getNode() != null)
					return;

				processDefaultDiscoEvent(be);
			}
		});
	}

	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.addListener(eventType, listener);
	}

	public void addListener(Listener<? extends BaseEvent> listener) {
		observable.addListener(listener);
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
	}

	public void getInfo(JID jid, DiscoInfoAsyncCallback callback) throws XMLException, JaxmppException {
		getInfo(jid, null, (AsyncCallback) callback);
	}

	public void getInfo(JID jid, String node, AsyncCallback callback) throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(jid);
		iq.setType(StanzaType.get);
		Element query = new DefaultElement("query", null, "http://jabber.org/protocol/disco#info");
		if (node != null)
			query.setAttribute("node", node);
		iq.addChild(query);

		sessionObject.registerResponseHandler(iq, callback);
		writer.write(iq);
	}

	public void getInfo(JID jid, String node, DiscoInfoAsyncCallback callback) throws JaxmppException {
		getInfo(jid, node, (AsyncCallback) callback);
	}

	public void processDefaultDiscoEvent(final DiscoInfoEvent be) {
		be.setIdentity(new Identity());
		String category = DiscoInfoModule.this.sessionObject.getProperty(IDENTITY_CATEGORY_KEY);
		String type = DiscoInfoModule.this.sessionObject.getProperty(IDENTITY_TYPE_KEY);
		String nme = DiscoInfoModule.this.sessionObject.getProperty(SoftwareVersionModule.NAME_KEY);
		be.getIdentity().setCategory(category == null ? "client" : category);
		be.getIdentity().setName(nme == null ? SoftwareVersionModule.DEFAULT_NAME_VAL : nme);
		be.getIdentity().setType(type == null ? "pc" : type);

		be.setFeatures(DiscoInfoModule.this.modulesManager.getAvailableFeatures().toArray(new String[] {}));

	}

	@Override
	protected void processGet(IQ element) throws XMPPException, XMLException, JaxmppException {
		Element query = element.getChildrenNS("query", "http://jabber.org/protocol/disco#info");
		final String requestedNode = query.getAttribute("node");

		final DiscoInfoEvent event = new DiscoInfoEvent(InfoRequested, sessionObject);
		event.setIdentity(new Identity());
		event.setRequestStanza(element);
		event.setNode(requestedNode);

		this.observable.fireEvent(event);

		Element result = XmlTools.makeResult(element);

		Element queryResult = new DefaultElement("query", null, "http://jabber.org/protocol/disco#info");
		queryResult.setAttribute("node", event.getNode());
		result.addChild(queryResult);

		if (event.getIdentity() != null) {
			Element identity = new DefaultElement("identity");
			identity.setAttribute("category", event.getIdentity().getCategory());
			identity.setAttribute("type", event.getIdentity().getType());
			identity.setAttribute("name", event.getIdentity().getName());
			queryResult.addChild(identity);
		}

		if (event.getFeatures() != null)
			for (String feature : event.getFeatures()) {
				DefaultElement f = new DefaultElement("feature");
				f.setAttribute("var", feature);
				queryResult.addChild(f);
			}

		writer.write(result);
	}

	@Override
	protected void processSet(IQ element) throws XMPPException, XMLException, JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	public void removeAllListeners() {
		observable.removeAllListeners();
	}

	public void removeListener(EventType eventType, Listener<? extends BaseEvent> listener) {
		observable.removeListener(eventType, listener);
	}

}
