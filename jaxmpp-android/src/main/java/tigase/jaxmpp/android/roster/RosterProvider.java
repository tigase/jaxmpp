package tigase.jaxmpp.android.roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import tigase.jaxmpp.core.client.BareJID;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterCacheProvider;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem;
import tigase.jaxmpp.core.client.xmpp.modules.roster.RosterItem.Subscription;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

public class RosterProvider implements RosterCacheProvider {

	private final Context context;
	private final SQLiteOpenHelper dbHelper;
	private SharedPreferences prefs;
	private final String versionKeyPrefix;

	public RosterProvider(Context context, SQLiteOpenHelper dbHelper, String versionKeyPrefix) {
		this.context = context;
		this.dbHelper = dbHelper;
		this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.versionKeyPrefix = versionKeyPrefix;
	}
	
	public Set<String> addItem(SessionObject sessionObject, RosterItem rosterItem) {
		Set<String> addedGroups = null;
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentValues v = new ContentValues();

			v.put(RosterItemsCacheTableMetaData.FIELD_ID, rosterItem.getId());
			v.put(RosterItemsCacheTableMetaData.FIELD_JID, rosterItem.getJid().toString());
			v.put(RosterItemsCacheTableMetaData.FIELD_ACCOUNT, sessionObject.getUserBareJid().toString());
			v.put(RosterItemsCacheTableMetaData.FIELD_NAME, rosterItem.getName());
			v.put(RosterItemsCacheTableMetaData.FIELD_SUBSCRIPTION, rosterItem.getSubscription().name());
			v.put(RosterItemsCacheTableMetaData.FIELD_ASK, rosterItem.isAsk());
			v.put(RosterItemsCacheTableMetaData.FIELD_TIMESTAMP, (new Date()).getTime());
			
			db.insert(RosterItemsCacheTableMetaData.TABLE_NAME, null, v);
			
			addedGroups = updateRosterItemGroups(db, rosterItem);
			
			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}
		
		return addedGroups;
	}

	public RosterItem  getItem(SessionObject sessionObject, BareJID jid) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.query(RosterItemsCacheTableMetaData.TABLE_NAME, new String[] { 
				RosterItemsCacheTableMetaData.FIELD_ID, 
				RosterItemsCacheTableMetaData.FIELD_NAME, RosterItemsCacheTableMetaData.FIELD_SUBSCRIPTION,
				RosterItemsCacheTableMetaData.FIELD_ASK, RosterItemsCacheTableMetaData.FIELD_TIMESTAMP, 
			}, 
			RosterItemsCacheTableMetaData.FIELD_ACCOUNT + " = '?' and " + RosterItemsCacheTableMetaData.FIELD_JID + " = '?'", 
			new String[] { DatabaseUtils.sqlEscapeString(sessionObject.getUserBareJid().toString()), 
			DatabaseUtils.sqlEscapeString(jid.toString())}, null, null, null);

		try {
			if (c.moveToNext()) {
				RosterItem rosterItem = new RosterItem(jid, sessionObject);
				rosterItem.setName(c.getString(1));
				rosterItem.setSubscription(Subscription.valueOf(c.getString(2)));
				rosterItem.setAsk(c.getInt(3) == 1);
				rosterItem.setData(RosterItem.ID_KEY, c.getLong(0));

				c.close();
				
				List<String> groups = rosterItem.getGroups();
				c = db.rawQuery("SELECT " + RosterGroupsCacheTableMetaData.FIELD_ID + ", " + RosterGroupsCacheTableMetaData.FIELD_NAME 
						+ " FROM " + RosterGroupsCacheTableMetaData.TABLE_NAME + " g "
						+ " INNER JOIN " + RosterItemsGroupsCacheTableMetaData.TABLE_NAME + " gi "
						+ "ON g." + RosterGroupsCacheTableMetaData.FIELD_ID + " = gi." + RosterItemsGroupsCacheTableMetaData.FIELD_GROUP
						+ " WHERE gi." + RosterItemsGroupsCacheTableMetaData.FIELD_ITEM + " = ?", new String[] { String.valueOf(rosterItem.getId()) });
				
				while (c.moveToNext()) {
					groups.add(c.getString(1));
				}
				
				return rosterItem;
			}
			else {
				return null;
			}
		}
		finally {
			if (c != null && !c.isClosed())
				c.close();
		}
		
	}
	
	public void removeItem(SessionObject sessionObject, RosterItem rosterItem) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			db.delete(RosterItemsGroupsCacheTableMetaData.TABLE_NAME, RosterItemsGroupsCacheTableMetaData.FIELD_ITEM + " = ?", 
					new String[] { String.valueOf(rosterItem.getId()) });
			db.delete(RosterItemsCacheTableMetaData.TABLE_NAME, RosterItemsCacheTableMetaData.FIELD_ID + " = ?", 
					new String[] { String.valueOf(rosterItem.getId()) });			
			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}			
	}
	
	public Collection<? extends String> getGroups(SessionObject sessionObject) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT DISTINCT g." + RosterGroupsCacheTableMetaData.FIELD_NAME + " FROM " + RosterGroupsCacheTableMetaData.TABLE_NAME + " g "
				+ " INNER JOIN " + RosterItemsGroupsCacheTableMetaData.TABLE_NAME + " gi "
					+ " ON g." + RosterGroupsCacheTableMetaData.FIELD_ID + " = gi." + RosterItemsGroupsCacheTableMetaData.FIELD_GROUP
				+ " INNER JOIN " + RosterItemsCacheTableMetaData.TABLE_NAME + " i "
					+ " ON i." + RosterItemsCacheTableMetaData.FIELD_ID + " = gi." + RosterItemsGroupsCacheTableMetaData.FIELD_ITEM
				+ " WHERE i." + RosterItemsCacheTableMetaData.FIELD_ACCOUNT + " = '?'"
				, new String[] { DatabaseUtils.sqlEscapeString(sessionObject.getUserBareJid().toString()) });
		try {
			List<String> groups = new ArrayList<String>();
			while (c.moveToNext()) {
				groups.add(c.getString(0));
			}
			return groups;
		}
		finally {
			c.close();
		}
	}
	
	public int getCount(SessionObject sessionObject) {
		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT count(" + RosterItemsCacheTableMetaData.FIELD_ID + ") FROM " + RosterItemsCacheTableMetaData.TABLE_NAME 
				+ " WHERE " + RosterItemsCacheTableMetaData.FIELD_ACCOUNT + " = '?'", 
				new String[] { DatabaseUtils.sqlEscapeString(sessionObject.getUserBareJid().toString()) });
		try {
			if (c.moveToNext()) {
				return c.getInt(0);
			}
			
			// should not enter here
			return 0;
		}
		finally {
			c.close();
		}
	}
	
	public void removeAll(SessionObject sessionObject) {
		final SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			db.execSQL("DELETE FROM " + RosterItemsGroupsCacheTableMetaData.TABLE_NAME 
					+ " WHERE " + RosterItemsGroupsCacheTableMetaData.FIELD_ITEM + " IN ("
						+ "SELECT " + RosterItemsCacheTableMetaData.FIELD_ID + " FROM " + RosterItemsCacheTableMetaData.FIELD_ACCOUNT + " = '?'"
					+ ")", new String[] { DatabaseUtils.sqlEscapeString(sessionObject.getUserBareJid().toString()) });
			db.delete(RosterItemsCacheTableMetaData.TABLE_NAME, RosterItemsCacheTableMetaData.FIELD_ACCOUNT + " = '?'", 
					new String[] { DatabaseUtils.sqlEscapeString(sessionObject.getUserBareJid().toString()) });			
			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}
	}
	
	private void removeEmptyGroups(SQLiteDatabase db) {
		db.delete(RosterGroupsCacheTableMetaData.TABLE_NAME, RosterGroupsCacheTableMetaData.FIELD_ID + " NOT IN ("
				+ "SELECT " + RosterItemsGroupsCacheTableMetaData.FIELD_GROUP + " FROM " + RosterItemsGroupsCacheTableMetaData.FIELD_GROUP
				+ ")", new String[0]);
	}
	
	private Set<String> updateRosterItemGroups(final SQLiteDatabase db, RosterItem rosterItem) {
		Map<String,Long> groupIds = new HashMap<String,Long>();
		
		// retrieve current groups
		Cursor c = db.rawQuery("SELECT " + RosterGroupsCacheTableMetaData.FIELD_ID + ", " + RosterGroupsCacheTableMetaData.FIELD_NAME 
				+ " FROM " + RosterGroupsCacheTableMetaData.TABLE_NAME + " g "
				+ " INNER JOIN " + RosterItemsGroupsCacheTableMetaData.TABLE_NAME + " gi "
						+ " ON g." + RosterGroupsCacheTableMetaData.FIELD_ID + " = gi." + RosterItemsGroupsCacheTableMetaData.FIELD_GROUP
				+ " WHERE gi." + RosterItemsGroupsCacheTableMetaData.FIELD_ITEM + " = ?", new String[] { String.valueOf(rosterItem.getId()) });
		
		try {
			while (c.moveToNext()) {
				groupIds.put(c.getString(1), c.getLong(0));
			}
			c.close();
			
			// remove groups from which roster item was removed
			Set<String> toRemove = groupIds.keySet();
			toRemove.removeAll(rosterItem.getGroups());
			if (!toRemove.isEmpty()) {
				StringBuilder groupsToRemove = new StringBuilder();
				groupsToRemove.append("0");
				for (String group : toRemove) {
					groupsToRemove.append(",");
					groupsToRemove.append(groupIds.get(group));
				}
			
				db.execSQL("DELETE FROM " + RosterItemsGroupsCacheTableMetaData.TABLE_NAME + " WHERE " 
					+ RosterItemsGroupsCacheTableMetaData.FIELD_ITEM + " = " + rosterItem.getId() 
					+ " AND " + RosterItemsGroupsCacheTableMetaData.FIELD_GROUP + " IN (?)", 
					new String[] { groupsToRemove.toString() });
			}
			
			Set<String> addedGroups = new HashSet<String>();
			// add new groups
			if (rosterItem.getGroups() != null) {
				List<String> toAdd = new ArrayList<String>(rosterItem.getGroups());
				toAdd.removeAll(groupIds.keySet());
				if (!toAdd.isEmpty()) {
					StringBuilder toAddAll = new StringBuilder();
					for (String group : toAdd) {
						if (toAddAll.length() > 0) {
							toAddAll.append(",");
						}
						toAddAll.append("'");
						toAddAll.append(DatabaseUtils.sqlEscapeString(group));
						toAddAll.append("'");
					}
					
					// query for ids of existing groups
					c = db.rawQuery("SELECT " + RosterGroupsCacheTableMetaData.FIELD_ID + ", " + RosterGroupsCacheTableMetaData.FIELD_NAME 
					+ " FROM " + RosterGroupsCacheTableMetaData.TABLE_NAME + " g WHERE g.name IN (?)" , new String[] { toAddAll.toString() });
					List<Long> toAddIds = new ArrayList<Long>();
					while (c.moveToNext()) {
						toAddIds.add(c.getLong(0));
						toAdd.remove(c.getString(1));
						addedGroups.add(c.getString(1));
					}
					
					// create non existing groups
					for (String group : toAdd) {
						ContentValues v = new ContentValues();				
						v.put(RosterGroupsCacheTableMetaData.FIELD_NAME, group);
						long id = db.insert(RosterGroupsCacheTableMetaData.TABLE_NAME, null, v);
						toAddIds.add(id);
						addedGroups.add(group);
					}
					
					// create relations to added groups
					for (long id : toAddIds) {
						ContentValues v = new ContentValues();
						v.put(RosterItemsGroupsCacheTableMetaData.FIELD_ITEM, rosterItem.getId());
						v.put(RosterItemsGroupsCacheTableMetaData.FIELD_GROUP, id);
						db.insert(RosterItemsGroupsCacheTableMetaData.TABLE_NAME, null, v);
					}
				}
			}
			return addedGroups;
		}
		finally {
			if (c != null && !c.isClosed()) c.close();
		}
	}

	@Override
	public String getCachedVersion(SessionObject sessionObject) {
		return prefs.getString(createKey(sessionObject), "");
	}

	@Override
	public Collection<RosterItem> loadCachedRoster(SessionObject sessionObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateReceivedVersion(SessionObject sessionObject, String ver) {
		prefs.edit().putString(createKey(sessionObject), ver).commit();	
	}
	
	private String createKey(SessionObject sessionObject) {
		return versionKeyPrefix + "." + sessionObject.getUserBareJid();
	}	
}
