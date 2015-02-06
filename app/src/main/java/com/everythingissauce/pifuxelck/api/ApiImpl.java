package com.everythingissauce.pifuxelck.api;

import com.everythingissauce.pifuxelck.Base64Util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.everythingissauce.pifuxelck.auth.Identity.Partial;
import com.everythingissauce.pifuxelck.auth.Identity;
import com.everythingissauce.pifuxelck.data.InboxEntry;
import com.everythingissauce.pifuxelck.data.Turn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the API that is used to communicate with an abstract backend.
 */
class ApiImpl implements Api {

  private static final String TAG = "ApiImpl";

  private static final String THREAD_NAME = "Api Thread";

  private static final String REGISTER_END_POINT = "/account";
  private static final String REGISTER_DISPLAY_NAME = "display_name";
  private static final String REGISTER_KEY_OBJECT = "public_key";
  private static final String REGISTER_EXPONENT = "exponent";
  private static final String REGISTER_MODULUS = "modulus";
  private static final String REGISTER_PHONE = "hashed_phone";

  private static final String LOGIN_START_END_POINT = "/login/0/";
  private static final String LOGIN_START_CHALLENGE = "challenge";
  private static final String LOGIN_START_CHALLENGE_ID = "id";

  private static final String LOGIN_FINISH_END_POINT = "/login/1/";

  private static final String ACCOUNT_LOOKUP_END_POINT = "/account/lookup/";

  private static final String NEW_GAME_END_POINT = "/newgame";
  private static final String NEW_GAME_PLAYERS = "players";
  private static final String NEW_GAME_LABEL = "label";

  private static final String INBOX_END_POINT = "/inbox";

  private static final String MOVE_END_POINT = "/move/";

  private final HttpRequestFactory mHttpRequestFactory;
  private final Handler mHandler;
  private final Handler mMainHandler;

  private final Object mAuthTokenLock = new Object();
  private String mAuthToken = null;

  public ApiImpl(HttpRequestFactory httpRequestFactory) {
    mHttpRequestFactory = httpRequestFactory;

    HandlerThread handlerThread = new HandlerThread(THREAD_NAME);
    handlerThread.start();

    mHandler = new Handler(handlerThread.getLooper());
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
          Log.e(TAG, "Unable to create new account JSON.", exception);
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
  public void login(final Identity identity, final Callback<String> callback) {
    loginStart(identity, new CallbackWrapper<String>(callback) {
      @Override
      public void onApiSuccess(String jsonResponse) {
        String challenge;
        String challengeId;
        try {
          JSONObject response = new JSONObject(jsonResponse);
          challenge = response.getString(LOGIN_START_CHALLENGE);
          challengeId = response.getString(LOGIN_START_CHALLENGE_ID);
        } catch (JSONException exception) {
          Log.e(TAG, "Unable to parse login start response.", exception);
          callbackFailureOnUi(callback);
          return;
        }

        loginFinish(identity, challengeId, challenge, callback);
      }
    });
  }

  private void loginStart(
      final Identity identity, final Callback<String> callback) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        mHttpRequestFactory.newRequest()
            .setEndPoint(LOGIN_START_END_POINT + identity.getId())
            .setMethod(HttpRequest.GET)
            .setCallback(callback)
            .makeRequest();
      }
    });
  }

  private void loginFinish(
      final Identity identity,
      final String challengeId,
      final String challenge,
      final Callback<String> callback) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        String verification = identity.signBytes(Base64Util.decode(challenge));
        mHttpRequestFactory.newRequest()
            .setEndPoint(LOGIN_FINISH_END_POINT + challengeId)
            .setMethod(HttpRequest.POST)
            .setBody(verification)
            .setCallback(new CallbackWrapper<String>(callback) {
                  @Override
                  public void onApiSuccess(String token) {
                    setAuthToken(token);
                    super.onApiSuccess(token);
                  }
                })
            .makeRequest();
      }
    });
  }

  @Override
  public void lookupUserId(
      final String displayName, final Callback<Long> callback) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        mHttpRequestFactory.newRequest()
            .setEndPoint(ACCOUNT_LOOKUP_END_POINT + displayName)
            .setMethod(HttpRequest.GET)
            .setAuthToken(getAuthToken())
            .setCallback(new CallbackTransform<String, Long>(callback) {
              @Override
              public Long transform(String userId) {
                return Long.parseLong(userId);
              }
            })
            .makeRequest();
      }
    });
  }

  @Override
  public void newGame(
      final String label,
      final List<Long> players,
      final Callback<Void>  callback) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        String requestBody = null;
        try {
          JSONArray playersJson = new JSONArray();
          for (long playerId : players) {
            playersJson.put(playerId);
          }

          JSONObject bodyJson = new JSONObject();
          bodyJson.put(NEW_GAME_PLAYERS, playersJson);
          bodyJson.put(NEW_GAME_LABEL, label);

          requestBody = bodyJson.toString();
        } catch (JSONException exception) {
          Log.e(TAG, "Unable to create new game JSON.", exception);
          callbackFailureOnUi(callback);
          return;
        }

        mHttpRequestFactory.newRequest()
            .setEndPoint(NEW_GAME_END_POINT)
            .setMethod(HttpRequest.POST)
            .setAuthToken(getAuthToken())
            .setBody(requestBody)
            .setCallback(CallbackTransform.<String>voidTransform(callback))
            .makeRequest();
      }
    });
  }

  @Override
  public void inbox(final Callback<List<InboxEntry>> callback) {
    final Callback<String> wrappedCallback =
        new CallbackTransform<String, List<InboxEntry>>(callback) {
      @Override
      public List<InboxEntry> transform(String body) throws Exception {
        List<InboxEntry> entryList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(body);
        for (int i = 0; i < jsonArray.length(); i++) {
          entryList.add( InboxEntry.fromJson(jsonArray.getJSONObject(i)));
        }
        return entryList;
      }
    };

    mHandler.post(new Runnable() {
      @Override
      public void run() {
        mHttpRequestFactory.newRequest()
            .setEndPoint(INBOX_END_POINT)
            .setMethod(HttpRequest.POST)
            .setAuthToken(getAuthToken())
            .setCallback(wrappedCallback)
            .makeRequest();
      }
    });
  }

  @Override
  public void move(
      final long gameId, final Turn turn, final Callback<Void> callback) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        try {
          mHttpRequestFactory.newRequest()
              .setEndPoint(MOVE_END_POINT + gameId)
              .setMethod(HttpRequest.POST)
              .setAuthToken(getAuthToken())
              .setBody(turn.toJson().toString())
              .setCallback(CallbackTransform.<String>voidTransform(callback))
              .makeRequest();
        } catch (JSONException exception) {
          Log.e(TAG, "Unable to create turn JSON.", exception);
          callbackFailureOnUi(callback);
        }
      }
    });
  }

  private void setAuthToken(String token) {
    synchronized (mAuthTokenLock) {
      mAuthToken = token;
    }
  }

  private String getAuthToken() {
    synchronized (mAuthTokenLock) {
      return mAuthToken;
    }
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
