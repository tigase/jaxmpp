package test;

import org.whispersystems.libsignal.*;
import org.whispersystems.libsignal.protocol.CiphertextMessage;
import org.whispersystems.libsignal.state.*;
import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;
import org.whispersystems.libsignal.util.KeyHelper;
import tigase.jaxmpp.core.client.Base64;
import tigase.jaxmpp.core.client.*;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.PingModule;
import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat;
import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule;
import tigase.jaxmpp.core.client.xmpp.modules.omemo.Bundle;
import tigase.jaxmpp.core.client.xmpp.modules.omemo.JaXMPPSignalProtocolStore;
import tigase.jaxmpp.core.client.xmpp.modules.omemo.OmemoModule;
import tigase.jaxmpp.core.client.xmpp.modules.omemo.XmppOMEMOSession;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.modules.pubsub.PubSubErrorCondition;
import tigase.jaxmpp.core.client.xmpp.modules.pubsub.PubSubModule;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;
import tigase.jaxmpp.j2se.ConnectionConfiguration;
import tigase.jaxmpp.j2se.Jaxmpp;
import tigase.jaxmpp.j2se.Presence;
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static tigase.jaxmpp.core.client.xmpp.modules.omemo.OmemoModule.DEVICELIST_NODE;

public class Client {

	//	public static String TALK_TO_JID = "bartosz.malkowski@tigase.org";
	public static String TALK_TO_JID = "+48508245952@quicksy.im";
//	public static String TALK_TO_JID = "andrzej.wojcik@tigase.org";

	static HashMap<SignalProtocolAddress, SessionCipher> sessionCiphers = new HashMap<>();

	private static void createOMEMOSession(Jaxmpp jaxmpp, BareJID jid) throws JaxmppException {
		jaxmpp.getModule(OmemoModule.class).createOMEMOSession(jid, new OmemoModule.CreateOMEMOSessionHandler() {
			@Override
			public void onError() {
				System.out.println("!!! ERROR !!!!");
			}

			@Override
			public void onSessionCreated(XmppOMEMOSession session) {
				System.out.println("Session created: " + session);
			}
		});

	}

	private static void genKeys(Jaxmpp jaxmpp) throws Exception {
		IdentityKeyPair identityKeyPair = KeyHelper.generateIdentityKeyPair();
		int registrationId = KeyHelper.generateRegistrationId(true);
		List<PreKeyRecord> preKeys = KeyHelper.generatePreKeys(1, 100);
		SignedPreKeyRecord signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, 1);

		Properties props = new Properties();
		props.setProperty("account", jaxmpp.getSessionObject().getUserBareJid().toString());
		props.setProperty("password", jaxmpp.getSessionObject().getProperty(SessionObject.PASSWORD));

		props.setProperty("identityKeyPair", Base64.encode(identityKeyPair.serialize()));
		props.setProperty("registrationId", String.valueOf(registrationId));

		for (int i = 0; i < preKeys.size(); i++) {
			props.setProperty("preKey." + preKeys.get(i).getId(), Base64.encode(preKeys.get(i).serialize()));
		}

		props.setProperty("signedPreKey", Base64.encode(signedPreKey.serialize()));
		props.store(System.out, "OMEMO Client credentials");
	}

	private static void getKeys(Jaxmpp jaxmpp, BareJID jid) throws JaxmppException {
		jaxmpp.getModule(OmemoModule.class).getKeys(jid, null);
	}

	private static JaXMPPSignalProtocolStore loadMyKeys(Jaxmpp jaxmpp, final Properties props) throws Exception {
		if (!props.containsKey("registrationId")) {
			return null;
		}

		System.out.println("Loading OMEMO keys...");
		IdentityKeyPair identityKeyPair = new IdentityKeyPair(Base64.decode(props.getProperty("identityKeyPair")));
		int registrationId = Integer.valueOf(props.getProperty("registrationId"));
		SignedPreKeyRecord signedPreKeyRecord = new SignedPreKeyRecord(
				Base64.decode(props.getProperty("signedPreKey")));
		List<PreKeyRecord> preKeys = new ArrayList<>();

		Enumeration<String> names = (Enumeration<String>) props.propertyNames();
		while (names.hasMoreElements()) {
			String n = names.nextElement();
			if (n.startsWith("preKey.")) {
				preKeys.add(new PreKeyRecord(Base64.decode(props.getProperty(n))));
			}
		}

		final InMemeoryStore store = new InMemeoryStore(identityKeyPair, registrationId) {

			@Override
			public void storeSession(SignalProtocolAddress address, SessionRecord record) {
				System.out.println("!!!!! storeSession()");
				super.storeSession(address, record);
			}

			@Override
			public boolean containsSession(SignalProtocolAddress address) {
				System.out.println("!!!!! containsSession(" + address + ")");
				return super.containsSession(address);
			}

			@Override
			public SessionRecord loadSession(SignalProtocolAddress address) {
				System.out.println("!!!!! loadSession(" + address + ")");
				return super.loadSession(address);
			}

			@Override
			public boolean isTrustedIdentity(SignalProtocolAddress address, IdentityKey identityKey,
											 Direction direction) {
				System.out.println("!!!!! isTrustedIdentity()");
				return super.isTrustedIdentity(address, identityKey, direction);
			}
		};

		preKeys.forEach(preKeyRecord -> store.storePreKey(preKeyRecord.getId(), preKeyRecord));
		store.storeSignedPreKey(signedPreKeyRecord.getId(), signedPreKeyRecord);

		System.out.println(signedPreKeyRecord.getId() + "      " + registrationId);

		return store;
	}

	public static void main(String[] args) throws Exception {
		Logger logger = Logger.getLogger("tigase.jaxmpp");
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
		logger.setLevel(Level.ALL);


		final Jaxmpp jaxmpp = new Jaxmpp();
		jaxmpp.getSessionObject()
				.setProperty(SessionObject.Scope.user, SocketConnector.COMPRESSION_DISABLED_KEY, Boolean.TRUE);
		Presence.initialize(jaxmpp);
		jaxmpp.getModulesManager().register(new PresenceModule());
		jaxmpp.getModulesManager().register(new RosterModule());
		jaxmpp.getModulesManager().register(new PubSubModule());
		jaxmpp.getModulesManager().register(new MessageModule());
		jaxmpp.getModulesManager().register(new OmemoModule());
		jaxmpp.getModulesManager().register(new PingModule());

		jaxmpp.getModule(MessageModule.class).addExtension(jaxmpp.getModule(OmemoModule.class).getExtension());

		final Properties props = new Properties();
		try (FileReader reader = new FileReader(args[0])) {
			props.load(reader);
		}

		JaXMPPSignalProtocolStore store = loadMyKeys(jaxmpp, props);

		System.out.println();
//		System.out.println("HASH:      " + Hex.encode(store.getIdentityKeyPair().getPublicKey().serialize(), 1));
//		System.out.println("DEVICE ID: " + store.getLocalRegistrationId());
		System.out.println();
		System.out.println();

		ConnectionConfiguration configuration = jaxmpp.getConnectionConfiguration();
		configuration.setUserJID(props.getProperty("account"));
		configuration.setUserPassword(props.getProperty("password"));
		configuration.setResource("bot");
//		configuration.setDisableTLS(true);

//		jaxmpp.getSessionObject().setProperty(SocketConnector.SERVER_HOST, "34.216.98.42");
//		jaxmpp.getSessionObject().setProperty(SessionObject.DOMAIN_NAME,"xmpp.tigase.tech");
		// 52.10.82.152

		OmemoModule.setSignalProtocolStore(jaxmpp.getSessionObject(), store);

		jaxmpp.getEventBus()
				.addHandler(Connector.StanzaSendingHandler.StanzaSendingEvent.class,
							(sessionObject, stanza) -> System.out.println("<< " + stanza.getAsString()));
		jaxmpp.getEventBus()
				.addHandler(Connector.StanzaReceivedHandler.StanzaReceivedEvent.class, (sessionObject, stanza) -> {
					try {
						System.out.println(">> " + stanza.getAsString());
					} catch (XMLException e) {
						e.printStackTrace();
					}
				});

		jaxmpp.getEventBus()
				.addHandler(MessageModule.MessageReceivedHandler.MessageReceivedEvent.class,
							(sessionObject, chat, stanza) -> {
								if (chat != null) {
									try {
										if (stanza.getBody() != null) {
											System.out.println("CHAT " + (chat != null ? chat.getJid() : "-") + " :: " +
																	   stanza.getBody());
										}
									} catch (XMLException e) {
										e.printStackTrace();
									}
								}
							});

		jaxmpp.login(true);

		System.out.println("Connected=" + jaxmpp.isConnected());

		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
			String line;
			while ((line = br.readLine()) != null) {
				if ("stop".equals(line.trim())) {
					System.out.println("Stopping...");
					break;
				} else if (line.startsWith("ping")) {
					String[] p = line.split(" ");
					JID jid = p.length > 1 ? JID.jidInstance(p[1]) : null;
					ping(jaxmpp, jid);
				} else if ("subscribe".equals(line.trim())) {
					String p = line.split(" ")[1];
					jaxmpp.getModule(PresenceModule.class).subscribe(JID.jidInstance(p));
					jaxmpp.getModule(PresenceModule.class).subscribed(JID.jidInstance(p));
				} else if (line.startsWith("msg ")) {
					sendMessage(jaxmpp, line.substring(4).trim());
				} else if (line.startsWith("omemo get ")) {
					// omemo get +48508245952@quicksy.im
					String p = line.split(" ")[2];
					System.out.println("Getting keys for " + p);
					getKeys(jaxmpp, BareJID.bareJIDInstance(p));
				} else if (line.startsWith("omemo msg ")) {
					// omemo get +48508245952@quicksy.im
					String m = line.substring(10);
					sendMessage(jaxmpp, BareJID.bareJIDInstance(TALK_TO_JID), m);
				} else if ("omemo gen".equals(line.trim())) {
					System.out.println("Generating OMEMO keys");
					genKeys(jaxmpp);
				} else if ("omemo publish".equals(line.trim())) {
					System.out.println("Publishing own support");
					publishSupport(jaxmpp);
				} else if (line.startsWith("omemo sub")) {
					String p = line.split(" ")[2];
					System.out.println("Subscribing for device lists of " + p);
					subscribeDeviceList(jaxmpp, p);
				} else if (line.startsWith("omemo session")) {
					String[] prms = line.split(" ");

					BareJID jid =
							prms.length == 3 ? BareJID.bareJIDInstance(prms[2]) : BareJID.bareJIDInstance(TALK_TO_JID);
					createOMEMOSession(jaxmpp, jid);
				} else if (line.startsWith("chat ")) {
					String m = line.substring(5);
					sendChatMessage(jaxmpp, BareJID.bareJIDInstance(TALK_TO_JID), m);
				} else if (line.equals("list")) {
					jaxmpp.getModule(PubSubModule.class)
							.retrieveSubscription(null, DEVICELIST_NODE,
												  new PubSubModule.SubscriptionsRetrieveAsyncCallback() {
													  @Override
													  public void onTimeout() throws JaxmppException {
														  System.err.println("TIMEOUT");
													  }

													  @Override
													  protected void onRetrieve(IQ response, String node,
																				Collection<PubSubModule.SubscriptionElement> subscriptions) {
														  for (PubSubModule.SubscriptionElement subscription : subscriptions) {
															  try {
																  System.out.println(
																		  "SUBSCRIPTION: " + subscription.getSubID());
															  } catch (XMLException e) {
																  e.printStackTrace();
															  }
														  }
													  }

													  @Override
													  protected void onEror(IQ response,
																			XMPPException.ErrorCondition errorCondition,
																			PubSubErrorCondition pubSubErrorCondition)
															  throws JaxmppException {
														  System.err.println(
																  "ERR " + errorCondition + " " + pubSubErrorCondition);
													  }
												  });
				} else if (line.equals("unsub")) {
					Arrays.asList(DEVICELIST_NODE, "eu.siacs.conversations.axolotl.bundles:1422845261",
								  "eu.siacs.conversations.axolotl.bundles:428200763",
								  "eu.siacs.conversations.axolotl.bundles:2136727595",
								  "eu.siacs.conversations.axolotl.bundles:186505182",
								  "eu.siacs.conversations.axolotl.bundles:186505182",
								  "eu.siacs.conversations.axolotl.bundles:927740648",
								  "eu.siacs.conversations.axolotl.bundles:927740648",
								  "eu.siacs.conversations.axolotl.bundles:871751269",
								  "eu.siacs.conversations.axolotl.devicelist",
								  "eu.siacs.conversations.axolotl.bundles:1810744316",
								  "eu.siacs.conversations.axolotl.bundles:675490094",
								  "eu.siacs.conversations.axolotl.bundles:812230292").forEach(node -> {
						try {
							jaxmpp.getModule(PubSubModule.class).deleteNode(null, node, new AsyncCallback() {
								@Override
								public void onError(Stanza responseStanza, XMPPException.ErrorCondition error)
										throws JaxmppException {
									System.out.println("error: " + error);
								}

								@Override
								public void onSuccess(Stanza responseStanza) throws JaxmppException {
									System.out.println("Unsubscribed");
								}

								@Override
								public void onTimeout() throws JaxmppException {
									System.out.println("TImeout");
								}
							});
						} catch (JaxmppException e) {
							e.printStackTrace();
						}
					});

				}
			}
		}

		jaxmpp.disconnect(true);
		System.out.println(".");

	}

	private static void ping(Jaxmpp jaxmpp, JID jid) throws JaxmppException {
		jaxmpp.getModule(PingModule.class).ping(jid, new PingModule.PingAsyncCallback() {
			@Override
			public void onError(Stanza responseStanza, XMPPException.ErrorCondition error) throws JaxmppException {
				System.out.println("PING ERROR: " + error);

			}

			@Override
			public void onTimeout() throws JaxmppException {
				System.out.println("PING TIMEOUT");
			}

			@Override
			protected void onPong(long time) {
				System.out.println("PONG: " + time + " ms");
			}
		});
	}

	private static void publishSupport(final Jaxmpp jaxmpp) throws JaxmppException, InvalidKeyIdException {
		jaxmpp.getModule(OmemoModule.class).publishDeviceList();
	}

	private static void sendChatMessage(Jaxmpp jaxmpp, BareJID jid, String msg) throws JaxmppException {
		final MessageModule messageModule = jaxmpp.getModule(MessageModule.class);
		Chat chat;
		if (messageModule.getChatManager().isChatOpenFor(jid)) {
			System.out.println("OTWARTY");
			chat = messageModule.getChatManager().getChat(JID.jidInstance(jid), null);
		} else {
			System.out.println("CREATE CHAT");
			chat = messageModule.createChat(JID.jidInstance(jid));
		}

		if (chat == null) {
			System.out.println("WTF KURWA?");
		}

		messageModule.sendMessage(chat, msg);
	}

	private static void sendMessage(Jaxmpp jaxmpp, BareJID jid, String msg)
			throws UntrustedIdentityException, JaxmppException {
		final Random rnd = new SecureRandom();

		final SignalProtocolStore store = OmemoModule.getSignalProtocolStore(jaxmpp.getSessionObject());

		// TODO get devices ID of jid

		jaxmpp.getModule(OmemoModule.class).getKeys(jid, new OmemoModule.KeysRetrieverHandler() {
			@Override
			public void onError() {
				System.err.println("JEBLO!");
			}

			@Override
			public void onSuccess(List<Bundle> bundles) {
				try {
					for (Bundle b : bundles) {
						PreKeyBundle bundle = b.getPreKeyBundle();
						List<PreKeyBundle> preKeys = b.getPreKeys();
						int deviceid = b.getDeviceId();

						PreKeyBundle preKey = preKeys.get(rnd.nextInt(preKeys.size()));

						final SignalProtocolAddress address = new SignalProtocolAddress(jid.toString(), deviceid);

						final PreKeyBundle preKeyBundle = new PreKeyBundle(0, address.getDeviceId(),
																		   preKey.getPreKeyId(), preKey.getPreKey(),
																		   bundle.getSignedPreKeyId(),
																		   bundle.getSignedPreKey(),
																		   bundle.getSignedPreKeySignature(),
																		   bundle.getIdentityKey());

						SessionBuilder sb = new SessionBuilder(store, store, store, store, address);
						sb.process(preKeyBundle);

						SessionCipher sessionCipher = new SessionCipher(store, store, store, store, address);

						sessionCiphers.put(address, sessionCipher);

						//

						CiphertextMessage enc = sessionCipher.encrypt("To jest test!".getBytes());

						System.out.println("! " + b.getDeviceId() + " > " + enc);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	private static void sendMessage(Jaxmpp jaxmpp, String line) throws JaxmppException {
		int p = line.indexOf(" ");
		JID jid = JID.jidInstance(line.substring(0, p));
		String msg = line.substring(p + 1);

		Message m = Stanza.createMessage();
		m.setBody(msg);
		m.setTo(jid);
		m.setType(StanzaType.chat);
		m.setId(UIDGenerator.next());

		jaxmpp.getModule(MessageModule.class).sendMessage(m);
	}

	private static void subscribeDeviceList(Jaxmpp jaxmpp, String p) throws JaxmppException {
		jaxmpp.getModule(OmemoModule.class).subscribeForDeviceList(BareJID.bareJIDInstance(p));
	}

	private static class InMemeoryStore
			extends InMemorySignalProtocolStore
			implements JaXMPPSignalProtocolStore {

		private final Map<BareJID, XmppOMEMOSession> sesssions = new ConcurrentHashMap<>();
		private final Map<Integer, byte[]> store = new HashMap<>();

		public InMemeoryStore(IdentityKeyPair identityKeyPair, int registrationId) {
			super(identityKeyPair, registrationId);
		}

		@Override
		public XmppOMEMOSession getSession(BareJID jid) {
			return this.sesssions.get(jid);
		}

		@Override
		public void storeSession(XmppOMEMOSession session) {
			this.sesssions.put(session.getJid(), session);
		}

		@Override
		public boolean isOMEMORequired(BareJID jid) {
			return false;
		}

		@Override
		public void setOMEMORequired(BareJID jid, boolean required) {

		}

		@Override
		public PreKeyRecord loadPreKey(int preKeyId) throws InvalidKeyIdException {
			try {
				if (!store.containsKey(preKeyId)) {
					throw new InvalidKeyIdException("No such prekeyrecord!");
				}

				return new PreKeyRecord(store.get(preKeyId));
			} catch (IOException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public void storePreKey(int preKeyId, PreKeyRecord record) {
			store.put(preKeyId, record.serialize());
		}

		@Override
		public boolean containsPreKey(int preKeyId) {
			return store.containsKey(preKeyId);
		}

		@Override
		public void removePreKey(int preKeyId) {
			store.remove(preKeyId);
		}

		@Override
		public List<PreKeyRecord> loadPreKeys() {
			try {
				List<PreKeyRecord> results = new LinkedList<>();

				for (byte[] serialized : store.values()) {
					results.add(new PreKeyRecord(serialized));
				}

				return results;
			} catch (IOException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public List<Integer> getSubDevice(String name) {
			return null;
		}
	}

}
