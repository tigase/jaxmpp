package tigase.jaxmpp.core.client.xmpp.modules.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.xml.Element;

public class ExtensionsChain {

	protected final Collection<Extension<XmppModule>> extensions = new ArrayList<Extension<XmppModule>>();

	private Logger log;

	public ExtensionsChain() {
		this.log = Logger.getLogger(this.getClass().getName());
	}

	public void addExtension(Extension<XmppModule> f) {
		this.extensions.add(f);
	}

	public Element executeAfterReceiveChain(final Element element) {
		Iterator<Extension<XmppModule>> it = extensions.iterator();
		Element e = element;
		while (it.hasNext() && e != null) {
			Extension<XmppModule> x = it.next();
			try {
				e = x.afterReceive(e);
			} catch (Exception ex) {
				log.log(Level.WARNING, "Problem on calling afterReceive: " + ex.getMessage(), ex);
			}
		}
		return e;
	}

	public Element executeBeforeSendChain(final Element element) {
		Iterator<Extension<XmppModule>> it = extensions.iterator();
		Element e = element;
		while (it.hasNext() && e != null) {
			Extension<XmppModule> x = it.next();
			try {
				e = x.beforeSend(e);
			} catch (Exception ex) {
				log.log(Level.WARNING, "Problem on calling beforeSend: " + ex.getMessage(), ex);
			}
		}
		return e;
	}

	public Collection<Extension<XmppModule>> getExtension() {
		return Collections.unmodifiableCollection(extensions);
	}

	public Collection<String> getFeatures() {
		HashSet<String> result = new HashSet<String>();
		for (Extension<XmppModule> e : this.extensions) {
			final String[] fs = e.getFeatures();
			if (fs != null) {
				for (String string : fs) {
					result.add(string);
				}
			}
		}
		return result;
	}

	public void removeExtension(Extension<XmppModule> f) {
		this.extensions.remove(f);
	}

}
