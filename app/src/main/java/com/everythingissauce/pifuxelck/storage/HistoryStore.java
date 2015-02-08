package com.everythingissauce.pifuxelck.storage;

import com.everythingissauce.pifuxelck.data.Game;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class HistoryStore {

  private static final String TAG = "HistoryStore";

  private static final String[] QUERY_COLUMNS = new String[]{
      HistorySqlHelper.COLUMN_ID,
      HistorySqlHelper.COLUMN_GAME_JSON
  };

  private static final String[] LAST_TIME_QUERY_COLUMNS = new String[]{
      HistorySqlHelper.COLUMN_COMPLETED_AT
  };

  private static final String[] SIZE_QUERY_COLUMNS = new String[]{
      "COUNT(*)"
  };

  private final Context mContext;
  private final HistorySqlHelper mSqlHelper;

  public HistoryStore(Context context) {
    mContext = context;
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
      values.put(HistorySqlHelper.COLUMN_ID, game.getId());
      values.put(HistorySqlHelper.COLUMN_COMPLETED_AT, game.getTimeCompleted());
      values.put(HistorySqlHelper.COLUMN_GAME_JSON, gameJson);
      db.insertWithOnConflict(
          HistorySqlHelper.TABLE_NAME,
          null,
          values,
          SQLiteDatabase.CONFLICT_REPLACE);
    } finally {
      db.close();
    }
  }

  public long getSize() {
    SQLiteDatabase db = mSqlHelper.getReadableDatabase();
    try {
      Cursor cursor = db.query(
          HistorySqlHelper.TABLE_NAME,
          SIZE_QUERY_COLUMNS,
          null, null, /* WHERE clause. */
          null, null, /* GROUP BY clause. */
          null /* ORDER BY */,
          null  /* LIMIT */);

      cursor.moveToFirst();
      if (!cursor.isAfterLast()) {
        return cursor.getLong(0);
      }
    } finally {
      db.close();
    }
    return 0;
  }

  public long getLastCompletedTime() {
    SQLiteDatabase db = mSqlHelper.getReadableDatabase();
    try {
      Cursor cursor = db.query(
          HistorySqlHelper.TABLE_NAME,
          LAST_TIME_QUERY_COLUMNS,
          null, null, /* WHERE clause. */
          null, null, /* GROUP BY clause. */
          HistorySqlHelper.COLUMN_COMPLETED_AT + " DESC",
          "1"  /* LIMIT */);

      cursor.moveToFirst();
      if (!cursor.isAfterLast()) {
        return cursor.getLong(0);
      }
    } finally {
      db.close();
    }
    return 0;
  }

  public Game getGame(long id) {
    final SQLiteDatabase db = mSqlHelper.getReadableDatabase();
    try {
      Cursor cursor = db.query(
          HistorySqlHelper.TABLE_NAME,
          QUERY_COLUMNS,
          HistorySqlHelper.COLUMN_ID + " = ?", new String[] {Long.toString(id)},
          null, null, /* GROUP BY clause. */
          HistorySqlHelper.COLUMN_COMPLETED_AT + " DESC",
          null /* LIMIT */);
      cursor.moveToFirst();
      return cursorToGame(cursor);
    } finally {
      db.close();
    }
  }

  public Loader<Cursor> getHistoryLoader() {
    return new AsyncTaskLoader<Cursor>(mContext) {
      @Override
      public Cursor loadInBackground() {
        return getHistoryCursor();
      }
    };
  }

  public Cursor getHistoryCursor() {
    final SQLiteDatabase db = mSqlHelper.getReadableDatabase();
    Cursor cursor = db.query(
          HistorySqlHelper.TABLE_NAME,
          QUERY_COLUMNS,
          null, null, /* WHERE clause. */
          null, null, /* GROUP BY clause. */
          HistorySqlHelper.COLUMN_COMPLETED_AT + " DESC",
          null /* LIMIT */);

    // Return the cursor in a wrapper that will also close the database when
    // the cursor is released.
    return new CursorWrapper(cursor) {
      @Override
      public void close() {
        super.close();
        db.close();
      }
    };
  }

  @Nullable
  public static Game cursorToGame(Cursor cursor) {
    if (cursor.isAfterLast() || cursor.isBeforeFirst()) {
      return null;
    }

    try {
      return Game.fromJson(new JSONObject(cursor.getString(1)));
    } catch (JSONException exception) {
      Log.e(TAG, "Unable to un-marshal game from JSON.", exception);
    }
    return null;
  }
}