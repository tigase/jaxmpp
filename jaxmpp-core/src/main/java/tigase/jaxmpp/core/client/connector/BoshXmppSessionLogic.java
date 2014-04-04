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
package tigase.jaxmpp.core.client.connector;

import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.SessionObject.Scope;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindEvent;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule.StreamFeaturesReceivedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.NonSaslAuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.auth.NonSaslAuthModule.NonSaslAuthEvent;
import tigase.jaxmpp.core.client.xmpp.modules.auth.SaslModule.SaslEvent;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule;

public class BoshXmppSessionLogic implements XmppSessionLogic {

	@Deprecated
	public static final String AUTHORIZED = "jaxmpp#authorized";

	private AuthModule authModule;

	private final Connector connector;

	private StreamFeaturesModule featuresModule;

	private final XmppModulesManager modulesManager;

	private ResourceBinderModule resourceBinder;

	private Listener<ResourceBindEvent> resourceBindListener;

	private final Listener<AuthModule.AuthEvent> saslEventListener;

	private SessionListener sessionListener;

	private final SessionObject sessionObject;

	private final Listener<StreamFeaturesReceivedEvent> streamFeaturesEventListener;

	private final PacketWriter writer;

	public BoshXmppSessionLogic(Connector connector, XmppModulesManager modulesManager, SessionObject sessionObject,
			PacketWriter writer) {
		this.connector = connector;
		this.modulesManager = modulesManager;
		this.sessionObject = sessionObject;
		this.writer = writer;

		this.streamFeaturesEventListener = new Listener<StreamFeaturesModule.StreamFeaturesReceivedEvent>() {

			@Override
			public void handleEvent(StreamFeaturesReceivedEvent be) throws JaxmppException {
				try {
					processStreamFeatures(be);
				} catch (JaxmppException e) {
					processException(e);
				}
			}
		};
		this.saslEventListener = new Listener<AuthModule.AuthEvent>() {

			@Override
			public void handleEvent(AuthModule.AuthEvent be) throws JaxmppException {
				try {
					if (be instanceof SaslEvent) {
						processSaslEvent((SaslEvent) be);
					} else if (be instanceof NonSaslAuthEvent) {
						processNonSaslEvent((NonSaslAuthEvent) be);
					}
				} catch (JaxmppException e) {
					processException(e);
				}
			}

		};
		this.resourceBindListener = new Listener<ResourceBindEvent>() {

			@Override
			public void handleEvent(ResourceBindEvent be) throws JaxmppException {
				try {
					processResourceBindEvent();
				} catch (JaxmppException e) {
					processException(e);
				}

			}
		};
	}

	@Override
	public void beforeStart() throws JaxmppException {
	}

	protected void processException(JaxmppException e) throws JaxmppException {
		if (sessionListener != null)
			sessionListener.onException(e);
	}

	protected void processNonSaslEvent(final NonSaslAuthModule.NonSaslAuthEvent be) throws JaxmppException {
		if (be.getType() == AuthModule.AuthFailed) {
			throw new JaxmppException("Unauthorized with condition=" + be.getError());
		} else if (be.getType() == AuthModule.AuthSuccess) {
			sessionObject.setProperty(Scope.stream, AUTHORIZED, Boolean.TRUE);
			connector.restartStream();
		}
	}

	public void processResourceBindEvent() throws JaxmppException {
		try {
			DiscoInfoModule discoInfo = this.modulesManager.getModule(DiscoInfoModule.class);
			if (discoInfo != null) {
				discoInfo.discoverServerFeatures(null);
			}

			RosterModule roster = this.modulesManager.getModule(RosterModule.class);
			if (roster != null) {
				roster.rosterRequest();
			}

			PresenceModule presence = this.modulesManager.getModule(PresenceModule.class);
			if (presence != null) {
				presence.sendInitialPresence();
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	protected void processSaslEvent(final SaslEvent be) throws JaxmppException {
		if (be.getType() == AuthModule.AuthFailed) {
			throw new JaxmppException("Unauthorized with condition=" + be.getError());
		} else if (be.getType() == AuthModule.AuthSuccess) {
			sessionObject.setProperty(Scope.stream, AUTHORIZED, Boolean.TRUE);
			connector.restartStream();
		}
	}

	protected void processStreamFeatures(StreamFeaturesReceivedEvent be) throws JaxmppException {
		try {
			if (sessionObject.getProperty(AUTHORIZED) != Boolean.TRUE) {
				authModule.login();
			} else if (sessionObject.getProperty(AUTHORIZED) == Boolean.TRUE) {
				resourceBinder.bind();
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setSessionListener(SessionListener sessionListener) throws JaxmppException {
		this.sessionListener = sessionListener;
		featuresModule = this.modulesManager.getModule(StreamFeaturesModule.class);
		authModule = this.modulesManager.getModule(AuthModule.class);
		resourceBinder = this.modulesManager.getModule(ResourceBinderModule.class);

		featuresModule.addListener(StreamFeaturesModule.StreamFeaturesReceived, streamFeaturesEventListener);
		authModule.addListener(AuthModule.AuthSuccess, this.saslEventListener);
		authModule.addListener(AuthModule.AuthFailed, this.saslEventListener);
		resourceBinder.addListener(ResourceBinderModule.ResourceBindSuccess, resourceBindListener);
	}

	@Override
	public void unbind() throws JaxmppException {
		featuresModule.removeListener(StreamFeaturesModule.StreamFeaturesReceived, streamFeaturesEventListener);
		authModule.removeListener(AuthModule.AuthSuccess, this.saslEventListener);
		authModule.removeListener(AuthModule.AuthFailed, this.saslEventListener);
		resourceBinder.removeListener(ResourceBinderModule.ResourceBindSuccess, resourceBindListener);

	}

}