package tigase.jaxmpp.core.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class XmppModulesManages {

	private final ArrayList<XmppModule> modules = new ArrayList<XmppModule>();

	public XmppModule findModule(final Element element) throws XMLException {
		for (XmppModule plugin : modules) {
			if (plugin.getCriteria() != null && plugin.getCriteria().match(element)) {
				return plugin;
			}
		}
		return null;
	}

	public Set<String> getAvailableFeatures() {
		HashSet<String> result = new HashSet<String>();
		for (XmppModule plugin : this.modules) {
			final String[] fs = plugin.getFeatures();
			if (fs != null) {
				for (String string : fs) {
					result.add(string);
				}
			}
		}
		return result;
	}

	public <T extends XmppModule> T register(T plugin) {
		this.modules.add(plugin);
		return plugin;
	}

}
