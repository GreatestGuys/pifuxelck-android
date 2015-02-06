package com.everythingissauce.pifuxelck.storage;

import com.everythingissauce.pifuxelck.data.Contact;
import com.everythingissauce.pifuxelck.data.Game;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class ContactsStore {

  private static final String TAG = "ContactsStore";

  private static final String[] QUERY_COLUMNS = new String[]{
      ContactsSqlHelper.COLUMN_ID,
      ContactsSqlHelper.COLUMN_DISPLAY_NAME
  };

  private final Context mContext;
  private final ContactsSqlHelper mSqlHelper;

  public ContactsStore(Context context) {
    mContext = context;
    mSqlHelper = new ContactsSqlHelper(context);
  }

  public void addContact(Contact contact) {
    SQLiteDatabase db = mSqlHelper.getWritableDatabase();
    try {
      ContentValues values = new ContentValues();
      values.put(ContactsSqlHelper.COLUMN_ID, contact.getUserId());
      values.put(
          ContactsSqlHelper.COLUMN_DISPLAY_NAME, contact.getDisplayName());
      db.insert(ContactsSqlHelper.TABLE_NAME, null, values);
    } finally {
      db.close();
    }
  }

  public void removeContact(Contact contact) {
    SQLiteDatabase db = mSqlHelper.getWritableDatabase();
    try {
      db.delete(
          ContactsSqlHelper.TABLE_NAME,
          ContactsSqlHelper.COLUMN_ID + " = ?",
          new String[] {Long.toString(contact.getUserId())});
    } finally {
      db.close();
    }
  }

  public Loader<Cursor> getContactsLoader(@Nullable final String query) {
    return new AsyncTaskLoader<Cursor>(mContext) {
      @Override
      public Cursor loadInBackground() {
        return getContactsCursor(query);
      }
    };
  }

  public Cursor getContactsCursor(@Nullable String query) {
    final SQLiteDatabase db = mSqlHelper.getReadableDatabase();

    String whereClause = null;
    String[] whereClauseArguments = null;
    if (!TextUtils.isEmpty(query)) {
      whereClause =
          ContactsSqlHelper.COLUMN_DISPLAY_NAME + " LIKE CONCAT('%', ?, '%')";
      whereClauseArguments = new String[] {query};
    }

    Cursor cursor = db.query(
        ContactsSqlHelper.TABLE_NAME,
        QUERY_COLUMNS,
        whereClause, whereClauseArguments,
        null, null, /* GROUP BY clause. */
        ContactsSqlHelper.COLUMN_DISPLAY_NAME + " ASC",
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
  public static Contact cursorToContact(Cursor cursor) {
    return new Contact(cursor.getLong(0), cursor.getString(1));
  }
}