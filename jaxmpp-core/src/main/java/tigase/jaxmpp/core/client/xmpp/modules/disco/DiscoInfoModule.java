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

			onInfoReceived(query.getAttribute("node"), idres, feres);
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

	private final String[] FEATURES = { "http://jabber.org/protocol/disco#info" };

	private final XmppModulesManager modulesManager;

	public DiscoInfoModule(SessionObject sessionObject, PacketWriter packetWriter, XmppModulesManager modulesManager) {
		super(sessionObject, packetWriter);
		this.modulesManager = modulesManager;
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
	}

	public void getInfo(JID jid, AsyncCallback callback) throws XMLException, JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(jid);
		iq.setType(StanzaType.get);
		iq.addChild(new DefaultElement("query", null, "http://jabber.org/protocol/disco#info"));

		sessionObject.registerResponseHandler(iq, callback);
		writer.write(iq);

	}

	public void getInfo(JID jid, DiscoInfoAsyncCallback callback) throws XMLException, JaxmppException {
		getInfo(jid, (AsyncCallback) callback);
	}

	@Override
	protected void processGet(IQ element) throws XMPPException, XMLException, JaxmppException {
		Element result = XmlTools.makeResult(element);

		Element query = new DefaultElement("query", null, "http://jabber.org/protocol/disco#info");

		Element identity = new DefaultElement("identity");
		String category = sessionObject.getProperty(IDENTITY_CATEGORY_KEY);
		String type = sessionObject.getProperty(IDENTITY_TYPE_KEY);
		identity.setAttribute("category", category == null ? "client" : category);
		identity.setAttribute("type", type == null ? "pc" : type);
		String nme = sessionObject.getProperty(SoftwareVersionModule.NAME_KEY);
		identity.setAttribute("name", nme == null ? SoftwareVersionModule.DEFAULT_NAME_VAL : nme);
		query.addChild(identity);

		for (String feature : this.modulesManager.getAvailableFeatures()) {
			DefaultElement f = new DefaultElement("feature");
			f.setAttribute("var", feature);
			query.addChild(f);
		}

		writer.write(result);
	}

	@Override
	protected void processSet(IQ element) throws XMPPException, XMLException, JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

}
