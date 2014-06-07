package tigase.jaxmpp.core.client.xmpp.modules.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.xml.Element;

public class ExtensionsChain {

	protected final Collection<Extension> extensions = new ArrayList<Extension>();

	private Logger log;

	public ExtensionsChain() {
		this.log = Logger.getLogger(this.getClass().getName());
	}

	public void addExtension(Extension f) {
		this.extensions.add(f);
	}

	public Element executeAfterReceiveChain(final Element element) {
		Iterator<Extension> it = extensions.iterator();
		Element e = element;
		while (it.hasNext() && e != null) {
			Extension x = it.next();
			try {
				e = x.afterReceive(e);
			} catch (Exception ex) {
				log.log(Level.WARNING, "Problem on calling afterReceive: " + ex.getMessage(), ex);
			}
		}
		return e;
	}

	public Element executeBeforeSendChain(final Element element) {
		Iterator<Extension> it = extensions.iterator();
		Element e = element;
		while (it.hasNext() && e != null) {
			Extension x = it.next();
			try {
				e = x.beforeSend(e);
			} catch (Exception ex) {
				log.log(Level.WARNING, "Problem on calling beforeSend: " + ex.getMessage(), ex);
			}
		}
		return e;
	}

	public Collection<Extension> getExtension() {
		return Collections.unmodifiableCollection(extensions);
	}

	public <T extends Extension> T getExtension(Class<T> cls) {
		Iterator<Extension> it = extensions.iterator();
		while (it.hasNext()) {
			Extension x = it.next();
			if (cls.isAssignableFrom(x.getClass()))
				return (T) x;
		}
		return null;
	}
	
	public Collection<String> getFeatures() {
		HashSet<String> result = new HashSet<String>();
		for (Extension e : this.extensions) {
			final String[] fs = e.getFeatures();
			if (fs != null) {
				for (String string : fs) {
					result.add(string);
				}
			}
		}
		return result;
	}

	public void removeExtension(Extension f) {
		this.extensions.remove(f);
	}

}
