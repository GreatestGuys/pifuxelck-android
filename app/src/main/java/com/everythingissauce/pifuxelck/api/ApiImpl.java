package com.everythingissauce.pifuxelck.api;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.everythingissauce.pifuxelck.auth.Identity.Partial;
import com.everythingissauce.pifuxelck.auth.Identity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents the API that is used to communicate with an abstract backend.
 */
class ApiImpl implements Api {

  private static final String THREAD_NAME = "Api Thread";

  private static final String REGISTER_END_POINT = "/account";
  private static final String REGISTER_DISPLAY_NAME = "display_name";
  private static final String REGISTER_KEY_OBJECT = "public_key";
  private static final String REGISTER_EXPONENT = "exponent";
  private static final String REGISTER_MODULUS = "modulus";
  private static final String REGISTER_PHONE = "hashed_phone";

  private final HttpRequestFactory mHttpRequestFactory;
  private final HandlerThread mHandlerThread;
  private final Handler mHandler;
  private final Handler mMainHandler;

  private final Object mAuthTokenLock = new Object();
  private String mAuthToken = null;

  public ApiImpl(HttpRequestFactory httpRequestFactory) {
    mHttpRequestFactory = httpRequestFactory;

    mHandlerThread = new HandlerThread(THREAD_NAME);
    mHandlerThread.start();

    mHandler = new Handler(mHandlerThread.getLooper());
    mMainHandler = new Handler(Looper.getMainLooper());
  }

  @Override
  public void registerAccount(
      final String displayName, final Callback<Identity> callback) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        final Partial partial = new Partial(displayName);
        String requestBody;
        try {
          JSONObject key = new JSONObject();
          key.put(REGISTER_EXPONENT, partial.getPublicExponentBase64());
          key.put(REGISTER_MODULUS, partial.getModulusBase64());

          JSONObject bodyJson = new JSONObject();
          bodyJson.put(REGISTER_DISPLAY_NAME, partial.getDisplayName());
          bodyJson.put(REGISTER_KEY_OBJECT, key);
          bodyJson.put(REGISTER_PHONE, "");

          requestBody = bodyJson.toString();
        } catch (JSONException exception) {
          callbackFailureOnUi(callback);
          return;
        }

        mHttpRequestFactory.newRequest()
            .setEndPoint(REGISTER_END_POINT)
            .setMethod(HttpRequest.POST)
            .setBody(requestBody)
            .setCallback(new CallbackTransform<String, Identity>(callback) {
                  @Override
                  public Identity transform(String body)
                      throws NumberFormatException {
                    return partial.build(Long.valueOf(body));
                  }
                })
            .makeRequest();
      }
    });
  }

  @Override
  public void login(Identity Identity, Callback<String> callback) {
    // TODO(will): Implement me D:
  }

  private <T> void callbackFailureOnUi(final Callback<T> callback) {
    mMainHandler.post(new Runnable() {
      @Override
      public void run() {
        callback.onApiFailure();
      }
    });
  }
}
