/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tigase.jaxmpp.core.client.xmpp.modules;

import java.util.List;
import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.PacketWriter;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

/**
 *
 * @author andrzej
 */
public class BookmarksModule extends AbstractIQModule  {

        private static final String BOOKMARKS_XMLNS = "storage:bookmarks";
        
        private static final Criteria CRIT = ElementCriteria.name("storage", BOOKMARKS_XMLNS);

        private static final String[] FEATURES = { BOOKMARKS_XMLNS };
        
        public static abstract class BookmarksAsyncCallback implements AsyncCallback {

                @Override
                public void onSuccess(final Stanza stanza) throws XMLException {
                        Element query = stanza.getChildrenNS("query", "jabber:iq:private");
                        Element storage = query.getChildrenNS("storage", BOOKMARKS_XMLNS);
                        onBookmarksReceived(storage.getChildren());
                }
                
                public abstract void onBookmarksReceived(List<Element> bookmarks);
        }
        
        
        public BookmarksModule(SessionObject sessionObject, PacketWriter packetWriter) {
                super(sessionObject, packetWriter);
        }
        
        @Override
        protected void processGet(IQ element) throws JaxmppException {
                throw new XMPPException(XMPPException.ErrorCondition.not_allowed);
        }

        @Override
        protected void processSet(IQ element) throws JaxmppException {
                throw new XMPPException(XMPPException.ErrorCondition.not_allowed);
        }

        @Override
        public Criteria getCriteria() {
                return CRIT;
        }

        @Override
        public String[] getFeatures() {
                return FEATURES;
        }

        public void retrieveBookmarks(BookmarksAsyncCallback callback) throws JaxmppException {
                IQ iq = IQ.create();
                iq.setType(StanzaType.get);
                
                Element query = new DefaultElement("query");
                query.setXMLNS("jabber:iq:private");
                iq.addChild(query);
                
                Element storage = new DefaultElement("storage");
                storage.setXMLNS(BOOKMARKS_XMLNS);
                query.addChild(storage);
                
                this.writer.write(iq, callback);
        }
        
}
