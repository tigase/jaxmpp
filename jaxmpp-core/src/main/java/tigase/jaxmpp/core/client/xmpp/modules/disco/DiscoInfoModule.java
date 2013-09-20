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
package tigase.jaxmpp.core.client.xmpp.modules.disco;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.EventType;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xml.XmlTools;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.SoftwareVersionModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule.ServerFeaturesReceivedHandler.ServerFeaturesReceivedEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class DiscoInfoModule extends AbstractIQModule {

	public static abstract class DiscoInfoAsyncCallback implements AsyncCallback {

		private String requestedNode;

		protected Stanza responseStanza;

		public DiscoInfoAsyncCallback(final String requestedNode) {
			this.requestedNode = requestedNode;
		}

		protected abstract void onInfoReceived(String node, Collection<Identity> identities, Collection<String> features)
				throws XMLException;

		@Override
		public void onSuccess(Stanza responseStanza) throws XMLException {
			this.responseStanza = responseStanza;
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

	public static class Identity {
		private String category;

		private String name;

		private String type;

		public String getCategory() {
			return category == null ? "" : category;
		}

		public String getName() {
			return name == null ? "" : name;
		}

		public String getType() {
			return type == null ? "" : type;
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

	public static final String SERVER_FEATURES_KEY = "SERVER_FEATURES_KEY";

	public interface ServerFeaturesReceivedHandler extends EventHandler {

		public static class ServerFeaturesReceivedEvent extends JaxmppEvent<ServerFeaturesReceivedHandler> {

			public static final EventType<ServerFeaturesReceivedHandler> TYPE = new EventType<ServerFeaturesReceivedHandler>();

			private final IQ stanza;

			private final String[] features;

			public ServerFeaturesReceivedEvent(SessionObject sessionObject, IQ responseStanza, String[] features) {
				super(TYPE, sessionObject);
				this.stanza = responseStanza;
				this.features = features;
			}

			@Override
			protected void dispatch(ServerFeaturesReceivedHandler handler) {
				handler.onServerFeaturesReceived(sessionObject, stanza, features);
			}

		}

		void onServerFeaturesReceived(SessionObject sessionObject, IQ stanza, String[] features);
	}

	private final String[] FEATURES = { "http://jabber.org/protocol/disco#info" };

	private final XmppModulesManager modulesManager;

	public DiscoInfoModule(Context context, XmppModulesManager modulesManager) {
		super(context);
		this.modulesManager = modulesManager;
		infoRequestCallback = DEFAULT_REQUERST_CALLBACK;
	}

	public void discoverServerFeatures(final DiscoInfoAsyncCallback callback) throws JaxmppException {
		final DiscoInfoAsyncCallback diac = new DiscoInfoAsyncCallback(null) {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws JaxmppException {
				if (callback != null)
					callback.onError(responseStanza, error);
			}

			@Override
			protected void onInfoReceived(String node, Collection<Identity> identities, Collection<String> features)
					throws XMLException {
				HashSet<String> ff = new HashSet<String>();
				ff.addAll(features);
				context.getSessionObject().setProperty(SERVER_FEATURES_KEY, ff);

				final ServerFeaturesReceivedEvent event = new ServerFeaturesReceivedEvent(context.getSessionObject(),
						(IQ) this.responseStanza, ff.toArray(new String[] {}));
				fireEvent(event);
				if (callback != null)
					callback.onInfoReceived(node, identities, features);
			}

			@Override
			public void onTimeout() throws JaxmppException {
				if (callback != null)
					callback.onTimeout();
			}
		};

		JID jid = context.getSessionObject().getProperty(ResourceBinderModule.BINDED_RESOURCE_JID);
		if (jid != null)
			getInfo(JID.jidInstance(jid.getDomain()), null, (AsyncCallback) diac);
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
		if (jid != null)
			iq.setTo(jid);
		iq.setType(StanzaType.get);
		Element query = new DefaultElement("query", null, "http://jabber.org/protocol/disco#info");
		if (node != null)
			query.setAttribute("node", node);
		iq.addChild(query);

		write(iq, callback);
	}

	public void getInfo(JID jid, String node, DiscoInfoAsyncCallback callback) throws JaxmppException {
		getInfo(jid, node, (AsyncCallback) callback);
	}

	public static interface InfoRequestCallback {

		Identity getIdentity(SessionObject sessionObject, IQ request, String requestedNode);

		String[] getFeatures(SessionObject sessionObject, IQ request, String requestedNode);

	}

	private InfoRequestCallback infoRequestCallback;

	private class DefaultInfoRequestCallback implements InfoRequestCallback {

		@Override
		public Identity getIdentity(SessionObject sessionObject, IQ request, String requestedNode) {
			Identity identity = new Identity();

			String category = sessionObject.getProperty(IDENTITY_CATEGORY_KEY);
			String type = sessionObject.getProperty(IDENTITY_TYPE_KEY);
			String nme = sessionObject.getProperty(SoftwareVersionModule.NAME_KEY);
			identity.setCategory(category == null ? "client" : category);
			identity.setName(nme == null ? SoftwareVersionModule.DEFAULT_NAME_VAL : nme);
			identity.setType(type == null ? "pc" : type);

			return identity;
		}

		@Override
		public String[] getFeatures(SessionObject sessionObject, IQ request, String requestedNode) {
			return DiscoInfoModule.this.modulesManager.getAvailableFeatures().toArray(new String[] {});
		}
	}

	private final InfoRequestCallback DEFAULT_REQUERST_CALLBACK = new DefaultInfoRequestCallback();

	@Override
	protected void processGet(IQ element) throws XMPPException, XMLException, JaxmppException {
		Element query = element.getChildrenNS("query", "http://jabber.org/protocol/disco#info");
		final String requestedNode = query.getAttribute("node");

		final Identity identity = infoRequestCallback.getIdentity(context.getSessionObject(), element, requestedNode);
		final String[] features = infoRequestCallback.getFeatures(context.getSessionObject(), element, requestedNode);

		Element result = XmlTools.makeResult(element);

		Element queryResult = new DefaultElement("query", null, "http://jabber.org/protocol/disco#info");
		queryResult.setAttribute("node", requestedNode);
		result.addChild(queryResult);

		if (identity != null) {
			Element identityElement = new DefaultElement("identity");
			identityElement.setAttribute("category", identity.getCategory());
			identityElement.setAttribute("type", identity.getType());
			identityElement.setAttribute("name", identity.getName());
			queryResult.addChild(identityElement);
		}

		if (features != null)
			for (String feature : features) {
				DefaultElement f = new DefaultElement("feature");
				f.setAttribute("var", feature);
				queryResult.addChild(f);
			}

		write(result);
	}

	@Override
	protected void processSet(IQ element) throws XMPPException, XMLException, JaxmppException {
		throw new XMPPException(ErrorCondition.not_allowed);
	}

	public InfoRequestCallback getInfoRequestCallback() {
		return infoRequestCallback;
	}

	public void setInfoRequestCallback(InfoRequestCallback infoRequestCallback) {
		this.infoRequestCallback = infoRequestCallback == null ? DEFAULT_REQUERST_CALLBACK : infoRequestCallback;
	}

}