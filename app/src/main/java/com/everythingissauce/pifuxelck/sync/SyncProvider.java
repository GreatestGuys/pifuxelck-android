package com.everythingissauce.pifuxelck.sync;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * The sync adapter framework requires a content provider. This class
 * satisfies that requirement by supplying the mimial definition of a content
 * provider.
 */
public class SyncProvider extends ContentProvider {
  @Override
  public boolean onCreate() {
    return false;
  }

  @Override
  public Cursor query(
      Uri uri, String[] strings, String s, String[] strings2, String s2) {
    return null;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues contentValues) {
    return null;
  }

  @Override
  public int delete(Uri uri, String s, String[] strings) {
    return 0;
  }

  @Override
  public int update(
      Uri uri, ContentValues contentValues, String s, String[] strings) {
    return 0;
  }
}
