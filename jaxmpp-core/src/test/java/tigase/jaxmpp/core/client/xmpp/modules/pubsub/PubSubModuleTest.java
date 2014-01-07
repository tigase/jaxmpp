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
package tigase.jaxmpp.core.client.xmpp.modules.pubsub;

import java.util.Collection;
import java.util.Date;

import org.junit.Test;

import tigase.jaxmpp.core.client.AbstractJaxmppTest;
import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.ResponseManager;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.XmppModulesManager;
import tigase.jaxmpp.core.client.criteria.tpath.TPath;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.PingModule;
import tigase.jaxmpp.core.client.xmpp.modules.pubsub.PubSubModule.PublishAsyncCallback;
import tigase.jaxmpp.core.client.xmpp.modules.pubsub.PubSubModule.RetrieveItemsAsyncCallback;
import tigase.jaxmpp.core.client.xmpp.modules.pubsub.PubSubModule.SubscriptionAsyncCallback;
import tigase.jaxmpp.core.client.xmpp.modules.pubsub.PubSubModule.SubscriptionElement;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Message;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

public class PubSubModuleTest extends AbstractJaxmppTest {

	private final PubSubModule pubsub;

	private final TPath tpath = new TPath();

	public PubSubModuleTest() {
		XmppModulesManager xmppModulesManages = new XmppModulesManager(context);
		xmppModulesManages.register(new PingModule(context));
		this.pubsub = new PubSubModule(context);
	}

	private Runnable getResponseHandler(Element element) throws JaxmppException {
		return ResponseManager.getResponseHandler(context.getSessionObject(), element, context.getWriter());
	}

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
	}

	@Test
	public void testDeleteItem() throws XMLException, JaxmppException {
		final boolean[] check = new boolean[] { false };

		this.pubsub.deleteItem(BareJID.bareJIDInstance("workflows.shakespeare.lit"), "a290fjsl29j19kjb",
				"ae890ac52d0df67ed7cfdf51b644e901", new PubSubAsyncCallback() {

					@Override
					protected void onEror(IQ response, ErrorCondition errorCondition, PubSubErrorCondition pubSubErrorCondition) {
						fail();
					}

					@Override
					public void onSuccess(Stanza responseStanza) throws XMLException {
						check[0] = true;
						assertEquals(StanzaType.result, responseStanza.getType());
					}

					@Override
					public void onTimeout() throws XMLException {
						fail();
					}
				});

		final Element iq = this.poll();
		final String id = iq.getAttribute("id");
		assertEquals("set", tpath.compile("/iq/attr('type')").evaluate(iq));
		assertEquals("workflows.shakespeare.lit", tpath.compile("/iq/attr('to')").evaluate(iq));
		assertEquals("http://jabber.org/protocol/pubsub", tpath.compile("/iq/pubsub/attr('xmlns')").evaluate(iq));
		assertEquals("a290fjsl29j19kjb", tpath.compile("/iq/pubsub/retract/attr('node')").evaluate(iq));
		assertEquals("ae890ac52d0df67ed7cfdf51b644e901", tpath.compile("/iq/pubsub/retract/item/attr('id')").evaluate(iq));

		IQ responseIq = IQ.create();
		responseIq.setId(id);
		responseIq.setAttribute("from", "workflows.shakespeare.lit");
		responseIq.setType(StanzaType.result);

		getResponseHandler(responseIq).run();
		org.junit.Assert.assertTrue("Callback not called", check[0]);
	}

	@Test
	public void testProcess() throws XMLException, JaxmppException, XMPPException {
		final boolean[] check = new boolean[] { false };

		Message message = Message.create();
		message.setAttribute("from", "pubsub.shakespeare.lit");
		message.setAttribute("to", "francisco@denmark.lit");

		Element event = new DefaultElement("event", null, "http://jabber.org/protocol/pubsub#event");
		message.addChild(event);

		Element items = new DefaultElement("items");
		items.setAttribute("node", "princely_musings");
		event.addChild(items);

		Element item = new DefaultElement("item");
		item.setAttribute("id", "ae890ac52d0df67ed7cfdf51b644e901");
		items.addChild(item);

		final Element payload = new DefaultElement("content", "dupa", "jaxmpp:test");

		item.addChild(payload);

		assertEquals(
				"dupa",
				tpath.compile("/message/event/items/item[@id='ae890ac52d0df67ed7cfdf51b644e901']/content/value()").evaluate(
						message));

		context.getEventBus().addHandler(PubSubModule.NotificationReceivedHandler.NotificationReceivedEvent.class,
				new PubSubModule.NotificationReceivedHandler() {

					@Override
					public void onNotificationReceived(SessionObject sessionObject, Message message, JID pubSubJID,
							String nodeName, String itemId, Element recPayload, Date delayTime, String itemType) {
						check[0] = true;
						try {
							assertEquals("ae890ac52d0df67ed7cfdf51b644e901", itemId);
							assertEquals(
									"dupa",
									tpath.compile(
											"/message/event/items/item[@id='ae890ac52d0df67ed7cfdf51b644e901']/content/value()").evaluate(
											message));
							assertEquals(JID.jidInstance("pubsub.shakespeare.lit"), pubSubJID);
							assertEquals("princely_musings", nodeName);
							assertEquals(payload, recPayload);
						} catch (XMLException e) {
							fail(e.getMessage());
						}

					}
				});
		this.pubsub.process(message);
		assertEquals("Listener not called", true, check[0]);
	}

	@Test
	public void testPublishItemBareJIDStringStringElementAsyncCallback() throws XMLException, JaxmppException {
		final boolean[] check = new boolean[] { false };
		DefaultElement payload = new DefaultElement("content", "dupa", "jaxmpp:test");
		this.pubsub.publishItem(BareJID.bareJIDInstance("a@b.c"), "nn", "123", payload, new PublishAsyncCallback() {

			@Override
			protected void onEror(IQ response, ErrorCondition errorCondition, PubSubErrorCondition pubSubErrorCondition) {
				fail();
			}

			@Override
			public void onPublish(String itemId) {
				check[0] = true;
				assertEquals("123", itemId);
			}

			@Override
			public void onTimeout() throws XMLException {
				fail();
			}
		});

		final Element iq = this.poll();
		final String id = iq.getAttribute("id");

		assertEquals("set", tpath.compile("/iq/attr('type')").evaluate(iq));
		assertEquals("a@b.c", tpath.compile("/iq/attr('to')").evaluate(iq));
		assertEquals("http://jabber.org/protocol/pubsub", tpath.compile("/iq/pubsub/attr('xmlns')").evaluate(iq));
		assertEquals("nn", tpath.compile("/iq/pubsub/publish/attr('node')").evaluate(iq));
		assertEquals("123", tpath.compile("/iq/pubsub/publish/item/attr('id')").evaluate(iq));
		assertEquals("jaxmpp:test", tpath.compile("/iq/pubsub/publish/item/content/attr('xmlns')").evaluate(iq));
		assertEquals("dupa", tpath.compile("/iq/pubsub/publish[@node='nn']/item[@id='123']/content/value()").evaluate(iq));

		IQ responseIq = IQ.create();
		responseIq.setId(id);
		responseIq.setAttribute("from", "a@b.c");
		responseIq.setType(StanzaType.result);

		Element pubsub = new DefaultElement("pubsub", null, "http://jabber.org/protocol/pubsub");
		responseIq.addChild(pubsub);
		Element publish = new DefaultElement("publish");
		publish.setAttribute("node", "nn");
		pubsub.addChild(publish);
		Element item = new DefaultElement("item");
		item.setAttribute("id", "123");
		publish.addChild(item);

		Runnable handler = getResponseHandler(responseIq);

		handler.run();
		assertEquals("AsyncCallback not called", true, check[0]);
	}

	@Test
	public void testPublishItemBareJIDStringStringElementAsyncCallback_Error() throws XMLException, JaxmppException {
		final boolean[] check = new boolean[] { false };
		DefaultElement payload = new DefaultElement("content", "dupa", "jaxmpp:test");
		this.pubsub.publishItem(BareJID.bareJIDInstance("a@b.c"), "nn", "123", payload, new PublishAsyncCallback() {

			@Override
			protected void onEror(IQ response, ErrorCondition errorCondition, PubSubErrorCondition pubSubErrorCondition) {
				check[0] = true;
				assertEquals(ErrorCondition.feature_not_implemented, errorCondition);
				assertEquals(PubSubErrorCondition.unsupported_publish, pubSubErrorCondition);
			}

			@Override
			public void onPublish(String itemId) {
				fail();
			}

			@Override
			public void onTimeout() throws XMLException {
				fail();
			}
		});

		final Element iq = this.poll();
		final String id = iq.getAttribute("id");

		assertEquals("set", tpath.compile("/iq/attr('type')").evaluate(iq));
		assertEquals("a@b.c", tpath.compile("/iq/attr('to')").evaluate(iq));
		assertEquals("http://jabber.org/protocol/pubsub", tpath.compile("/iq/pubsub/attr('xmlns')").evaluate(iq));
		assertEquals("nn", tpath.compile("/iq/pubsub/publish/attr('node')").evaluate(iq));
		assertEquals("123", tpath.compile("/iq/pubsub/publish/item/attr('id')").evaluate(iq));
		assertEquals("jaxmpp:test", tpath.compile("/iq/pubsub/publish/item/content/attr('xmlns')").evaluate(iq));
		assertEquals("dupa", tpath.compile("/iq/pubsub/publish[@node='nn']/item[@id='123']/content/value()").evaluate(iq));

		IQ responseIq = IQ.create();
		responseIq.setId(id);
		responseIq.setAttribute("from", "a@b.c");
		responseIq.setType(StanzaType.error);

		Element error = new DefaultElement("error", null, null);
		error.setAttribute("type", "cancel");
		responseIq.addChild(error);
		error.addChild(new DefaultElement("feature-not-implemented", null, "urn:ietf:params:xml:ns:xmpp-stanzas"));
		Element uns = new DefaultElement("unsupported", null, "http://jabber.org/protocol/pubsub#errors");
		uns.setAttribute("feature", "publish");
		error.addChild(uns);

		Runnable handler = getResponseHandler(responseIq);
		handler.run();
		assertEquals("AsyncCallback not called", true, check[0]);
	}

	@Test
	public void testRetrieveItemBareJIDStringRetrieveItemsAsyncCallback() throws XMLException, JaxmppException {
		final boolean[] check = new boolean[] { false };
		this.pubsub.retrieveItem(BareJID.bareJIDInstance("pubsub.shakespeare.lit"), "princely_musings",
				"ae890ac52d0df67ed7cfdf51b644e901", new RetrieveItemsAsyncCallback() {

					@Override
					protected void onEror(IQ response, ErrorCondition errorCondition, PubSubErrorCondition pubSubErrorCondition) {
						fail();
					}

					@Override
					protected void onRetrieve(IQ responseStanza, String nodeName, Collection<Item> items) {
						check[0] = true;
						try {
							assertEquals("princely_musings", nodeName);
							assertEquals(1, items.size());
							Item item = items.iterator().next();
							assertEquals("ae890ac52d0df67ed7cfdf51b644e901", item.getId());
							assertEquals("dupa_01", item.getPayload().getValue());
						} catch (XMLException e) {
							e.printStackTrace();
							fail(e.getMessage());
						}
					}

					@Override
					public void onTimeout() throws XMLException {
						fail();
					}
				});

		final Element iq = this.poll();
		final String id = iq.getAttribute("id");
		assertEquals("get", tpath.compile("/iq/attr('type')").evaluate(iq));
		assertEquals("pubsub.shakespeare.lit", tpath.compile("/iq/attr('to')").evaluate(iq));
		assertEquals("http://jabber.org/protocol/pubsub", tpath.compile("/iq/pubsub/attr('xmlns')").evaluate(iq));
		assertEquals("princely_musings", tpath.compile("/iq/pubsub/items/attr('node')").evaluate(iq));
		assertEquals("ae890ac52d0df67ed7cfdf51b644e901", tpath.compile("/iq/pubsub/items/item/attr('id')").evaluate(iq));

		IQ responseIq = IQ.create();
		responseIq.setId(id);
		responseIq.setAttribute("from", "pubsub.shakespeare.lit");
		responseIq.setType(StanzaType.result);

		Element pubsub = new DefaultElement("pubsub", null, "http://jabber.org/protocol/pubsub");
		responseIq.addChild(pubsub);
		Element items = new DefaultElement("items");
		items.setAttribute("node", "princely_musings");
		pubsub.addChild(items);

		Element item0 = new DefaultElement("item");
		item0.setAttribute("id", "ae890ac52d0df67ed7cfdf51b644e901");
		items.addChild(item0);
		item0.addChild(new DefaultElement("payload", "dupa_01", "tigase:test"));

		getResponseHandler(responseIq).run();
		assertEquals("AsyncCallback not called", true, check[0]);
	}

	@Test
	public void testSubscribeBareJIDStringJIDSubscriptionAsyncCallback() throws XMLException, JaxmppException {
		final boolean[] check = new boolean[] { false };
		this.pubsub.subscribe(BareJID.bareJIDInstance("pubsub.shakespeare.lit"), "princely_musings",
				JID.jidInstance("francisco@denmark.lit"), new SubscriptionAsyncCallback() {

					@Override
					protected void onEror(IQ response, ErrorCondition errorCondition, PubSubErrorCondition pubSubErrorCondition) {
						fail();
					}

					@Override
					protected void onSubscribe(IQ response, SubscriptionElement subscriptionElement) {
						try {
							check[0] = true;
							assertEquals("princely_musings", subscriptionElement.getNode());
							assertEquals(JID.jidInstance("francisco@denmark.lit"), subscriptionElement.getJID());
							assertEquals("ba49252aaa4f5d320c24d3766f0bdcade78c78d3", subscriptionElement.getSubID());
							assertEquals(Subscription.subscribed, subscriptionElement.getSubscription());
						} catch (Exception e) {
							fail(e.getMessage());
						}
					}

					@Override
					public void onTimeout() throws XMLException {
						fail();
					}

				});

		final Element iq = this.poll();
		final String id = iq.getAttribute("id");
		assertEquals("set", tpath.compile("/iq/attr('type')").evaluate(iq));
		assertEquals("pubsub.shakespeare.lit", tpath.compile("/iq/attr('to')").evaluate(iq));
		assertEquals("http://jabber.org/protocol/pubsub", tpath.compile("/iq/pubsub/attr('xmlns')").evaluate(iq));
		assertEquals("princely_musings", tpath.compile("/iq/pubsub/subscribe/attr('node')").evaluate(iq));
		assertEquals("francisco@denmark.lit", tpath.compile("/iq/pubsub/subscribe/attr('jid')").evaluate(iq));

		IQ responseIq = IQ.create();
		responseIq.setId(id);
		responseIq.setAttribute("from", "pubsub.shakespeare.lit");
		responseIq.setType(StanzaType.result);

		Element pubsub = new DefaultElement("pubsub", null, "http://jabber.org/protocol/pubsub");
		responseIq.addChild(pubsub);
		Element subs = new DefaultElement("subscription");
		subs.setAttribute("node", "princely_musings");
		subs.setAttribute("jid", "francisco@denmark.lit");
		subs.setAttribute("subid", "ba49252aaa4f5d320c24d3766f0bdcade78c78d3");
		subs.setAttribute("subscription", "subscribed");
		pubsub.addChild(subs);

		getResponseHandler(responseIq).run();
		assertEquals("AsyncCallback not called", true, check[0]);
	}

	@Test
	public void testUnlockItemBareJIDStringStringPubSubAsyncCallback() throws XMLException, JaxmppException, XMPPException {
		final boolean[] check = new boolean[] { false, false };
		this.pubsub.unlockItem(BareJID.bareJIDInstance("workflows.shakespeare.lit"), "a290fjsl29j19kjb",
				"ae890ac52d0df67ed7cfdf51b644e901", new PubSubAsyncCallback() {

					@Override
					protected void onEror(IQ response, ErrorCondition errorCondition, PubSubErrorCondition pubSubErrorCondition) {
						fail();
					}

					@Override
					public void onSuccess(Stanza responseStanza) throws XMLException {
						check[0] = true;
						assertEquals(StanzaType.result, responseStanza.getType());
					}

					@Override
					public void onTimeout() throws XMLException {
						fail();
					}
				});

		final Element iq = this.poll();
		final String id = iq.getAttribute("id");
		assertEquals("set", tpath.compile("/iq/attr('type')").evaluate(iq));
		assertEquals("workflows.shakespeare.lit", tpath.compile("/iq/attr('to')").evaluate(iq));
		assertEquals("http://jabber.org/protocol/pubsub", tpath.compile("/iq/pubsub/attr('xmlns')").evaluate(iq));
		assertEquals("urn:xmpp:pubsub:queueing:0", tpath.compile("/iq/pubsub/unlock/attr('xmlns')").evaluate(iq));
		assertEquals("a290fjsl29j19kjb", tpath.compile("/iq/pubsub/unlock/attr('node')").evaluate(iq));
		assertEquals("ae890ac52d0df67ed7cfdf51b644e901", tpath.compile("/iq/pubsub/unlock/item/attr('id')").evaluate(iq));

		IQ responseIq = IQ.create();
		responseIq.setId(id);
		responseIq.setAttribute("from", "workflows.shakespeare.lit");
		responseIq.setType(StanzaType.result);

		getResponseHandler(responseIq).run();
		assertEquals("AsyncCallback not called", true, check[0]);

		Message message = Message.create();
		message.setAttribute("from", "workflow.shakespeare.lit");
		message.setAttribute("to", "workerbee237@shakespeare.lit");

		Element event = new DefaultElement("event", null, "http://jabber.org/protocol/pubsub#event");
		message.addChild(event);

		Element items = new DefaultElement("items");
		items.setAttribute("node", "a290fjsl29j19kjb");
		event.addChild(items);

		Element unlock = new DefaultElement("unlock", null, "urn:xmpp:queueing:0");
		unlock.setAttribute("id", "ae890ac52d0df67ed7cfdf51b644e901");
		items.addChild(unlock);

		context.getEventBus().addHandler(PubSubModule.NotificationReceivedHandler.NotificationReceivedEvent.class,
				new PubSubModule.NotificationReceivedHandler() {

					@Override
					public void onNotificationReceived(SessionObject sessionObject, Message message, JID pubSubJID,
							String nodeName, String itemId, Element payload, Date delayTime, String itemType) {
						check[1] = true;
						try {
							assertEquals("unlock", itemType);
							assertEquals("a290fjsl29j19kjb", nodeName);
							assertEquals("ae890ac52d0df67ed7cfdf51b644e901", itemId);
						} catch (Exception e) {
							fail(e.getMessage());
						}

					}
				});

		this.pubsub.process(message);
		assertEquals("Listener not called", true, check[1]);
	}

	@Test
	public void testUnsubscribeBareJIDStringJIDAsyncCallback() throws XMLException, JaxmppException {
		final boolean[] check = new boolean[] { false };
		this.pubsub.unsubscribe(BareJID.bareJIDInstance("pubsub.shakespeare.lit"), "princely_musings",
				JID.jidInstance("francisco@denmark.lit"), new PubSubAsyncCallback() {

					@Override
					protected void onEror(IQ response, ErrorCondition errorCondition, PubSubErrorCondition pubSubErrorCondition) {
						fail();
					}

					@Override
					public void onSuccess(Stanza responseStanza) throws XMLException {
						check[0] = true;
						assertEquals(StanzaType.result, responseStanza.getType());
					}

					@Override
					public void onTimeout() throws XMLException {
						fail();
					}
				});

		final Element iq = this.poll();
		final String id = iq.getAttribute("id");
		assertEquals("set", tpath.compile("/iq/attr('type')").evaluate(iq));
		assertEquals("pubsub.shakespeare.lit", tpath.compile("/iq/attr('to')").evaluate(iq));
		assertEquals("http://jabber.org/protocol/pubsub", tpath.compile("/iq/pubsub/attr('xmlns')").evaluate(iq));
		assertEquals("princely_musings", tpath.compile("/iq/pubsub/unsubscribe/attr('node')").evaluate(iq));
		assertEquals("francisco@denmark.lit", tpath.compile("/iq/pubsub/unsubscribe/attr('jid')").evaluate(iq));

		IQ responseIq = IQ.create();
		responseIq.setId(id);
		responseIq.setAttribute("from", "pubsub.shakespeare.lit");
		responseIq.setType(StanzaType.result);

		getResponseHandler(responseIq).run();
		assertEquals("AsyncCallback not called", true, check[0]);
	}
}