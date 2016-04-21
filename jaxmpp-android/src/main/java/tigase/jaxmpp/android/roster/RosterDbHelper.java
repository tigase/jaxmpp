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

import android.database.sqlite.SQLiteDatabase;

public class RosterDbHelper {

	private static final String CREATE_ITEMS_TABLE = 
			"CREATE TABLE " + RosterItemsCacheTableMetaData.TABLE_NAME + " ("
			+ RosterItemsCacheTableMetaData.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
			+ RosterItemsCacheTableMetaData.FIELD_ACCOUNT + " TEXT, "
			+ RosterItemsCacheTableMetaData.FIELD_JID + " TEXT, "
			+ RosterItemsCacheTableMetaData.FIELD_NAME + " TEXT, "
			+ RosterItemsCacheTableMetaData.FIELD_ASK + " BOOLEAN, "
			+ RosterItemsCacheTableMetaData.FIELD_SUBSCRIPTION + " TEXT, "
			+ RosterItemsCacheTableMetaData.FIELD_TIMESTAMP + " DATETIME"
			+ ");";
	
	private static final String CREATE_GROUPS_TABLE = 
			"CREATE TABLE " + RosterGroupsCacheTableMetaData.TABLE_NAME + " ("
			+ RosterGroupsCacheTableMetaData.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ RosterGroupsCacheTableMetaData.FIELD_NAME + " TEXT NOT NULL"
			+ ");";
	
	private static final String CREATE_ITEMS_GROUPS_TABLE =
			"CREATE TABLE " + RosterItemsGroupsCacheTableMetaData.TABLE_NAME + " ("
			+ RosterItemsGroupsCacheTableMetaData.FIELD_ITEM + " INTEGER, "
			+ RosterItemsGroupsCacheTableMetaData.FIELD_GROUP + " INTEGER, "
			+ "FOREIGN KEY(" + RosterItemsGroupsCacheTableMetaData.FIELD_ITEM + ") REFERENCES " 
			+ RosterItemsCacheTableMetaData.TABLE_NAME + "(" + RosterItemsCacheTableMetaData.FIELD_ID + "),"
			+ "FOREIGN KEY(" + RosterItemsGroupsCacheTableMetaData.FIELD_GROUP + ") REFERENCES " 
			+ RosterGroupsCacheTableMetaData.TABLE_NAME + "(" + RosterGroupsCacheTableMetaData.FIELD_ID + ")"
			+ ");";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_ITEMS_TABLE);
		database.execSQL(CREATE_GROUPS_TABLE);
		database.execSQL(CREATE_ITEMS_GROUPS_TABLE);
	}
	
	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
}
