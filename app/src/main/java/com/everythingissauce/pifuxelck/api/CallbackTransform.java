package com.everythingissauce.pifuxelck.api;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

/**
 * A callback that maps from results of one type onto another.
 */
abstract class CallbackTransform<U, V> implements Api.Callback<U> {

  private static final String TAG = "CallbackTransform";

  /**
   * A Transformation that maps Strings to Void objects.
   */
  public static <U> Api.Callback<U> voidTransform(Api.Callback<Void> callback) {
    return new CallbackTransform<U, Void>(callback) {
      @Override
      public Void transform(U result) throws Exception {
        return null;
      }
    };
  }

  private final Api.Callback<V> mCallback;

  public CallbackTransform(Api.Callback<V> callback) {
    mCallback = callback;
  }

  public abstract V transform(U result) throws Exception;

  public void onApiSuccess(final U result) {
    new AsyncTask<Void, Void, Pair<V, Exception>>() {
      @Override
      protected Pair<V, Exception> doInBackground(Void... voids) {
        try {
          return Pair.create(transform(result), null);
        } catch (Exception exception) {
          Log.e(TAG, "Unable to apply callback transformation.", exception);
          return Pair.create(null, exception);
        }
      }

      @Override
      public void onPostExecute(Pair<V, Exception> result) {
        if (result.second != null) {
          mCallback.onApiFailure();
        } else {
          mCallback.onApiSuccess(result.first);
        }
      }
    }.execute();
  }

  public void onApiFailure() {
    mCallback.onApiFailure();
  }
}
