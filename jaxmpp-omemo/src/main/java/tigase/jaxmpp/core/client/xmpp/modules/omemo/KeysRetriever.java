package tigase.jaxmpp.core.client.xmpp.modules.omemo;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.pubsub.PubSubErrorCondition;
import tigase.jaxmpp.core.client.xmpp.modules.pubsub.PubSubModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class KeysRetriever {

	private final Context context;
	private final BareJID jid;
	private final PubSubModule pubsub;

	public KeysRetriever(Context context, BareJID jid) {
		this.context = context;
		this.jid = jid;

		this.pubsub = context.getModuleProvider().getModule(PubSubModule.class);
	}

	public void retrieve() throws JaxmppException {
		pubsub.retrieveItem(jid, OmemoModule.DEVICELIST_NODE, new PubSubModule.RetrieveItemsAsyncCallback() {
			@Override
			public void onTimeout() throws JaxmppException {
				System.out.println("ERROR: timeout");

			}

			@Override
			protected void onRetrieve(IQ responseStanza, String nodeName, Collection<Item> items) {
				try {
					for (Item item : items) {
						Element list = item.getPayload();
						if (list != null && list.getName().equals("list") &&
								list.getXMLNS().equals(OmemoModule.XMLNS)) {
							ArrayList<String> ids = new ArrayList<>();
							List<Element> devices = list.getChildren("device");
							for (Element device : devices) {
								ids.add(device.getAttribute("id"));
							}
							getKeysOfDevices(ids);
						}
					}
				} catch (JaxmppException e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void onEror(IQ response, XMPPException.ErrorCondition errorCondition,
								  PubSubErrorCondition pubSubErrorCondition) throws JaxmppException {
				System.out.println("ERROR: " + pubSubErrorCondition);
			}
		});
	}

	abstract void finish(List<Bundle> bundles);

	private void getKeysOfDevices(final Collection<String> devicesId) throws JaxmppException {
		final int count = devicesId.size();
		final ArrayList<Bundle> result = new ArrayList<>();
		for (final String id : devicesId) {
			pubsub.retrieveItem(jid, OmemoModule.BUNDLES_NODE + id, new PubSubModule.RetrieveItemsAsyncCallback() {
				@Override
				public void onTimeout() throws JaxmppException {

				}

				@Override
				protected void onEror(IQ response, XMPPException.ErrorCondition errorCondition,
									  PubSubErrorCondition pubSubErrorCondition) throws JaxmppException {

				}

				@Override
				protected void onRetrieve(IQ responseStanza, String nodeName, Collection<Item> items) {
					try {
						for (Item item : items) {
							if (item.getId().equals(OmemoModule.CURRENT)) {
								result.add(new Bundle(jid, new Integer(id), item.getPayload()));
							}
						}
						if (result.size() == count) {
							finish(result);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

}
