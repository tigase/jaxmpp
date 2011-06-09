package tigase.jaxmpp.j2se;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.Connector;
import tigase.jaxmpp.core.client.Connector.ConnectorEvent;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.connector.AbstractBoshConnector;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.observer.Listener;
import tigase.jaxmpp.core.client.observer.Observable;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.forms.JabberDataElement;
import tigase.jaxmpp.core.client.xmpp.forms.TextSingleField;
import tigase.jaxmpp.core.client.xmpp.forms.XDataType;
import tigase.jaxmpp.core.client.xmpp.modules.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.MessageModule.MessageEvent;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule;
import tigase.jaxmpp.core.client.xmpp.modules.ResourceBinderModule.ResourceBindEvent;
import tigase.jaxmpp.core.client.xmpp.modules.SoftwareVersionModule;
import tigase.jaxmpp.core.client.xmpp.modules.SoftwareVersionModule.SoftwareVersionAsyncCallback;
import tigase.jaxmpp.core.client.xmpp.modules.adhoc.AdHocCommand;
import tigase.jaxmpp.core.client.xmpp.modules.adhoc.AdHocCommansModule;
import tigase.jaxmpp.core.client.xmpp.modules.adhoc.AdHocRequest;
import tigase.jaxmpp.core.client.xmpp.modules.adhoc.AdHocResponse;
import tigase.jaxmpp.core.client.xmpp.modules.adhoc.State;
import tigase.jaxmpp.core.client.xmpp.modules.auth.AuthModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule.DiscoInfoAsyncCallback;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoInfoModule.Identity;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoItemsModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoItemsModule.DiscoItemsAsyncCallback;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoItemsModule.Item;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.PresenceEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence.Show;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

public class Test {

	public static void $main(String[] args) throws Exception {
		Logger logger = Logger.getLogger("tigase.jaxmpp");
		// create a ConsoleHandler
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
		logger.setLevel(Level.ALL);

		System.out.println(Test.class.getName());

		if (logger.isLoggable(Level.CONFIG))
			logger.config("Logger successfully initialized");

		Jaxmpp jaxmpp = new Jaxmpp();
		// for BOSH connector
		jaxmpp.getProperties().setUserProperty(AbstractBoshConnector.BOSH_SERVICE_URL_KEY, "http://xmpp.tigase.org");
		// jaxmpp.getProperties().setUserProperty(AbstractBoshConnector.BOSH_SERVICE_URL,
		// "http://messenger.tigase.org:80/bosh");

		// for Socket connector
		jaxmpp.getProperties().setUserProperty(AuthModule.FORCE_NON_SASL, Boolean.TRUE);
		jaxmpp.getProperties().setUserProperty(SocketConnector.SERVER_HOST, "xmpp.tigase.org");
		// port value is not necessary. Default is 5222
		jaxmpp.getProperties().setUserProperty(SocketConnector.SERVER_PORT, 5222);

		// "bosh" and "socket" values available
		jaxmpp.getProperties().setUserProperty(Jaxmpp.CONNECTOR_TYPE, "socket");

		jaxmpp.getProperties().setUserProperty(SessionObject.RESOURCE, "jaxmpp");
		jaxmpp.getProperties().setUserProperty(SessionObject.USER_JID, JID.jidInstance(args[0]));
		jaxmpp.getProperties().setUserProperty(SessionObject.PASSWORD, args[1]);

		Observable observable = new Observable(null);
		observable.addListener(ResourceBinderModule.ResourceBindSuccess,
				new Listener<ResourceBinderModule.ResourceBindEvent>() {

					@Override
					public void handleEvent(ResourceBindEvent be) {

					}
				});
		observable.fireEvent(new ResourceBinderModule.ResourceBindEvent(ResourceBinderModule.ResourceBindSuccess));

		System.out.println("// login");
		// not necessary. it allows to set own status on sending initial
		// presence

		jaxmpp.getModulesManager().getModule(ResourceBinderModule.class).addListener(ResourceBinderModule.ResourceBindSuccess,
				new Listener<ResourceBinderModule.ResourceBindEvent>() {

					@Override
					public void handleEvent(ResourceBindEvent be) {
						System.out.println("Binded as " + be.getJid());
					}
				});
		jaxmpp.getModulesManager().getModule(PresenceModule.class).addListener(PresenceModule.BeforeInitialPresence,
				new Listener<PresenceEvent>() {

					@Override
					public void handleEvent(PresenceEvent be) {
						// be.cancel();
						be.setPriority(0);
						be.setStatus("jaxmpp2 based Bot!");
						be.setShow(Show.online);
					}
				});

		// listener of incoming messages
		jaxmpp.getModulesManager().getModule(MessageModule.class).addListener(MessageModule.MessageReceived,
				new Listener<MessageModule.MessageEvent>() {

					@Override
					public void handleEvent(MessageEvent be) {
						try {
							System.out.println("Received message: " + be.getMessage().getAsString());
						} catch (XMLException e) {
							e.printStackTrace();
						}
					}
				});

		jaxmpp.addListener(Connector.Error, new Listener<Connector.ConnectorEvent>() {

			@Override
			public void handleEvent(ConnectorEvent be) throws JaxmppException {
				(new Exception()).printStackTrace();
				System.out.println(be.getStreamError() + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				System.out.println(be.getStanza().getAsString());
			}
		});

		final long t1 = System.currentTimeMillis();
		jaxmpp.login(true);
		System.out.println(" CONNECTED; secure=" + jaxmpp.isSecure());

		// ping example
		IQ pingIq = IQ.create();
		pingIq.setTo(JID.jidInstance("example.com"));
		pingIq.setType(StanzaType.get);
		pingIq.addChild(new DefaultElement("ping", null, "urn:xmpp:ping"));
		jaxmpp.send(pingIq, new AsyncCallback() {

			@Override
			public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
				System.out.println("Ping Error response " + error);
			}

			@Override
			public void onSuccess(Stanza responseStanza) throws XMLException {
				System.out.println("PONG");
			}

			@Override
			public void onTimeout() throws XMLException {
				System.out.println("No ping response");
			}
		});

		jaxmpp.getModulesManager().getModule(DiscoItemsModule.class).getItems(JID.jidInstance("example.com"),
				new DiscoItemsAsyncCallback() {

					@Override
					public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
						System.out.println(" error " + error);
					}

					@Override
					public void onInfoReceived(String attribute, ArrayList<Item> items) throws XMLException {
						for (Item string : items) {
							System.out.println(" ITEM: " + string.getName());
						}
					}

					@Override
					public void onTimeout() throws XMLException {
						System.out.println(" timeout");
					}
				});
		jaxmpp.getModulesManager().getModule(DiscoInfoModule.class).getInfo(JID.jidInstance("example.com"),
				new DiscoInfoAsyncCallback() {

					@Override
					public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
						System.out.println("disco info error " + error);
					}

					@Override
					protected void onInfoReceived(String node, Collection<Identity> identities, Collection<String> features)
							throws XMLException {
						for (Identity i : identities) {
							System.out.println("ID: " + i.getType() + " " + i.getCategory() + " " + i.getName());
						}
						System.out.println(" FEATURES: " + features.toString());
					}

					@Override
					public void onTimeout() throws XMLException {
						System.out.println("disco info timeout");
					}
				});
		jaxmpp.getModulesManager().getModule(SoftwareVersionModule.class).checkSoftwareVersion(JID.jidInstance("example.com"),
				new SoftwareVersionAsyncCallback() {

					@Override
					public void onError(Stanza responseStanza, ErrorCondition error) throws XMLException {
						System.out.println("Version Error response " + error);
					}

					@Override
					public void onTimeout() throws XMLException {
						System.out.println("No version response");

					}

					@Override
					protected void onVersionReceived(String name, String version, String os) {
						System.out.println("Version: " + name + ", " + version + ", " + os);

					}
				});
		jaxmpp.getModulesManager().getModule(AdHocCommansModule.class).register(new AdHocCommand() {

			@Override
			public String[] getFeatures() {
				return new String[] { "http://jabber.org/protocol/commands", "jabber:x:data" };
			}

			@Override
			public String getName() {
				return "Hello world";
			}

			@Override
			public String getNode() {
				return "hello-world";
			}

			@Override
			public void handle(AdHocRequest request, AdHocResponse response) throws JaxmppException {
				try {
					if (request.getForm() == null) {
						JabberDataElement e = new JabberDataElement(XDataType.form);
						e.setTitle("Hello World Command");
						e.addTextSingleField("name", null).setLabel("Nickname");
						response.setForm(e);
						response.setState(State.executing);
					} else {
						TextSingleField name = request.getForm().getField("name");
						JabberDataElement e = new JabberDataElement(XDataType.result);

						e.setTitle("Hello World Command Result");
						e.addFixedField("Hello " + name.getFieldValue() + "!");

						response.setForm(e);
					}

				} catch (Exception e) {
					throw new JaxmppException(e);
				}
			}

			@Override
			public boolean isAllowed(JID jid) {
				return false;
			}
		});

		Thread.sleep(1000 * 120);

		// jaxmpp.getPresence().setPresence(null, "Bot changed status", 1);

		// jaxmpp.sendMessage(JID.jidInstance("bmalkow@malkowscy.net"), "Test",
		// "Wiadomosc ");

		// Thread.sleep(1000 * 15);

		System.out.println("// disconnect");
		jaxmpp.disconnect();
		final long t2 = System.currentTimeMillis();

		System.out.println(". " + (t2 - t1) + " ms");
	}

	public static void main(String[] args) {
		System.out.println((int) '\32');
	}
}
