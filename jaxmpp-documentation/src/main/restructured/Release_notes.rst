**JaXMPP V3.2.0 Release Notes**

Tigase JaXMPP v3.2.0 has been released! Please review the change notes below to see what has changed since our last release.

Changes
=======

-  `#4369 <https://tigase.tech/issues/4369>`__: Support for `XEP-0313: Message Archive Management <http://xmpp.org/extensions/xep-0313.html>`__ has now been added within Jaxmpp-MAM module.

-  Implementation of `XEP-0357: Push Notifications <http://xmpp.org/extensions/xep-0357.html>`__

-  SCRAM-SHA-1 and SCRAM-SHA-1-PLUS are now enabled by default for Authorization.

-  `#4280 <https://tigase.tech/issues/4280>`__: added support for `XEP-0352: Client State Indication <http://xmpp.org/extensions/xep-0352.html>`__

-  `#5950 <https://tigase.tech/issues/5950>`__: added support for `XEP-0363: HTTP File Upload <http://xmpp.org/extensions/xep-0363.html>`__

-  `#5223 <https://tigase.tech/issues/5223>`__: in-band registration to use XMPP Data Forms XEP-0004

-  `#4288 <https://tigase.tech/issues/4288>`__: Login and Disconnect methods have been changed in blocking mode.

-  `#4398 <https://tigase.tech/issues/4398>`__: Removed JDK8 specific features not supported by GWT to prevent errors.

-  `#4762 <https://tigase.tech/issues/4762>`__: added support for handling WebSocket PONG frames

-  `#7387 <https://tigase.tech/issues/7387>`__: added support for PubSub notifications about node removal and association/dissociation to the collection node


Fixes
=====

-  `#4317 <https://tigase.tech/issues/4317>`__: fixed NPE if receiver of file transfer is not connected or disconnected in SOCKS5.

-  `#4318 <https://tigase.tech/issues/4318>`__: added checking if destination file is set when file transfer is accepted.

-  `#4378 <https://tigase.tech/issues/4378>`__: fixed exceptions occuring when a server-initiated disconnection could lead to an invalid connector state, fix issue with invalid connector state when connecting using Websocket Protocol over TCP.

-  `#8104 <https://tigase.tech/issues/8104>`__: fixing issue with parsing timestamp

-  `#8033 <https://tigase.tech/issues/8033>`__: added thread safety to ExtensionsChain implementation

-  `#2654 <https://tigase.tech/issues/2654>`__: improve DNS resolution for local addresses

-  `#7793 <https://tigase.tech/issues/7793>`__: fixing possible NPE in UnifiedRegistrationForm when value for a fiels is null

-  `#7649 <https://tigase.tech/issues/7649>`__: improving GWT code for sites hosted on HTTPS enabled servers

-  `#7525 <https://tigase.tech/issues/7525>`__: added method for retrieving error text from error stanzas

-  `#6330 <https://tigase.tech/issues/6330>`__: fixes for AdHocCommansAsyncCallback not calling onResponseReceived() if there is no data form in the response and for ElementWrapper which failed to set GwtElement value

-  `#6330 <https://tigase.tech/issues/6330>`__: added event for notification about PubSub node configuration change

-  `#6212 <https://tigase.tech/issues/6212>`__, `#6232 <https://tigase.tech/issues/6232>`__: fixing issue introduced by recent changes in sending authcid and authzid during authentication

-  `#6212 <https://tigase.tech/issues/6212>`__: improvements to SASL implementations

-  `#5749 <https://tigase.tech/issues/5749>`__: fix issue with login to jabber.org, says incorrect password, even though password is correct

-  `#1590 <https://tigase.tech/issues/1590>`__: add support for subscription pre-approval in RosterModule and RosterItem

-  `#5669 <https://tigase.tech/issues/5669>`__: removed firing ErrorEvent by connectors if exception is thrown in start() method within the same thread

-  `#5624 <https://tigase.tech/issues/5624>`__: fixed possible lock if Jaxmpp::login() method throws an exception and login(true) was called

-  `#5582 <https://tigase.tech/issues/5582>`__: fixing race condition during Jaxmpp login leading to a thread being permanently locked

-  `#5584 <https://tigase.tech/issues/5584>`__: MAM module not being called for incoming messages

-  `#5588 <https://tigase.tech/issues/5588>`__: fixed issue with messages without from causing NPE in MessegeModule

-  `#5527 <https://tigase.tech/issues/5527>`__: added support for connection using plain SSL instead of STARTTLS

-  `#5529 <https://tigase.tech/issues/5529>`__: fixed handling of see-other-host in ConnectionManager for GWT

-  `#5318 <https://tigase.tech/issues/5318>`__: added implementation for GwtElement::removeChild() method

-  `#5421 <https://tigase.tech/issues/5421>`__: fixed issue with handling PONG WebSocket frames with payload

-  `#4961 <https://tigase.tech/issues/4961>`__: fix JaXMPP problems on Android

-  `#4733 <https://tigase.tech/issues/4733>`__: fixed issue with comparision of elements with XMLNS

-  `#4732 <https://tigase.tech/issues/4732>`__: minor fix in MessageArchiveManagementModule

-  `#4460 <https://tigase.tech/issues/4460>`__: fixed issue with retrieving PubSub node configuration

-  `#4728 <https://tigase.tech/issues/4728>`__: fix minor issue with element equals() method

-  `#4348 <https://tigase.tech/issues/4348>`__: fixed issues with Jaxmpp state and reconnecting

-  `#4348 <https://tigase.tech/issues/4348>`__: changed DEFAULT_SOCKET_TIMEOUT to 0 as value bigger than zero causes issues for long connections without any activity

-  `#4460 <https://tigase.tech/issues/4460>`__: improved usage of events with callbacks called after all handlers processed event

-  `#4266 <https://tigase.tech/issues/4266>`__: fixing issue with reconnection after disconnection by client

-  `#4505 <https://tigase.tech/issues/4505>`__: fix handling of badly encoded channel binding data

-  `#4398 <https://tigase.tech/issues/4398>`__: remove used JDK8 specific features which are not supported by GWT

-  `#4291 <https://tigase.tech/issues/4291>`__: ContactChangedPresenceEvent has show "online" for unavailable presence

-  `#4266 <https://tigase.tech/issues/4266>`__: fixed issue with blocking disconnection

-  `#4124 <https://tigase.tech/issues/4124>`__: fixed issue with support for see-other-host with WebSocket in GWT version
