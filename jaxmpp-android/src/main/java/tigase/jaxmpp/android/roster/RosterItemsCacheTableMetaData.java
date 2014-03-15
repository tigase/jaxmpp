/*
 * Tigase XMPP Client Library
 * Copyright (C) 2014 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
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
package tigase.jaxmpp.android.roster;

import android.provider.BaseColumns;

public class RosterItemsCacheTableMetaData implements BaseColumns {

	public static final String FIELD_ACCOUNT = "account";

	public static final String FIELD_ASK = "ask";

	public static final String FIELD_ID = "_id";

	public static final String FIELD_JID = "jid";

	public static final String FIELD_NAME = "name";

	public static final String FIELD_SUBSCRIPTION = "subscription";

	public static final String FIELD_TIMESTAMP = "timestamp";

	public static final String TABLE_NAME = "roster_items";
	
}
