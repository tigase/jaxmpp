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
package tigase.jaxmpp.j2se.connectors.socket;

import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.BaseEvent;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule.StreamFeaturesReceivedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.registration.InBandRegistrationModule;

public class SocketInBandRegistrationXmppSessionLogic implements XmppSessionLogic {

	private final SocketConnector connector;

	private StreamFeaturesModule featuresModule;

	private final XmppModulesManager modulesManager;

	private final Listener<BaseEvent> registrationListener;

	private InBandRegistrationModule registrationModule;

	private SessionListener sessionListener;

	private final SessionObject sessionObject;

	private final Listener<StreamFeaturesReceivedEvent> streamFeaturesEventListener;

	public SocketInBandRegistrationXmppSessionLogic(SocketConnector connector, XmppModulesManager modulesManager,
			SessionObject sessionObject, PacketWriter writer) {
		this.connector = connector;
		this.modulesManager = modulesManager;
		this.sessionObject = sessionObject;

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
		registrationListener = new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) throws JaxmppException {
				if (be.getType() == InBandRegistrationModule.ReceivedError) {
					SocketInBandRegistrationXmppSessionLogic.this.connector.stop();
				} else if (be.getType() == InBandRegistrationModule.ReceivedTimeout) {
					SocketInBandRegistrationXmppSessionLogic.this.connector.stop();
				} else if (be.getType() == InBandRegistrationModule.NotSupportedError) {
					SocketInBandRegistrationXmppSessionLogic.this.connector.stop();
				}
			}
		};
	}

	@Override
	public void beforeStart() throws JaxmppException {
		// TODO Auto-generated method stub

	}

	@Override
	public void bind(SessionListener sessionListener) throws JaxmppException {
		this.sessionListener = sessionListener;
		featuresModule = this.modulesManager.getModule(StreamFeaturesModule.class);
		registrationModule = this.modulesManager.getModule(InBandRegistrationModule.class);

		featuresModule.addListener(StreamFeaturesModule.StreamFeaturesReceived, streamFeaturesEventListener);
		registrationModule.addListener(this.registrationListener);
	}

	protected void processException(JaxmppException e) throws JaxmppException {
		if (sessionListener != null)
			sessionListener.onException(e);
	}

	protected void processStreamFeatures(StreamFeaturesReceivedEvent be) throws JaxmppException {
		System.out.println(be.getFeatures().getAsString());
		try {
			final Boolean tlsDisabled = sessionObject.getProperty(SocketConnector.TLS_DISABLED_KEY);
			final boolean tlsAvailable = SocketConnector.isTLSAvailable(sessionObject);

			final boolean isConnectionSecure = connector.isSecure();

			if (!isConnectionSecure && tlsAvailable && (tlsDisabled == null || !tlsDisabled)) {
				connector.startTLS();
			} else {
				registrationModule.start();
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unbind() throws JaxmppException {
		featuresModule.removeListener(StreamFeaturesModule.StreamFeaturesReceived, streamFeaturesEventListener);
		registrationModule.removeListener(this.registrationListener);
	}

}