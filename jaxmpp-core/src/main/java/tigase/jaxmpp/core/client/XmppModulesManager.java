package tigase.jaxmpp.core.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class XmppModulesManager {

	public static interface InitializingBean {

		void init() throws JaxmppException;

	}

	private final ArrayList<XmppModule> modules = new ArrayList<XmppModule>();

	private final HashMap<Class<XmppModule>, XmppModule> modulesByClasses = new HashMap<Class<XmppModule>, XmppModule>();

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

	@SuppressWarnings("unchecked")
	public <T extends XmppModule> T getModule(Class<T> moduleClass) {
		return (T) this.modulesByClasses.get(moduleClass);
	}

	public void init() {
		for (XmppModule mod : this.modules) {
			if (mod instanceof InitializingBean) {
				try {
					((InitializingBean) mod).init();
				} catch (JaxmppException e) {
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends XmppModule> T register(T plugin) {
		this.modulesByClasses.put((Class<XmppModule>) plugin.getClass(), plugin);
		this.modules.add(plugin);
		return plugin;
	}

}
