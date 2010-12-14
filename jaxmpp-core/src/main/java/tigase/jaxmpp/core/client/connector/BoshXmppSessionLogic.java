package tigase.jaxmpp.core.client.connector;

import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.XmppSessionLogic;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindEvent;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule;
import tigase.jaxmpp.core.client.xmpp.modules.StreamFeaturesModule.StreamFeaturesReceivedEvent;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslModule;
import tigase.jaxmpp.core.client.xmpp.modules.sasl.SaslModule.SaslEvent;

public class BoshXmppSessionLogic implements XmppSessionLogic {

	public static final String AUTHORIZED = "jaxmpp#authorized";

	private final Connector connector;

	private StreamFeaturesModule featuresModule;

	private final XmppModulesManager modulesManager;

	private ResourceBinderModule resourceBinder;

	private Listener<ResourceBindEvent> resourceBindListener;

	private final Listener<SaslEvent> saslEventListener;

	private SaslModule saslModule;

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
			public void handleEvent(StreamFeaturesReceivedEvent be) {
				try {
					processStreamFeatures(be);
				} catch (JaxmppException e) {
					processException(e);
				}
			}
		};
		this.saslEventListener = new Listener<SaslEvent>() {

			@Override
			public void handleEvent(SaslEvent be) {
				try {
					processSaslEvent(be);
				} catch (JaxmppException e) {
					processException(e);
				}
			}

		};
		this.resourceBindListener = new Listener<ResourceBindEvent>() {

			@Override
			public void handleEvent(ResourceBindEvent be) {
				try {
					processResourceBindEvent(be);
				} catch (JaxmppException e) {
					processException(e);
				}

			}
		};
	}

	@Override
	public void bind(SessionListener sessionListener) throws JaxmppException {
		this.sessionListener = sessionListener;
		featuresModule = this.modulesManager.getModule(StreamFeaturesModule.class);
		saslModule = this.modulesManager.getModule(SaslModule.class);
		resourceBinder = this.modulesManager.getModule(ResourceBinderModule.class);

		featuresModule.addListener(StreamFeaturesModule.StreamFeaturesReceived, streamFeaturesEventListener);
		saslModule.addListener(SaslModule.SaslSuccess, this.saslEventListener);
		saslModule.addListener(SaslModule.SaslFailed, this.saslEventListener);
		resourceBinder.addListener(ResourceBinderModule.ResourceBindSuccess, resourceBindListener);
	}

	protected void processException(JaxmppException e) {
		if (sessionListener != null)
			sessionListener.onException(e);
	}

	protected void processResourceBindEvent(ResourceBindEvent be) throws JaxmppException {
		try {
			RosterModule roster = this.modulesManager.getModule(RosterModule.class);
			roster.rosterRequest();

			PresenceModule presence = this.modulesManager.getModule(PresenceModule.class);
			presence.sendInitialPresence();
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	protected void processSaslEvent(SaslEvent be) throws JaxmppException {
		try {
			if (be.getType() == SaslModule.SaslFailed) {
				throw new JaxmppException("Unauthorized with condition=" + be.getError());
			} else if (be.getType() == SaslModule.SaslSuccess) {
				sessionObject.setProperty(AUTHORIZED, Boolean.TRUE);
				connector.restartStream();
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	protected void processStreamFeatures(StreamFeaturesReceivedEvent be) throws JaxmppException {
		try {
			if (sessionObject.getProperty(AUTHORIZED) != Boolean.TRUE) {
				saslModule.login();
			} else if (sessionObject.getProperty(AUTHORIZED) == Boolean.TRUE) {
				resourceBinder.bind();
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unbind() throws JaxmppException {
		featuresModule.removeListener(StreamFeaturesModule.StreamFeaturesReceived, streamFeaturesEventListener);
		saslModule.removeListener(SaslModule.SaslSuccess, this.saslEventListener);
		saslModule.removeListener(SaslModule.SaslFailed, this.saslEventListener);
		resourceBinder.removeListener(ResourceBinderModule.ResourceBindSuccess, resourceBindListener);

	}

}
