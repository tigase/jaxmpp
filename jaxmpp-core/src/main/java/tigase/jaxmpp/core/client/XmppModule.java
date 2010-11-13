package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 * Main interface for implement modules. Module is stateless!. To store any
 * statefull data use {@linkplain SessionObject SessionObject}
 * 
 * @author bmalkow
 * 
 */
public interface XmppModule {

	/**
	 * Criteria
	 * 
	 * @return
	 */
	Criteria getCriteria();

	/**
	 * <p>
	 * Returns features what are implemented by Module.
	 * </p>
	 * <p>
	 * See <a href="http://xmpp.org/registrar/disco-features.html">Service
	 * Discovery Features</a>
	 * </p>
	 * 
	 * @return array of features
	 */
	String[] getFeatures();

	/**
	 * Main method of module. Module will process incoming stanza by call this
	 * method.
	 * 
	 * @param element
	 *            incoming XMPP stanza
	 * @param sessionObject
	 *            XMPP session object
	 * @param packetWriter
	 *            XML writer
	 */
	void process(Element element, SessionObject sessionObject, PacketWriter writer) throws XMPPException, XMLException;

}
