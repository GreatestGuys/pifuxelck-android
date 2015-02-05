package com.everythingissauce.pifuxelck.api;

/**
 * A callback that wraps another callback.
 */
abstract class CallbackWrapper<T> implements Api.Callback<T> {

  private final Api.Callback<T> mCallback;

  public CallbackWrapper(Api.Callback<T> callback) {
    mCallback = callback;
  }

  public void onApiSuccess(T result) {
    mCallback.onApiSuccess(result);
  }

  public void onApiFailure() {
    mCallback.onApiFailure();
  }
}
