/*
 * PubSubErrorCondition.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
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
package tigase.jaxmpp.core.client.xmpp.modules.pubsub;

public enum PubSubErrorCondition {

	closed_node,
	configuration_required,
	conflict,
	invalid_jid,
	invalid_payload,
	invalid_subid,
	item_required,
	jid_required,
	node_required,
	nodeid_required,
	not_in_roster_group,
	not_subscribed,
	payload_required,
	payload_too_big,
	pending_subscription,
	presence_subscription_required,
	subid_required,
	too_many_subscriptions,
	unsupported,
	unsupported_access_authorize,
	unsupported_access_model,
	unsupported_access_open,
	unsupported_access_presence,
	unsupported_access_roster,
	unsupported_access_whitelist,
	unsupported_auto_create,
	unsupported_auto_subscribe,
	unsupported_collections,
	unsupported_config_node,
	unsupported_create_and_configure,
	unsupported_create_nodes,
	unsupported_delete_items,
	unsupported_delete_nodes,
	unsupported_filtered_notifications,
	unsupported_get_pending,
	unsupported_instant_nodes,
	unsupported_item_ids,
	unsupported_last_published,
	unsupported_leased_subscription,
	unsupported_manage_subscriptions,
	unsupported_member_affiliation,
	unsupported_meta_data,
	unsupported_modify_affiliations,
	unsupported_multi_collection,
	unsupported_multi_subscribe,
	unsupported_outcast_affiliation,
	unsupported_persistent_items,
	unsupported_presence_notifications,
	unsupported_presence_subscribe,
	unsupported_publish,
	unsupported_publish_only_affiliation,
	unsupported_publish_options,
	unsupported_publisher_affiliation,
	unsupported_purge_nodes,
	unsupported_retract_items,
	unsupported_retrieve_affiliations,
	unsupported_retrieve_default,
	unsupported_retrieve_items,
	unsupported_retrieve_subscriptions,
	unsupported_subscribe,
	unsupported_subscription_notifications,
	unsupported_subscription_options;
}