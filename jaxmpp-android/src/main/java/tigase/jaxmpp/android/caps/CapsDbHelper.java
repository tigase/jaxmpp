package tigase.jaxmpp.android.caps;

import android.database.sqlite.SQLiteDatabase;

public class CapsDbHelper {

	private static final String CREATE_CAPS_IDENTITIES_TABLE = 
			"CREATE TABLE " + CapsIdentitiesTableMetaData.TABLE_NAME + " ("
			+ CapsIdentitiesTableMetaData.FIELD_ID + " INTEGER PRIMARY KEY, "
			+ CapsIdentitiesTableMetaData.FIELD_NODE + " TEXT, "
			+ CapsIdentitiesTableMetaData.FIELD_NAME + " TEXT, "
			+ CapsIdentitiesTableMetaData.FIELD_CATEGORY + " TEXT, "
			+ CapsIdentitiesTableMetaData.FIELD_TYPE + " TEXT"
			+ ");";

	private static final String CREATE_CAPS_FEATURES_TABLE =
			"CREATE TABLE " + CapsFeaturesTableMetaData.TABLE_NAME + " ("
			+ CapsFeaturesTableMetaData.FIELD_ID + " INTEGER PRIMARY KEY, "
			+ CapsFeaturesTableMetaData.FIELD_NODE + " TEXT, "
			+ CapsFeaturesTableMetaData.FIELD_FEATURE + " TEXT"
			+ ");";
		
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_CAPS_IDENTITIES_TABLE);
		database.execSQL(CREATE_CAPS_FEATURES_TABLE);
		
		String sql = "CREATE INDEX IF NOT EXISTS ";
		sql += CapsFeaturesTableMetaData.TABLE_NAME + "_" + CapsFeaturesTableMetaData.FIELD_NODE + "_idx";
		sql += " ON " + CapsFeaturesTableMetaData.TABLE_NAME + " (";
		sql += CapsFeaturesTableMetaData.FIELD_NODE;
		sql += ")";
		database.execSQL(sql);   

		sql = "CREATE INDEX IF NOT EXISTS ";
		sql += CapsFeaturesTableMetaData.TABLE_NAME + "_" + CapsFeaturesTableMetaData.FIELD_FEATURE + "_idx";
		sql += " ON " + CapsFeaturesTableMetaData.TABLE_NAME + " (";
		sql += CapsFeaturesTableMetaData.FIELD_FEATURE;
		sql += ")";
		database.execSQL(sql);   

		sql = "CREATE INDEX IF NOT EXISTS ";
		sql += CapsIdentitiesTableMetaData.TABLE_NAME + "_" + CapsFeaturesTableMetaData.FIELD_NODE + "_idx";
		sql += " ON " + CapsIdentitiesTableMetaData.TABLE_NAME + " (";
		sql += CapsIdentitiesTableMetaData.FIELD_NODE;
		sql += ")";
		database.execSQL(sql);   
		
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}	
}
