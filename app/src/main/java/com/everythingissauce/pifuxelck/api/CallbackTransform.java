package com.everythingissauce.pifuxelck.api;

import android.util.Log;

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

  public void onApiSuccess(U result) {
    try {
      mCallback.onApiSuccess(transform(result));
    } catch (Exception exception) {
      Log.e(TAG, "Unable to apply callback transformation.", exception);
      mCallback.onApiFailure();
    }
  }

  public void onApiFailure() {
    mCallback.onApiFailure();
  }
}
