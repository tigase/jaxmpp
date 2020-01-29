<h1 align="center">
  Tigase Java XMPP Client Library
</h1>

# What it is

Tigase Java XMPP Client Library is an [XMPP](https://xmpp.org) client library written in a Java programming language. 
It provides implementation of core of the XMPP standard and processing XML. Additionally it provides support for many popular extensions (XEP's). 

This repository contains source files of the library.

# Features
JaXMPP implements support for [RFC 6120: Extensible Messaging and Presence Protocol (XMPP): Core](https://xmpp.org/rfcs/rfc6120.html) and [RFC 6121: Extensible Messaging and Presence Protocol (XMPP): Instant Messaging and Presence](https://xmpp.org/rfcs/rfc6121.html).
Additionally it supports many popular XEPs. Below is a list of some of the supported XEPs:

* [XEP-0004: Data Forms](https://xmpp.org/extensions/xep-0004.html)
* [XEP-0030: Service Discovery](https://xmpp.org/extensions/xep-0030.html)
* [XEP-0045: Multi-User Chat](https://xmpp.org/extensions/xep-0045.html)
* [XEP-0050: Ad-Hoc Commands](https://xmpp.org/extensions/xep-0050.html)
* [XEP-0054: vcard-temp](https://xmpp.org/extensions/xep-0054.html)
* [XEP-0069: Result Set Management](https://xmpp.org/extensions/xep-0059.html)
* [XEP-0060: Publish-Subscribe](https://xmpp.org/extensions/xep-0060.html)
* [XEP-0077: In-Band Registration](https://xmpp.org/extensions/xep-0077.html)
* [XEP-0084: User Avatar](https://xmpp.org/extensions/xep-0084.html)
* [XEP-0092: Software Version](https://xmpp.org/extensions/xep-0092.html)
* [XEP-0115: Entity Capabilities](https://xmpp.org/extensions/xep-0115.html)
* [XEP-0138: Stream Compression](https://xmpp.org/extensions/xep-0138.html)
* [XEP-0153: vCard-Based Avatar](https://xmpp.org/extensions/xep-0153.html)
* [XEP-0163: Personal Eventing Protocol](https://xmpp.org/extensions/xep-0163.html)
* [XEP-0166: Jingle](https://xmpp.org/extensions/xep-0166.html)
* [XEP-0167: Jingle RTP Session](https://xmpp.org/extensions/xep-0167.html)
* [XEP-0176: Jingle ICE-UDP Transport Method](https://xmpp.org/extensions/xep-0176.html)
* [XEP-0184: Message Delivery Receipts](https://xmpp.org/extensions/xep-0184.html)
* [XEP-0198: Stream Management](https://xmpp.org/extensions/xep-0198.html)
* [XEP-0203: Delayed Delivery](https://xmpp.org/extensions/xep-0203.html)
* [XEP-0249: Direct MUC Invitations](https://xmpp.org/extensions/xep-0249.html)
* [XEP-0280: Message Carbons](https://xmpp.org/extensions/xep-0280.html)
* [XEP-0313: Message Archive Management](https://xmpp.org/extensions/xep-0313.html)
* [XEP-0357: Push Notifications](https://xmpp.org/extensions/xep-0357.html)
* [XEP-0363: HTTP File Upload](https://xmpp.org/extensions/xep-0363.html)
* [XEP-0384: OMEMO Encryption](https://xmpp.org/extensions/xep-0384.html)

# Support

When looking for support, please first search for answers to your question in the available online channels:

* Our online documentation: [Tigase Docs](https://docs.tigase.net)
* Our online forums: [Tigase Forums](https://help.tigase.net/portal/community)
* Our online Knowledge Base [Tigase KB](https://help.tigase.net/portal/kb)

If you didn't find an answer in the resources above, feel free to submit your question to either our 
[community portal](https://help.tigase.net/portal/community) or open a [support ticket](https://help.tigase.net/portal/newticket).
 
# Compilation 

[Maven](https://maven.apache.org/) is required tool to compile library:

    mvn package

To build [Android](https://developer.android.com/) JaXMPP module just use `android` profile:

    mvn package -Pandroid 

Note, that `ANDROID_HOME` must be declared.

# License

<img alt="Tigase Tigase Logo" src="https://github.com/tigase/website-assets/blob/master/tigase/images/tigase-logo.png?raw=true" width="25"/> Official <a href="https://tigase.net/">Tigase</a> repository is available at: https://github.com/tigase/jaxmpp/.

Copyright (c) 2004 Tigase, Inc.

Licensed under AGPL License Version 3. Other licensing options available upon request.
