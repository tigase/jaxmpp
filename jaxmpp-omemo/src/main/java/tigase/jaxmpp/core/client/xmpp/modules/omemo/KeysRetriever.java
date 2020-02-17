package tigase.jaxmpp.core.client.xmpp.modules.omemo;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.pubsub.PubSubErrorCondition;
import tigase.jaxmpp.core.client.xmpp.modules.pubsub.PubSubModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;

import java.util.*;
import java.util.logging.Logger;

public abstract class KeysRetriever {

	private final Context context;
	private final BareJID jid;
	private final PubSubModule pubsub;

	private Logger log = Logger.getLogger(this.getClass().getName());

	public static Collection<Integer> getDeviceIDsFromPayload(
			Collection<PubSubModule.RetrieveItemsAsyncCallback.Item> items) {
		final HashSet<Integer> ids = new HashSet<>();
		try {
			for (PubSubModule.RetrieveItemsAsyncCallback.Item item : items) {
				Element list = item.getPayload();
				if (list != null && list.getName().equals("list") && list.getXMLNS().equals(OmemoModule.XMLNS)) {
					List<Element> devices = list.getChildren("device");
					for (Element device : devices) {
						ids.add(Integer.valueOf(device.getAttribute("id")));
					}
				}
			}
		} catch (JaxmppException e) {
			e.printStackTrace();
		}
		return ids;
	}

	public KeysRetriever(Context context, BareJID jid) {
		this.context = context;
		this.jid = jid;

		this.pubsub = context.getModuleProvider().getModule(PubSubModule.class);
	}

	public void retrieve(Collection<Integer> deviceIds) throws JaxmppException {
		getKeysOfDevices(deviceIds);
	}

	public void retrieve() throws JaxmppException {
		log.fine("Checking devicelist of " + jid);
		pubsub.retrieveItem(jid, OmemoModule.DEVICELIST_NODE, new PubSubModule.RetrieveItemsAsyncCallback() {
			@Override
			public void onTimeout() throws JaxmppException {
				System.out.println("ERROR: timeout");
				error();
			}

			@Override
			protected void onRetrieve(IQ responseStanza, String nodeName, Collection<Item> items) {
				try {
					Collection<Integer> ids = getDeviceIDsFromPayload(items);
					log.finer("Found " + ids.size() + " devices.");
					retrieve(ids);
				} catch (JaxmppException e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void onEror(IQ response, XMPPException.ErrorCondition errorCondition,
								  PubSubErrorCondition pubSubErrorCondition) throws JaxmppException {
				System.out.println("ERROR: " + pubSubErrorCondition);
				error();
			}
		});
	}

	abstract protected void finish(List<Bundle> bundles);

	protected abstract void error();

	private void getKeysOfDevices(final Collection<?> devicesId) throws JaxmppException {
		log.finer("Checking bundles for devices " + devicesId);
		final int count = devicesId.size();
		final ArrayList<Bundle> result = new ArrayList<>();
		final Set<Object> requests = new HashSet<>();
		requests.addAll(devicesId);
		for (final Object id : devicesId) {
			pubsub.retrieveItem(jid, OmemoModule.BUNDLES_NODE + id, new PubSubModule.RetrieveItemsAsyncCallback() {
				@Override
				public void onTimeout() throws JaxmppException {
					log.fine("Request for device " + jid + "#" + id + " timeout");
					synchronized (context) {
						requests.remove(id);
						if (requests.isEmpty()) {
							finish(result);
						}
					}
				}

				@Override
				protected void onEror(IQ response, XMPPException.ErrorCondition errorCondition,
									  PubSubErrorCondition pubSubErrorCondition) throws JaxmppException {
					log.fine("Request for device " + jid + "#" + id + " error: " + errorCondition);
					synchronized (context) {
						requests.remove(id);
						if (requests.isEmpty()) {
							finish(result);
						}
					}
				}

				@Override
				protected void onRetrieve(IQ responseStanza, String nodeName, Collection<Item> items) {
					log.fine("Request for device " + jid + "#" + id + " success.");
					try {
						for (Item item : items) {
							if (item.getId().equals(OmemoModule.CURRENT)) {
								result.add(new Bundle(jid, new Integer(id.toString()), item.getPayload()));
							}
						}
						synchronized (context) {
							requests.remove(id);
							if (requests.isEmpty()) {
								finish(result);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

}
