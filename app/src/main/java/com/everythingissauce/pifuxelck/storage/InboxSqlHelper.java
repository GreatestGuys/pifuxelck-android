package com.everythingissauce.pifuxelck.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class InboxSqlHelper extends SQLiteOpenHelper {

  private static final String TAG = "InboxSqlHelper";

  private static final int VERSION = 1;
  private static final String DATABASE_NAME = "inbox.db";

  public static final String TABLE_NAME = "inbox";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_GAME_ID = "game_id";
  public static final String COLUMN_TURN_JSON = "turn_json";

  private static final String DATABASE_CREATE =
      "CREATE TABLE " + TABLE_NAME + " ("
          + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
          + COLUMN_GAME_ID + " INTEGER NOT NULL, "
          + COLUMN_TURN_JSON + " TEXT NOT NULL"
          + ");";

  public InboxSqlHelper(Context context) {
    super(context, DATABASE_NAME, null, VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    Log.i(TAG, "Creating inbox table with:\n" + DATABASE_CREATE);
    db.execSQL(DATABASE_CREATE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.i(TAG, "Upgrading inbox to " + newVersion + " by wiping data.");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(db);
  }
}
