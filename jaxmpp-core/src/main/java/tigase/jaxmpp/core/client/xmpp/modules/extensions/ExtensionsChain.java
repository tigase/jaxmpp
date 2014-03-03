package tigase.jaxmpp.core.client.xmpp.modules.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.xml.Element;

public class ExtensionsChain {

	protected final Collection<Extension<Element>> extensions = new ArrayList<Extension<Element>>();

	private Logger log;

	public ExtensionsChain() {
		this.log = Logger.getLogger(this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	public void addExtension(Extension<? extends Element> f) {
		this.extensions.add((Extension<Element>) f);
	}

	public Element executeAfterReceiveChain(final Element element) {
		Iterator<Extension<Element>> it = extensions.iterator();
		Element e = element;
		while (it.hasNext() && e != null) {
			Extension<Element> x = it.next();
			try {
				e = x.afterReceive(e);
			} catch (Exception ex) {
				log.warning("Problem on calling afterReceive: " + ex.getMessage());
			}
		}
		return e;
	}

	public Element executeBeforeSendChain(final Element element) {
		Iterator<Extension<Element>> it = extensions.iterator();
		Element e = element;
		while (it.hasNext() && e != null) {
			Extension<Element> x = it.next();
			try {
				e = x.beforeSend(e);
			} catch (Exception ex) {
				log.warning("Problem on calling beforeSend: " + ex.getMessage());
			}
		}
		return e;
	}

	public Collection<Extension<Element>> getExtension() {
		return Collections.unmodifiableCollection(extensions);
	}

	public Collection<String> getFeatures() {
		HashSet<String> result = new HashSet<String>();
		for (Extension<?> e : this.extensions) {
			final String[] fs = e.getFeatures();
			if (fs != null) {
				for (String string : fs) {
					result.add(string);
				}
			}
		}
		return result;
	}

	public void removeExtension(Extension<?> f) {
		this.extensions.remove(f);
	}

}
