/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tigase.jaxmpp.core.client.xmpp.modules.chat;

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

/**
 *
 * @author andrzej
 */
public enum ChatState {
	
	active,
	inactive,
	gone,
	composing,
	paused;

	public static String XMLNS = "http://jabber.org/protocol/chatstates";	
	
	public Element toElement() throws XMLException	 {
		Element elem  = new DefaultElement(name());
		elem.setXMLNS(XMLNS);
		return elem;
	}
	
	public static ChatState fromElement(Element elem) throws XMLException {
		if (!XMLNS.equals(elem.getXMLNS()))
			return null;
		return ChatState.valueOf(elem.getName());
	} 
	
}
