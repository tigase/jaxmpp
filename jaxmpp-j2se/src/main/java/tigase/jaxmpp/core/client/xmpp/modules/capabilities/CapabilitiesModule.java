package tigase.jaxmpp.core.client.xmpp.modules.capabilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.SoftwareVersionModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule.DiscoInfoAsyncCallback;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule.DiscoInfoEvent;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule.Identity;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.PresenceEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public class CapabilitiesModule implements XmppModule {

	private final static String ALGORITHM = "SHA-1";

	public final static String NODE_NAME_KEY = "NODE_NAME_KEY";

	public static final String VERIFICATION_STRING_KEY = "XEP115VerificationString";

	public static String generateVerificationString(String[] identities, String[] features) {
		try {
			MessageDigest md = MessageDigest.getInstance(ALGORITHM);

			for (String id : identities) {
				md.update(id.getBytes());
				md.update((byte) '<');
			}

			Arrays.sort(features);

			for (String f : features) {
				md.update(f.getBytes());
				md.update((byte) '<');
			}

			byte[] digest = md.digest();
			return Base64.encode(digest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	private CapabilitiesCache cache;

	private final DiscoInfoModule discoInfoModule;

	private final XmppModulesManager modulesManager;

	private final PresenceModule presenceModule;

	private final SessionObject sessionObject;

	private final PacketWriter writer;

	public CapabilitiesModule(SessionObject sessionObject, PacketWriter writer, DiscoInfoModule discoInfoModule,
			PresenceModule presenceModule, XmppModulesManager modulesManager) {
		this.sessionObject = sessionObject;
		this.writer = writer;
		this.discoInfoModule = discoInfoModule;
		this.presenceModule = presenceModule;
		this.presenceModule.addListener(PresenceModule.BeforePresenceSend, new Listener<PresenceModule.PresenceEvent>() {

			@Override
			public void handleEvent(PresenceEvent be) throws JaxmppException {
				onBeforePresenceSend(be);
			}
		});

		Listener<PresenceEvent> presenceListener = new Listener<PresenceModule.PresenceEvent>() {

			@Override
			public void handleEvent(PresenceEvent be) throws JaxmppException {
				onReceivedPresence(be);
			}
		};
		this.presenceModule.addListener(PresenceModule.ContactAvailable, presenceListener);
		this.presenceModule.addListener(PresenceModule.ContactChangedPresence, presenceListener);

		this.discoInfoModule.addListener(new Listener<DiscoInfoEvent>() {

			@Override
			public void handleEvent(DiscoInfoEvent be) {
				onDiscoInfoModuleRequest(be);
			}
		});
		this.modulesManager = modulesManager;

	}

	private String calculateVerificationString() {
		String category = sessionObject.getProperty(DiscoInfoModule.IDENTITY_CATEGORY_KEY);
		String type = sessionObject.getProperty(DiscoInfoModule.IDENTITY_TYPE_KEY);
		String nme = sessionObject.getProperty(SoftwareVersionModule.NAME_KEY);
		String v = sessionObject.getProperty(SoftwareVersionModule.VERSION_KEY);

		String identity = category + "/" + type + "//" + nme + " " + v;

		String ver = generateVerificationString(new String[] { identity },
				this.modulesManager.getAvailableFeatures().toArray(new String[] {}));

		sessionObject.setProperty(VERIFICATION_STRING_KEY, ver);
		return ver;
	}

	public CapabilitiesCache getCache() {
		return cache;
	}

	@Override
	public Criteria getCriteria() {
		return null;
	}

	@Override
	public String[] getFeatures() {
		return new String[] { "http://jabber.org/protocol/caps" };
	}

	protected String getNodeName() {
		String s = sessionObject.getProperty(NODE_NAME_KEY);
		return s == null ? "http://tigase.org" : s;
	}

	protected boolean isEnabled() {
		return true;
	}

	protected void onBeforePresenceSend(PresenceEvent be) throws XMLException {
		if (!isEnabled())
			return;
		String ver = sessionObject.getProperty(VERIFICATION_STRING_KEY);
		if (ver == null) {
			ver = calculateVerificationString();
		}
		if (ver == null)
			return;

		Presence p = be.getPresence();
		if (p != null) {
			final DefaultElement c = new DefaultElement("c", null, "http://jabber.org/protocol/caps");
			c.setAttribute("hash", "sha-1");
			c.setAttribute("node", getNodeName());
			c.setAttribute("ver", ver);
			p.addChild(c);
		}
	}

	protected void onDiscoInfoModuleRequest(DiscoInfoEvent be) {
		if (be.getNode() == null)
			return;
		if (!isEnabled())
			return;
		String ver = sessionObject.getProperty(VERIFICATION_STRING_KEY);
		if (ver == null) {
			ver = calculateVerificationString();
		}
		if (ver == null)
			return;

		if (!be.getNode().equals(getNodeName() + '#' + ver))
			return;

		this.discoInfoModule.processDefaultDiscoEvent(be);
	}

	protected void onReceivedPresence(final PresenceEvent be) throws JaxmppException {
		if (cache == null)
			return;
		final Presence presence = be.getPresence();
		if (presence == null)
			return;
		Element c = presence.getChildrenNS("c", "http://jabber.org/protocol/caps");
		if (c == null)
			return;

		String node = c.getAttribute("node");
		String ver = c.getAttribute("ver");
		if (node == null || ver == null)
			return;

		if (cache.isCached(node + "#" + ver))
			return;

		discoInfoModule.getInfo(presence.getFrom(), node + "#" + ver, new DiscoInfoAsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
			}

			@Override
			protected void onInfoReceived(final String node, Collection<Identity> identities, final Collection<String> features)
					throws XMLException {
				String name = "?";
				String category = "?";
				String type = "?";

				if (identities != null && identities.size() > 0) {
					Identity identity = identities.iterator().next();
					name = identity.getName();
					category = identity.getCategory();
					type = identity.getType();
				}
				if (cache != null)
					cache.store(node, name, category, type, features);
			}

			@Override
			public void onTimeout() throws JaxmppException {
			}
		});

	}

	@Override
	public void process(Element element) throws XMPPException, XMLException, JaxmppException {
		// TODO Auto-generated method stub

	}

	public void setCache(CapabilitiesCache cache) {
		this.cache = cache;
	}
}
