/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.jaxmpp.core.client.xmpp.modules.connection;

import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;

/**
 * 
 * @author andrzej
 */
public interface ConnectionEndpoint {

	public String getHost() throws JaxmppException;

	public JID getJid() throws JaxmppException;

	public Integer getPort() throws JaxmppException;

}
