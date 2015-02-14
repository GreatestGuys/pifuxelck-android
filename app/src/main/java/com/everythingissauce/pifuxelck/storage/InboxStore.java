package com.everythingissauce.pifuxelck.storage;

import com.everythingissauce.pifuxelck.data.InboxEntry;
import com.everythingissauce.pifuxelck.data.Turn;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InboxStore {

  private final static String TAG = "InboxStore";

  private static final String[] ENTRY_QUERY_COLUMNS = new String[] {
      InboxSqlHelper.COLUMN_GAME_ID,
      InboxSqlHelper.COLUMN_TURN_JSON,
      InboxSqlHelper.COLUMN_REPLY_JSON
  };

  private static final String[] SIZE_QUERY_COLUMNS = new String[]{
      "COUNT(*)"
  };

  private static final String[] ID_QUERY_COLUMNS = new String[] {
      InboxSqlHelper.COLUMN_GAME_ID
  };

  private final InboxSqlHelper mSqlHelper;

  public InboxStore(Context context) {
    mSqlHelper = new InboxSqlHelper(context);
  }

  public int getSize() {
    SQLiteDatabase db = mSqlHelper.getReadableDatabase();
    try {
      Cursor cursor = db.query(
          InboxSqlHelper.TABLE_NAME,
          SIZE_QUERY_COLUMNS,
          null, null, /* WHERE clause. */
          null, null, /* GROUP BY clause. */
          null /* ORDER BY */,
          null  /* LIMIT */);

      cursor.moveToFirst();
      if (!cursor.isAfterLast()) {
        return cursor.getInt(0);
      }
    } finally {
      db.close();
    }
    return 0;
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

  public void updateEntryWithReply(long gameId, Turn reply) {
    String replyJson = null;
    try {
      replyJson = reply.toJson().toString();
    } catch (JSONException exception) {
      Log.i(TAG, "Unable to marshal turn into JSON object.", exception);
      return;
    }

    SQLiteDatabase db = mSqlHelper.getWritableDatabase();
    try {
      ContentValues values = new ContentValues();
      values.put(InboxSqlHelper.COLUMN_REPLY_JSON, replyJson);
      db.updateWithOnConflict(
          InboxSqlHelper.TABLE_NAME,
          values,
          InboxSqlHelper.COLUMN_GAME_ID + " = ?",
          new String[]{Long.toString(gameId)},
          SQLiteDatabase.CONFLICT_REPLACE);
    } finally {
      db.close();
    }
  }

  public void remove(long gameId) {
    SQLiteDatabase db = mSqlHelper.getWritableDatabase();
    try {
      db.delete(
          InboxSqlHelper.TABLE_NAME,
          InboxSqlHelper.COLUMN_GAME_ID + " = ?",
          new String[] {Long.toString(gameId)});
    } finally {
      db.close();
    }
  }

  public List<Long> getEntryIds() {
    SQLiteDatabase db = mSqlHelper.getReadableDatabase();
    try {
      Cursor cursor = db.query(
          InboxSqlHelper.TABLE_NAME,
          ID_QUERY_COLUMNS,
          null, null, /* WHERE clause. */
          null, null, /* GROUP BY clause. */
          InboxSqlHelper.COLUMN_ID + " DESC",
          null /* LIMIT */);

      List<Long> ids = new ArrayList<Long>(cursor.getCount());
      cursor.moveToFirst();
      while (!cursor.isAfterLast()) {
        ids.add(cursor.getLong(0));
        cursor.moveToNext();
      }
      return ids;
    } finally {
      db.close();
    }
  }

  public List<InboxEntry> getEntries() {
    SQLiteDatabase db = mSqlHelper.getReadableDatabase();
    try {
      Cursor cursor = db.query(
          InboxSqlHelper.TABLE_NAME,
          ENTRY_QUERY_COLUMNS,
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
      String replyString = cursor.getString(2);
      Turn replyTurn = null;
      if (!TextUtils.isEmpty(replyString)) {
        replyTurn = Turn.fromJson(new JSONObject(replyString));
      }

      return new InboxEntry(
          cursor.getLong(0),  // Game ID
          Turn.fromJson(new JSONObject(cursor.getString(1))),  // Turn JSON
          replyTurn);
    } catch (JSONException exception) {
      Log.e(TAG, "Unable to un-marshal turn from JSON.", exception);
    }
    return null;
  }
}