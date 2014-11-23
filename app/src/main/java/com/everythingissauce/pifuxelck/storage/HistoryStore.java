package com.everythingissauce.pifuxelck.storage;

import com.everythingissauce.pifuxelck.data.Game;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class HistoryStore {

  private final static String TAG = "HistoryStore";

  private final HistorySqlHelper mSqlHelper;

  public HistoryStore(Context context) {
    mSqlHelper = new HistorySqlHelper(context);
  }

  public void addGame(Game game) {
    String gameJson = null;
    try {
      gameJson = game.toJson().toString();
    } catch (JSONException exception) {
      Log.i(TAG, "Unable to marshal game into JSON object.", exception);
      return;
    }

    SQLiteDatabase db = mSqlHelper.getWritableDatabase();
    try {
      ContentValues values = new ContentValues();
      values.put(HistorySqlHelper.COLUMN_COMPLETED_AT, game.getTimeCompleted());
      values.put(HistorySqlHelper.COLUMN_GAME_JSON, gameJson);
      db.insert(HistorySqlHelper.TABLE_NAME, null, values);
    } finally {
      db.close();
    }
  }

  public Cursor getHistory() {
    SQLiteDatabase db = mSqlHelper.getWritableDatabase();
    try {
      return db.query(
          HistorySqlHelper.TABLE_NAME,
          new String[]{HistorySqlHelper.COLUMN_GAME_JSON},
          null, null, /* WHERE clause. */
          null, null, /* GROUP BY clause. */
          HistorySqlHelper.COLUMN_COMPLETED_AT + " DESC",
          null /* LIMIT */);
    } finally {
      db.close();
    }
  }

  @Nullable
  public static Game cursorToGame(Cursor cursor) {
    try {
      Game.fromJson(new JSONObject(cursor.getString(0)));
    } catch (JSONException exception) {
      Log.e(TAG, "Unable to un-marshal game from JSON.", exception);
    }
    return null;
  }
}