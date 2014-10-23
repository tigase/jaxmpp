package tigase.jaxmpp.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import java.util.HashMap;
import java.util.Map;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceStore;
import tigase.jaxmpp.core.client.xmpp.stanzas.Presence;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;

public class GWTPresenceStore extends PresenceStore implements JsonSerializationHelper.Serializable {

	public GWTPresenceStore() {
		presencesMapByBareJid = new HashMap<BareJID, Map<String, Presence>>();
		presenceByJid = new HashMap<JID, Presence>();
		bestPresence = new HashMap<BareJID, Presence>();
	}

	@Override
	protected Map<String, Presence> createResourcePresenceMap() {
		return new HashMap<String, Presence>();
	}
	
	@Override
	public String getJsonType() {
		return "presenceStore";
	}

	@Override
	public JavaScriptObject toJSON() throws XMLException {
		JavaScriptObject obj = JavaScriptObject.createObject();
		JavaScriptObject presences = JavaScriptObject.createObject();
		for (JID jid : presenceByJid.keySet()) {
			Presence presence = presenceByJid.get(jid);
			JavaScriptObject jsPresence = JsonSerializationHelper.toJSON(presence);
			JsonSerializationHelper.putToObject(presences, jid.toString(), jsPresence);
		}
		JsonSerializationHelper.putToObject(obj, "type", getJsonType());
		JsonSerializationHelper.putToObject(obj, "items", presences);
		return obj;
	}
	
	@Override
	public Object fromJSON(JsonSerializationHelper helper, JavaScriptObject obj) throws JaxmppException {
		String type = JsonSerializationHelper.getStringFromObject(obj, "type");
		if (!getJsonType().equals(type)) {
			return null;
		}
		
		JavaScriptObject presences = JsonSerializationHelper.getObjectFromObject(obj, "items");
		JsArrayString jids = JsonSerializationHelper.getKeysFromObject(presences);
		for (int i=0; i<jids.length(); i++) {
			String key = jids.get(i);
			JID jid = JID.jidInstance(key);
			JavaScriptObject jsPresence = JsonSerializationHelper.getObjectFromObject(presences, key);
			Element el = (Element) helper.fromJSON(jsPresence);
			Presence presence = (Presence) Stanza.create(el);
			update(presence);
		}
		
		return this;
	}
}
