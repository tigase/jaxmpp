package tigase.jaxmpp.android.roster;

import android.database.sqlite.SQLiteDatabase;

public class RosterDbHelper {

	private static final String CREATE_ITEMS_TABLE = 
			"CREATE TABLE " + RosterItemsCacheTableMetaData.TABLE_NAME + " ("
			+ RosterItemsCacheTableMetaData.FIELD_ID + " INTEGER PRIMARY KEY, "
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
