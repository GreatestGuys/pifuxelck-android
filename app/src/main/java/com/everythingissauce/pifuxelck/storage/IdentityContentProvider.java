package com.everythingissauce.pifuxelck.storage;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.auth.Identity;

import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;

public class IdentityContentProvider extends ContentProvider {

  private static final String TAG = "IdentityContentProvider";

  private static final String MIME_TYPE = "application/json";

  private IdentityProvider mIdentityProvider;

  @Override
  public boolean onCreate() {
    mIdentityProvider = new IdentityProvider(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
                      String[] selectionArgs, String sortOrder) {
    Log.e(TAG, "query [1]");
    return null;
  }

  @Override
  public ParcelFileDescriptor openFile(Uri uri, String mode) {
    Log.e(TAG, "openFile [1]");
    Identity identity = mIdentityProvider.getIdentity();
    if (identity == null) {
      Log.e(TAG, "openFile [2]");
      return null;
    }

    Log.e(TAG, "openFile [3]");
    String jsonData = null;
    try {
      Log.e(TAG, "openFile [4]");
      jsonData = identity.toJson().toString();
    } catch (JSONException exception) {
      Log.e(TAG, "Unable to serialize identity as JSON.", exception);
      return null;
    }
    Log.e(TAG, "openFile [5]");

    try {
      Log.e(TAG, "openFile [6]");
      ParcelFileDescriptor[] fds = ParcelFileDescriptor.createPipe();
      OutputStream outputStream =
          new ParcelFileDescriptor.AutoCloseOutputStream(fds[1]);
      outputStream.write(jsonData.getBytes("UTF-8"));
      Log.e(TAG, "openFile [7]");
      return fds[0];
    } catch (IOException exception) {
      Log.e(TAG, "Unable to write data to file descriptor.", exception);
      return null;
    }
  }

  @Override
  public String getType(Uri uri) {
    return MIME_TYPE;
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
  public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
    return 0;
  }
}
