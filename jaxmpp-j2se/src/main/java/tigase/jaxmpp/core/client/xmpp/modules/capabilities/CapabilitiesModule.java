/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.jaxmpp.core.client.xmpp.modules.capabilities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ContextAware;
import tigase.jaxmpp.core.client.xmpp.modules.InitializingModule;
import tigase.jaxmpp.core.client.xmpp.modules.SoftwareVersionModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule.DiscoInfoAsyncCallback;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule.Identity;
import tigase.jaxmpp.core.client.xmpp.modules.disco.NodeDetailsCallback;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.BeforePresenceSendHandler;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.ContactAvailableHandler;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.ContactChangedPresenceHandler;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public class CapabilitiesModule implements XmppModule, ContextAware, InitializingModule {

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

	private Context context;

	private DiscoveryModule discoveryModule;

	// private final XmppModulesManager modulesManager;

	private NodeDetailsCallback nodeDetailsCallback;

	public CapabilitiesModule() {
	}

	@Override
	public void afterRegister() {
	}

	@Override
	public void beforeRegister() {
		if (context == null)
			throw new RuntimeException("Context cannot be null");

		this.discoveryModule = context.getModuleProvider().getModule(DiscoveryModule.class);
		if (this.discoveryModule == null)
			throw new RuntimeException("Required module: DiscoveryModule not available.");

		PresenceModule presenceModule = context.getModuleProvider().getModule(PresenceModule.class);
		if (presenceModule == null)
			throw new RuntimeException("Required module: PresenceModule not available.");

		final BeforePresenceSendHandler beforePresenceSendHandler = new BeforePresenceSendHandler() {

			@Override
			public void onBeforePresenceSend(SessionObject sessionObject, Presence presence) throws JaxmppException {
				CapabilitiesModule.this.onBeforePresenceSend(presence);
			}

		};
		final ContactAvailableHandler contactAvailableHandler = new ContactAvailableHandler() {

			@Override
			public void onContactAvailable(SessionObject sessionObject, Presence stanza, JID jid, Show show, String status,
					Integer priority) throws JaxmppException {
				CapabilitiesModule.this.onReceivedPresence(stanza);
			}
		};
		final ContactChangedPresenceHandler contactChangedPresenceHandler = new ContactChangedPresenceHandler() {

			@Override
			public void onContactChangedPresence(SessionObject sessionObject, Presence stanza, JID jid, Show show,
					String status, Integer priority) throws JaxmppException {
				CapabilitiesModule.this.onReceivedPresence(stanza);
			}
		};
		nodeDetailsCallback = new DiscoveryModule.DefaultNodeDetailsCallback(discoveryModule);

		presenceModule.addBeforePresenceSendHandler(beforePresenceSendHandler);
		presenceModule.addContactAvailableHandler(contactAvailableHandler);
		presenceModule.addContactChangedPresenceHandler(contactChangedPresenceHandler);

		discoveryModule.setNodeCallback("", nodeDetailsCallback);

	}

	@Override
	public void beforeUnregister() {
	}

	private String calculateVerificationString() {
		String category = context.getSessionObject().getProperty(DiscoveryModule.IDENTITY_CATEGORY_KEY);
		String type = context.getSessionObject().getProperty(DiscoveryModule.IDENTITY_TYPE_KEY);
		String nme = context.getSessionObject().getProperty(SoftwareVersionModule.NAME_KEY);
		String v = context.getSessionObject().getProperty(SoftwareVersionModule.VERSION_KEY);

		String identity = category + "/" + type + "//" + nme + " " + v;

		String ver = generateVerificationString(new String[] { identity },
				context.getModuleProvider().getAvailableFeatures().toArray(new String[] {}));

		String oldVer = context.getSessionObject().getProperty(VERIFICATION_STRING_KEY);
		if (oldVer != null && !oldVer.equals(ver)) {
			discoveryModule.removeNodeCallback(getNodeName() + "#" + oldVer);
		}

		context.getSessionObject().setProperty(VERIFICATION_STRING_KEY, ver);
		discoveryModule.setNodeCallback(getNodeName() + "#" + ver, nodeDetailsCallback);

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
		String s = context.getSessionObject().getProperty(NODE_NAME_KEY);
		return s == null ? "http://tigase.org/jaxmpp" : s;
	}

	protected boolean isEnabled() {
		return true;
	}

	protected void onBeforePresenceSend(final Presence presence) throws XMLException {
		if (!isEnabled())
			return;
		String ver = context.getSessionObject().getProperty(VERIFICATION_STRING_KEY);
		if (ver == null) {
			ver = calculateVerificationString();
		}
		if (ver == null)
			return;

		if (presence != null) {
			final Element c = ElementFactory.create("c", null, "http://jabber.org/protocol/caps");
			c.setAttribute("hash", "sha-1");
			c.setAttribute("node", getNodeName());
			c.setAttribute("ver", ver);
			presence.addChild(c);
		}
	}

	protected void onReceivedPresence(final Presence presence) throws JaxmppException {
		if (cache == null)
			return;
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

		discoveryModule.getInfo(presence.getFrom(), node + "#" + ver, new DiscoInfoAsyncCallback(node + "#" + ver) {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
				System.out.println("Error: " + error);
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
				System.out.println("Timeout");
			}
		});

	}

	@Override
	public void process(Element element) throws XMPPException, XMLException, JaxmppException {
	}

	public void setCache(CapabilitiesCache cache) {
		this.cache = cache;
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}
}