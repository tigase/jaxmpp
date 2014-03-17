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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.JID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatProvider {

	private final Context context;
	private final SQLiteOpenHelper dbHelper;
	
	public ChatProvider(Context context, SQLiteOpenHelper dbHelper) {
		this.context = context;
		this.dbHelper = dbHelper;		
	}
	
	public boolean close(SessionObject sessionObject, long chatId) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			int deleted = db.delete(OpenChatTableMetaData.TABLE_NAME, OpenChatTableMetaData.FIELD_ID + " = ?", 
					new String[] { String.valueOf(chatId) });
			db.setTransactionSuccessful();
			return deleted > 0;
		}
		finally {
			db.endTransaction();
		}
	}

	public long createChat(SessionObject sessionObject, JID fromJid, String threadId) throws JaxmppException {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();
		values.put(OpenChatTableMetaData.FIELD_ACCOUNT, sessionObject.getUserBareJid().toString());
		values.put(OpenChatTableMetaData.FIELD_JID, fromJid.getBareJid().toString());
		values.put(OpenChatTableMetaData.FIELD_TIMESTAMP, (new Date()).getTime());
		
		if (fromJid.getResource() != null) {
			values.put(OpenChatTableMetaData.FIELD_RESOURCE, fromJid.getResource());
		}
		if (threadId != null) {
			values.put(OpenChatTableMetaData.FIELD_THREAD_ID, threadId);
		}
		
		return db.insert(OpenChatTableMetaData.TABLE_NAME, null, values);
	}

	public long creteMuc(SessionObject sessionObject, JID fromJid, String nickname, String password) throws JaxmppException {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		final ContentValues values = new ContentValues();
		values.put(OpenChatTableMetaData.FIELD_ACCOUNT, sessionObject.getUserBareJid().toString());
		values.put(OpenChatTableMetaData.FIELD_JID, fromJid.getBareJid().toString());
		values.put(OpenChatTableMetaData.FIELD_TIMESTAMP, (new Date()).getTime());
		
		if (nickname != null) {
			values.put(OpenChatTableMetaData.FIELD_NICKNAME, nickname);
		}
		if (password != null) {
			values.put(OpenChatTableMetaData.FIELD_PASSWORD, password);
		}
		
		return db.insert(OpenChatTableMetaData.TABLE_NAME, null, values);
	}

	/**
	 * Get parameters needed to create proper Chat instance from DB
	 * @param sessionObject
	 * @param jid
	 * @param threadId
	 * @return Array of objects { Long id, String threadId, String resourceId }
	 */
	public Object[] getChat(SessionObject sessionObject, JID jid, String threadId) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		if (threadId != null) {
			Cursor c = db.query(OpenChatTableMetaData.TABLE_NAME, new String[] { OpenChatTableMetaData.FIELD_ID, OpenChatTableMetaData.FIELD_RESOURCE }, 
					OpenChatTableMetaData.FIELD_ACCOUNT + " = ? and " + OpenChatTableMetaData.FIELD_JID + " = ? and " + OpenChatTableMetaData.FIELD_TYPE 
					+ " = 0 and " + OpenChatTableMetaData.FIELD_THREAD_ID + " = ?", 
					new String[] { 
						DatabaseUtils.sqlEscapeString(sessionObject.getUserBareJid().toString()),
						DatabaseUtils.sqlEscapeString(jid.getBareJid().toString()),
						DatabaseUtils.sqlEscapeString(threadId)
					}, null, null, null, null);
			try {
				if (c.moveToNext()) {
					return new Object[] { c.getLong(0), threadId, c.getString(1) };
				}
			} finally {
				c.close();
			}
		}
		if (jid.getResource() != null) {
			Cursor c = db.query(OpenChatTableMetaData.TABLE_NAME, new String[] { OpenChatTableMetaData.FIELD_ID, OpenChatTableMetaData.FIELD_THREAD_ID }, 
					OpenChatTableMetaData.FIELD_ACCOUNT + " = ? and " + OpenChatTableMetaData.FIELD_JID + " = ? and " + OpenChatTableMetaData.FIELD_TYPE 
					+ " = 0 and " + OpenChatTableMetaData.FIELD_RESOURCE + " = ?", 
					new String[] { 
						DatabaseUtils.sqlEscapeString(sessionObject.getUserBareJid().toString()),
						DatabaseUtils.sqlEscapeString(jid.getBareJid().toString()),
						DatabaseUtils.sqlEscapeString(jid.getResource())
					}, null, null, null, null);
			try {
				if (c.moveToNext()) {
					return new Object[] { c.getLong(0), c.getString(1), jid.getResource() };
				}
			} finally {
				c.close();
			}			
		}
		Cursor c = db.query(OpenChatTableMetaData.TABLE_NAME, new String[] { OpenChatTableMetaData.FIELD_ID, OpenChatTableMetaData.FIELD_THREAD_ID, 
				OpenChatTableMetaData.FIELD_RESOURCE }, OpenChatTableMetaData.FIELD_ACCOUNT + " = ? and " + OpenChatTableMetaData.FIELD_JID 
				+ " = ? and " + OpenChatTableMetaData.FIELD_TYPE + " = 0 and " + OpenChatTableMetaData.FIELD_THREAD_ID + " = ?", 
				new String[] { 
					DatabaseUtils.sqlEscapeString(sessionObject.getUserBareJid().toString()),
					DatabaseUtils.sqlEscapeString(jid.getBareJid().toString()),
					DatabaseUtils.sqlEscapeString(threadId)
				}, null, null, null, null);
		try {
			if (c.moveToNext()) {
				return new Object[] { c.getLong(0), c.getString(1), c.getString(2) };
			}
		} finally {
			c.close();
		}		
		return null;
	}

	/**
	 * Get parameters needed to create proper Chat instances from DB
	 * @param sessionObject
	 * @return List of arrays of objects { Long id, BareJID jid, String threadId, String resourceId }
	 */
	public List<Object[]> getChats(SessionObject sessionObject) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		List<Object[]> chats = new ArrayList<Object[]>();
		Cursor c = db.query(OpenChatTableMetaData.TABLE_NAME, new String[] { OpenChatTableMetaData.FIELD_ID, OpenChatTableMetaData.FIELD_JID, 
				OpenChatTableMetaData.FIELD_THREAD_ID, OpenChatTableMetaData.FIELD_RESOURCE }, OpenChatTableMetaData.FIELD_ACCOUNT + 
				" = ? and " + OpenChatTableMetaData.FIELD_TYPE + " = 0", 
				new String[] { 
					DatabaseUtils.sqlEscapeString(sessionObject.getUserBareJid().toString())
				}, null, null, null, null);
		try {
			while (c.moveToNext()) {
				chats.add(new Object[] { c.getLong(0),  BareJID.bareJIDInstance(c.getString(1)), c.getString(2), c.getString(3) });
			}
		} finally {
			c.close();
		}			
		return chats;
	}

	public boolean isChatOpenFor(SessionObject sessionObject, BareJID jid) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(OpenChatTableMetaData.TABLE_NAME, new String[] { OpenChatTableMetaData.FIELD_ID }, OpenChatTableMetaData.FIELD_ACCOUNT + 
				" = ? and " + OpenChatTableMetaData.FIELD_TYPE + " = 0 and " + OpenChatTableMetaData.FIELD_JID + " = ?", 
				new String[] { 
					DatabaseUtils.sqlEscapeString(sessionObject.getUserBareJid().toString()),
					DatabaseUtils.sqlEscapeString(jid.toString()),
				}, null, null, null, null);
		try {
			if (c.moveToNext()) {
				return true;
			}
		} finally {
			c.close();
		}			
		return false;
	}	
}
