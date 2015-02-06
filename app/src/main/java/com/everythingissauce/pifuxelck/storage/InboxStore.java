package com.everythingissauce.pifuxelck.storage;

import com.everythingissauce.pifuxelck.data.InboxEntry;
import com.everythingissauce.pifuxelck.data.Turn;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InboxStore {

  private final static String TAG = "InboxStore";

  private static final String[] QUERY_COLUMNS = new String[] {
      InboxSqlHelper.COLUMN_GAME_ID, InboxSqlHelper.COLUMN_TURN_JSON
  };

  private final InboxSqlHelper mSqlHelper;

  public InboxStore(Context context) {
    mSqlHelper = new InboxSqlHelper(context);
  }

  public void addEntry(InboxEntry entry) {
    String turnJson = null;
    try {
      turnJson = entry.getPreviousTurn().toJson().toString();
    } catch (JSONException exception) {
      Log.i(TAG, "Unable to marshal turn into JSON object.", exception);
      return;
    }

    SQLiteDatabase db = mSqlHelper.getWritableDatabase();
    try {
      ContentValues values = new ContentValues();
      values.put(InboxSqlHelper.COLUMN_GAME_ID, entry.getGameId());
      values.put(InboxSqlHelper.COLUMN_TURN_JSON, turnJson);
      db.insertWithOnConflict(
          InboxSqlHelper.TABLE_NAME,
          null,
          values,
          SQLiteDatabase.CONFLICT_REPLACE);
    } finally {
      db.close();
    }
  }

  public void clear() {
    SQLiteDatabase db = mSqlHelper.getWritableDatabase();
    try {
      db.delete(InboxSqlHelper.TABLE_NAME, null, null);
    } finally {
      db.close();
    }
  }

  public List<InboxEntry> getEntries() {
    SQLiteDatabase db = mSqlHelper.getReadableDatabase();
    try {
      Cursor cursor = db.query(
          InboxSqlHelper.TABLE_NAME,
          QUERY_COLUMNS,
          null, null, /* WHERE clause. */
          null, null, /* GROUP BY clause. */
          InboxSqlHelper.COLUMN_ID + " DESC",
          null /* LIMIT */);

      List<InboxEntry> entries = new ArrayList<InboxEntry>(cursor.getCount());
      cursor.moveToFirst();
      while (!cursor.isAfterLast()) {
        InboxEntry entry = cursorToInboxEntry(cursor);
        if (entry != null) entries.add(entry);
        cursor.moveToNext();
      }
      return entries;
    } finally {
      db.close();
    }
  }

  @Nullable
  private InboxEntry cursorToInboxEntry(Cursor cursor) {
    try {
      return new InboxEntry(
          cursor.getLong(0),  // Game ID
          Turn.fromJson(new JSONObject(cursor.getString(1))));  // Turn JSON
    } catch (JSONException exception) {
      Log.e(TAG, "Unable to un-marshal turn from JSON.", exception);
    }
    return null;
  }
}