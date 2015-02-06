/*
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2015 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.jaxmpp.core.client.xmpp.modules.flexible_offline;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.XMPPException;
import tigase.jaxmpp.core.client.XMPPException.ErrorCondition;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xml.XMLException;
import tigase.jaxmpp.core.client.xmpp.modules.AbstractIQModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule;
import tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule.DiscoItemsAsyncCallback;
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static tigase.jaxmpp.core.client.xmpp.modules.disco.DiscoveryModule.ITEMS_XMLNS;

public class FlexibleOfflineMessageRetrieval extends AbstractIQModule {

	public static final String FLEXIBLE_OFFLINE_MODE_KEY = "FLEXIBLE_OFFLINE_MODE_KEY";
	public static final String FLEXIBLE_OFFLINE_XMLNS = "http://jabber.org/protocol/offline";

	@Override
	public void afterRegister() {
		PresenceModule presenceModule = context.getModuleProvider().getModule( PresenceModule.class );
		presenceModule.setInitialPresence( false );
		log.log( Level.INFO, "loaded FlexibleOfflineMessageRetrieval, disabling initial presence");
	}

	public void fetchOfflineMessages(AsyncCallback asyncCallback) throws JaxmppException {
		getOfflineMessages( null, asyncCallback );
	}


	@Override
	public Criteria getCriteria() {
		return null;
	}

	@Override
	public String[] getFeatures(  ) {
		return null;
	}

	public void getOfflineMessages( ArrayList<Item> items, AsyncCallback asyncCallback) throws JaxmppException {
		IQ iq = IQ.create();
		JID user = JID.jidInstance( context.getSessionObject().getUserBareJid() );
		iq.setFrom( user);
		iq.setTo( user);
		iq.setType( StanzaType.get );
		Element query = ElementFactory.create( "offline", null, FLEXIBLE_OFFLINE_XMLNS );
		if ( items == null ){
			Element itemElement = ElementFactory.create( "fetch" );
			query.addChild( itemElement );
		} else if ( !items.isEmpty() ){
			for ( Item item : items ) {
				Element itemElement = ElementFactory.create( "item" );
				itemElement.setAttribute( "action", ITEM_ACTION.view.name() );
				itemElement.setAttribute( "node", item.getNode() );
				query.addChild( itemElement );

			}
		}
		iq.addChild( query );

		write( iq, asyncCallback );
	}

	public void getOfflineMessagesInfo( AsyncCallback asyncCallback ) throws JaxmppException {
		DiscoveryModule module = context.getModuleProvider().getModule( DiscoveryModule.class );

		module.getInfo( JID.jidInstance( context.getSessionObject().getUserBareJid() ), FLEXIBLE_OFFLINE_XMLNS, asyncCallback );

	}

	public void getOfflineMessagesItems( AsyncCallback asyncCallback ) throws JaxmppException {
		DiscoveryModule module = context.getModuleProvider().getModule( DiscoveryModule.class );

		module.getItems( JID.jidInstance( context.getSessionObject().getUserBareJid() ), FLEXIBLE_OFFLINE_XMLNS, asyncCallback );

	}
	public void purgeOfflineMessages( AsyncCallback asyncCallback ) throws JaxmppException {
		getOfflineMessages( null, asyncCallback );
	}

	public void removeOfflineMessages( ArrayList<Item> items, AsyncCallback asyncCallback) throws JaxmppException {
		IQ iq = IQ.create();
		JID user = JID.jidInstance( context.getSessionObject().getUserBareJid() );
		iq.setFrom( user);
		iq.setTo( user);
		iq.setType( StanzaType.get );
		Element query = ElementFactory.create( "offline", null, FLEXIBLE_OFFLINE_XMLNS );
		if ( items == null ){
			Element itemElement = ElementFactory.create( "purge" );
			query.addChild( itemElement );
		} else if ( !items.isEmpty() ){
			for ( Item item : items ) {
				Element itemElement = ElementFactory.create( "item" );
				itemElement.setAttribute( "action", ITEM_ACTION.remove.name() );
				itemElement.setAttribute( "node", item.getNode() );
				query.addChild( itemElement );

			}
		}
		iq.addChild( query );

		write( iq, asyncCallback );
	}

	@Override
	protected void processGet( IQ element) throws JaxmppException {
		throw new XMPPException( ErrorCondition.not_allowed );
	}

	@Override
	protected void processSet( IQ element) throws JaxmppException {
		throw new XMPPException( ErrorCondition.not_allowed );
	}

	public static abstract class FlexibleOfflineMessageItemsAsyncCallback extends DiscoItemsAsyncCallback {

		public abstract void onOfflineMessageListReceived( ArrayList<Item> items );

		@Override
		public void onSuccess( Stanza responseStanza ) throws XMLException {
			final Element query = responseStanza.getChildrenNS( "query", ITEMS_XMLNS );
			List<Element> ritems = query.getChildren( "item" );
			ArrayList<Item> items = new ArrayList<Item>();
			for ( Element i : ritems ) {
				Item to = new Item();
				if ( i.getAttribute( "jid" ) != null ){
					to.setJid( JID.jidInstance( i.getAttribute( "jid" ) ) );
				}
				to.setName( i.getAttribute( "name" ) );
				to.setNode( i.getAttribute( "node" ) );
				if ( i.getAttribute( "type" ) != null ){
					to.setType( MSG_TYPE.valueOf( i.getAttribute( "type" ) ) );
				}
				items.add( to );
			}
			onOfflineMessageListReceived( items );
		}
	}

	public static class Item extends DiscoveryModule.Item {

		private MSG_TYPE type;

		public MSG_TYPE getType() {
			return type;
		}

		public void setType( MSG_TYPE type ) {
			this.type = type;
		}

		@Override
		public String toString() {
			return "Item{" + super.toString() + "type=" + type + '}';
		}
	}

	enum MSG_TYPE {

		message, presence
	}

	enum ITEM_ACTION {

		view, remove
	}


}
