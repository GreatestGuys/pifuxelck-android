package com.everythingissauce.pifuxelck.api;

/**
 * A callback that maps from results of one type onto another.
 */
abstract class CallbackTransform<U, V> implements Api.Callback<U> {

  private final Api.Callback<V> mCallback;

  public CallbackTransform(Api.Callback<V> callback) {
    mCallback = callback;
  }

  public abstract V transform(U result) throws Exception;

  public void onApiSuccess(U result) {
    try {
      mCallback.onApiSuccess(transform(result));
    } catch (Exception exception) {
      mCallback.onApiFailure();
    }
  }

  public void onApiFailure() {
    mCallback.onApiFailure();
  }
}
