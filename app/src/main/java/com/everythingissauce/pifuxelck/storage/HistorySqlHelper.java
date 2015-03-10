package com.everythingissauce.pifuxelck.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class HistorySqlHelper extends SQLiteOpenHelper {

  private static final String TAG = "HistorySqlHelper";

  private static final int VERSION = 2;
  private static final String DATABASE_NAME = "history.db";

  public static final String TABLE_NAME = "history";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_COMPLETED_AT = "completed_at";
  public static final String COLUMN_PREVIEW_TEXT = "preview_text";
  public static final String COLUMN_GAME_JSON = "game_json";

  private static final String DATABASE_CREATE =
      "CREATE TABLE " + TABLE_NAME + " ("
          + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
          + COLUMN_COMPLETED_AT + " INTEGER, "
          + COLUMN_PREVIEW_TEXT + " TEXT, "
          + COLUMN_GAME_JSON + " TEXT"
          + ");";

  public HistorySqlHelper(Context context) {
    super(context, DATABASE_NAME, null, VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    Log.i(TAG, "Creating history table with:\n" + DATABASE_CREATE);
    db.execSQL(DATABASE_CREATE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.i(TAG, "Upgrading history to " + newVersion + " by wiping data.");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(db);
  }
}
