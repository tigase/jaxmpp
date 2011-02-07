package tigase.jaxmpp.core.client.xmpp.modules.disco;

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

public class DiscoInfoModule extends AbstractIQModule {

	public static final Criteria CRIT = ElementCriteria.name("iq").add(
			ElementCriteria.name("query", new String[] { "xmlns" }, new String[] { "http://jabber.org/protocol/disco#info" }));

	public final static String IDENTITY_CATEGORY_KEY = "IDENTITY_CATEGORY_KEY";

	public final static String IDENTITY_TYPE_KEY = "IDENTITY_TYPE_KEY";

	private final String[] FEATURES = { "http://jabber.org/protocol/disco#info" };

	private XmppModulesManager modulesManager;

	public DiscoInfoModule(SessionObject sessionObject, PacketWriter packetWriter) {
		super(sessionObject, packetWriter);
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
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
