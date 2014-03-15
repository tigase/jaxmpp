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
package tigase.jaxmpp.android.chat;

import android.database.sqlite.SQLiteDatabase;

public class OpenChatDbHelper {

	private static final String CREATE_OPEN_CHATS_TABLE = 
			"CREATE TABLE " + OpenChatTableMetaData.TABLE_NAME + " ("
			+ OpenChatTableMetaData.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ OpenChatTableMetaData.FIELD_ACCOUNT + " TEXT, "
			+ OpenChatTableMetaData.FIELD_JID + " TEXT, "
			+ OpenChatTableMetaData.FIELD_TIMESTAMP + " DATETIME, "
			+ OpenChatTableMetaData.FIELD_TYPE + " INTEGER, "
			+ OpenChatTableMetaData.FIELD_THREAD_ID + " TEXT, "
			+ OpenChatTableMetaData.FIELD_RESOURCE + " TEXT,"
			+ OpenChatTableMetaData.FIELD_NICKNAME + " TEXT, "
			+ OpenChatTableMetaData.FIELD_PASSWORD + " TEXT, "
			+ OpenChatTableMetaData.FIELD_ROOM_STATE + " TEXT"
			+ ");";
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_OPEN_CHATS_TABLE);
	}
	
	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}	
	
}
