package com.everythingissauce.pifuxelck.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class ContactsSqlHelper extends SQLiteOpenHelper {

  private static final String TAG = "ContactsSqlHelper";

  private static final int VERSION = 1;
  private static final String DATABASE_NAME = "contacts.db";

  public static final String TABLE_NAME = "contacts";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_DISPLAY_NAME= "display_name";

  private static final String DATABASE_CREATE =
      "CREATE TABLE " + TABLE_NAME + " ("
          + COLUMN_ID + " INTEGER PRIMARY KEY, "
          + COLUMN_DISPLAY_NAME + " INTEGER "
          + ");";

  public ContactsSqlHelper(Context context) {
    super(context, DATABASE_NAME, null, VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    Log.i(TAG, "Creating contacts table with:\n" + DATABASE_CREATE);
    db.execSQL(DATABASE_CREATE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.i(TAG, "Upgrading contacts to " + newVersion + " by wiping data.");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(db);
  }
}