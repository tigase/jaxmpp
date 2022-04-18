
Examples
========

A very simple client in Groovy (sending and listening for message)
------------------------------------------------------------------

.. code:: groovy

   import tigase.jaxmpp.core.client.BareJID
   import tigase.jaxmpp.core.client.JID
   import tigase.jaxmpp.core.client.SessionObject
   import tigase.jaxmpp.core.client.exceptions.JaxmppException
   import tigase.jaxmpp.core.client.xmpp.modules.chat.Chat
   import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule
   import tigase.jaxmpp.core.client.xmpp.stanzas.Message
   import tigase.jaxmpp.j2se.Jaxmpp

   final Jaxmpp contact = new Jaxmpp()

   tigase.jaxmpp.j2se.Presence.initialize(contact);

   contact.getModulesManager().register(new MessageModule());

   contact.getProperties().setUserProperty(SessionObject.USER_BARE_JID, BareJID.bareJIDInstance("admin@atlantiscity"))
   contact.getProperties().setUserProperty(SessionObject.PASSWORD, "admin")

   contact.getEventBus().addHandler(MessageModule.MessageReceivedHandler.MessageReceivedEvent.class,
           new MessageModule.MessageReceivedHandler() {

               @Override
               public void onMessageReceived(SessionObject sessionObject, Chat chat, Message stanza) {
                   System.out.println("message: " + stanza.getBody());
               }
           });

   println("Loging in...");

   contact.login(true)

   if (contact.isConnected()) {
       contact.getModule(MessageModule.class).sendMessage(JID.jidInstance("tigase1@atlantiscity"), "Test", "This is a test")

       Thread.sleep(1 * 10 * 1000)

       contact.disconnect()
   }

A very simple client in Groovy (direct presence + presence listen)
------------------------------------------------------------------

It sends direct presence and then listens for 10 minutes for any presence changes.

.. code:: groovy

   import tigase.jaxmpp.core.client.BareJID
   import tigase.jaxmpp.core.client.JID
   import tigase.jaxmpp.core.client.SessionObject
   import tigase.jaxmpp.core.client.exceptions.JaxmppException
   import tigase.jaxmpp.core.client.xmpp.modules.chat.MessageModule
   import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule
   import tigase.jaxmpp.core.client.xmpp.stanzas.Presence
   import tigase.jaxmpp.j2se.Jaxmpp
   import tigase.jaxmpp.j2se.connectors.socket.SocketConnector

   final Jaxmpp contact = new Jaxmpp()

   tigase.jaxmpp.j2se.Presence.initialize(contact);
   tigase.jaxmpp.j2se.Roster.initialize(contact);

   contact.getModulesManager().register(new MessageModule());

   contact.getProperties().setUserProperty(SessionObject.USER_BARE_JID, BareJID.bareJIDInstance("admin@atlantiscity"))
   contact.getProperties().setUserProperty(SessionObject.PASSWORD, "admin")

   contact.getEventBus().addHandler(PresenceModule.ContactChangedPresenceHandler.ContactChangedPresenceEvent.class, new PresenceModule.ContactChangedPresenceHandler() {
       @Override
       void onContactChangedPresence(SessionObject sessionObject, Presence stanza, JID jid, Presence.Show show, String status, Integer priority) throws JaxmppException {
           System.out.println("Presence received:\t" + jid + " is now " + show + " (" + status + ")");

       }
   });

   println("Loging in...");

   contact.login(true)

   if (contact.isConnected()) {

       println("Sending direct presence")
       def presenceModule = contact.getModulesManager().getModule(PresenceModule.class);
       presenceModule.sentDirectPresence(JID.jidInstance("tigase2@atlantiscity/Psi+"), Presence.Show.away, "new status", 65);
       Thread.sleep(1 * 1000)

       println("Sending direct unavailable presence")
       presenceModule.sentDirectPresence(JID.jidInstance("tigase2@atlantiscity/Psi+"), Presence.Show.offline, "new status", 65);
       Thread.sleep(1 * 1000)

       println("Waiting for the presence for 10 minutes")

       Thread.sleep(10 * 60 * 1000)

       contact.disconnect()
   }
