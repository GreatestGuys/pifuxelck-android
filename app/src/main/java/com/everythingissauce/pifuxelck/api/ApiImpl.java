package com.everythingissauce.pifuxelck.api;

import com.everythingissauce.pifuxelck.Base64Util;

import android.os.HandlerThread;
import android.util.Log;

import com.everythingissauce.pifuxelck.ThreadUtil;
import com.everythingissauce.pifuxelck.api.message.Message;
import com.everythingissauce.pifuxelck.api.message.User;
import com.everythingissauce.pifuxelck.auth.Identity;
import com.everythingissauce.pifuxelck.data.Game;
import com.everythingissauce.pifuxelck.data.InboxEntry;
import com.everythingissauce.pifuxelck.data.Turn;
import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * A concrete implementation of the API that communicates over HTTP/S with
 * the pifuxelck-server.
 */
class ApiImpl implements Api {

  private static final String TAG = "ApiImpl";

  private static final String THREAD_NAME = "Api Thread";

  private static final String REGISTER_END_POINT = "/api/2/account/register";

  private static final String LOGIN_START_END_POINT = "/login/0/";
  private static final String LOGIN_START_CHALLENGE = "challenge";
  private static final String LOGIN_START_CHALLENGE_ID = "id";

  private static final String LOGIN_FINISH_END_POINT = "/login/1/";

  private static final String LOGIN_END_POINT =  "/api/2/account/login";

  private static final String PASSWORD_END_POINT =  "/api/2/account";

  private static final String ACCOUNT_LOOKUP_END_POINT = "/account/lookup/";

  private static final String NEW_GAME_END_POINT = "/newgame";
  private static final String NEW_GAME_PLAYERS = "players";
  private static final String NEW_GAME_LABEL = "label";

  private static final String INBOX_END_POINT = "/inbox";

  private static final String MOVE_END_POINT = "/move/";

  private static final String HISTORY_END_POINT = "/history/";

  private final HttpRequestFactory mHttpRequestFactory;

  private final Object mAuthTokenLock = new Object();
  private String mAuthToken = null;

  public ApiImpl(HttpRequestFactory httpRequestFactory) {
    mHttpRequestFactory = httpRequestFactory;

    HandlerThread handlerThread = new HandlerThread(THREAD_NAME);
    handlerThread.start();
  }

  @Override
  public boolean loggedIn() {
    synchronized (mAuthTokenLock) {
      return mAuthToken != null;
    }
  }

  @Override
  public ListenableFuture<Identity> registerAccount(final String displayName,
                                                    final String password) {
    ListenableFuture<Message> messageFuture =
        makeRequest(new  Callable<ListenableFuture<String>>() {
      @Override
      public ListenableFuture<String> call() throws Exception {
        return mHttpRequestFactory.newRequest()
                .setEndPoint(REGISTER_END_POINT)
                .setMethod(HttpRequest.POST)
                .setAuthToken(getAuthToken())
                .setBody(new Message.Builder()
                    .setUser(new User(displayName, password))
                    .buildJsonString())
                .makeRequest();
      }
    });

    return Futures.transform(messageFuture, new Function<Message, Identity>() {
      @Override
      public Identity apply(Message input) {
        User user = input.getUser();
        return new Identity(user.getId(), displayName, password);
      }
    });
  }

  @Override
  public ListenableFuture<Identity> login(final Identity identity) {
    return identity.hasPassword()
        ? loginWithPassword(identity)
        : loginWithRsa(identity);
  }

  private ListenableFuture<Identity> loginWithPassword(
      final Identity identity) {
    ListenableFuture<Message> messageFuture =
        makeRequest(new  Callable<ListenableFuture<String>>() {
          @Override
          public ListenableFuture<String> call() throws Exception {
            return mHttpRequestFactory.newRequest()
                .setEndPoint(LOGIN_END_POINT)
                .setMethod(HttpRequest.POST)
                .setAuthToken(getAuthToken())
                .setBody(new Message.Builder()
                    .setUser(new User(identity))
                    .buildJsonString())
                .makeRequest();
          }
        });

    return Futures.transform(messageFuture, new Function<Message, Identity>() {
      @Override
      public Identity apply(Message input) {
        setAuthToken(input.getMeta().getAuth());
        User user = input.getUser();
        return new Identity(
            user.getId(),
            identity.getDisplayName(),
            identity.getPassword());
      }
    }, ThreadUtil.THREAD_POOL);
  }

  private ListenableFuture<Identity> loginWithRsa(final Identity identity) {
    return Futures.transform(
        loginStart(identity),
        new AsyncFunction<String, Identity>() {
      @Override
      public ListenableFuture<Identity> apply(String jsonResponse)
          throws Exception {
        JSONObject response = new JSONObject(jsonResponse);
        String challenge = response.getString(LOGIN_START_CHALLENGE);
        String challengeId = response.getString(LOGIN_START_CHALLENGE_ID);

        return loginFinish(identity, challengeId, challenge);
      }
    }, ThreadUtil.THREAD_POOL);
  }

  private ListenableFuture<String> loginStart(final Identity identity) {
      return mHttpRequestFactory.newRequest()
          .setEndPoint(LOGIN_START_END_POINT + identity.getId())
          .setMethod(HttpRequest.GET)
          .makeRequest();
  }

  private ListenableFuture<Identity> loginFinish(
      final Identity identity,
      final String challengeId,
      final String challenge) {
    String verification = identity.signBytes(Base64Util.decode(challenge));
    ListenableFuture<String> tokenFuture = mHttpRequestFactory
        .newRequest()
        .setEndPoint(LOGIN_FINISH_END_POINT + challengeId)
        .setMethod(HttpRequest.POST)
        .setBody(verification)
        .makeRequest();

    Futures.addCallback(tokenFuture, new FutureCallback<String>() {
      @Override
      public void onSuccess(String token) {
        setAuthToken(token);
      }

      @Override
      public void onFailure(Throwable t) {
      }
    }, ThreadUtil.THREAD_POOL);

    return Futures.transform(tokenFuture, new Function<String, Identity>() {
      @Override
      public Identity apply(String input) {
        return identity;
      }
    }, ThreadUtil.THREAD_POOL);
  }

  @Override
  public ListenableFuture<Identity> changePassword(
      final Identity identity, final String newPassword) {
    ListenableFuture<Message> messageFuture = makeRequest(
        new Callable<ListenableFuture<String>>() {
      @Override
      public ListenableFuture<String> call() throws Exception {
        return mHttpRequestFactory.newRequest()
            .setEndPoint(PASSWORD_END_POINT)
            .setMethod(HttpRequest.PUT)
            .setAuthToken(getAuthToken())
            .setBody(new Message.Builder()
                .setUser(new User(identity.getDisplayName(), newPassword))
                .buildJsonString())
            .makeRequest();
      }
    });

    return Futures.transform(messageFuture, new Function<Message, Identity>() {
      @Override
      public Identity apply(Message input) {
        return new Identity(
            identity.getId(), identity.getDisplayName(), newPassword);
      }
    });
  }

  @Override
  public ListenableFuture<Long> lookupUserId(final String displayName) {
      return Futures.transform(
          mHttpRequestFactory.newRequest()
              .setEndPoint(ACCOUNT_LOOKUP_END_POINT + displayName)
              .setMethod(HttpRequest.GET)
              .setAuthToken(getAuthToken())
              .makeRequest(),
          new Function<String, Long>() {
            @Override
            public Long apply(String input) {
              return Long.parseLong(input);
            }
          }, ThreadUtil.THREAD_POOL);
  }

  @Override
  public ListenableFuture<Void> newGame(
      final String label,
      final List<Long> players) {
    return Futures.transform(
        Futures.<Void>immediateFuture(null),
        new AsyncFunction<Void, Void>() {
          @Override
          public ListenableFuture<Void> apply(Void input) throws JSONException {
            JSONArray playersJson = new JSONArray();
            for (long playerId : players) {
              playersJson.put(playerId);
            }

            JSONObject bodyJson = new JSONObject();
            bodyJson.put(NEW_GAME_PLAYERS, playersJson);
            bodyJson.put(NEW_GAME_LABEL, label);

            String requestBody = bodyJson.toString();

            return ThreadUtil.voidFuture(
                mHttpRequestFactory.newRequest()
                    .setEndPoint(NEW_GAME_END_POINT)
                    .setMethod(HttpRequest.POST)
                    .setAuthToken(getAuthToken())
                    .setBody(requestBody)
                    .makeRequest());
      }
    }, ThreadUtil.THREAD_POOL);
  }

  @Override
  public ListenableFuture<List<InboxEntry>> inbox() {
    ListenableFuture<String> bodyFuture = mHttpRequestFactory.newRequest()
            .setEndPoint(INBOX_END_POINT)
            .setMethod(HttpRequest.POST)
            .setAuthToken(getAuthToken())
            .makeRequest();

    return Futures.transform(
        bodyFuture,
        new AsyncFunction<String, List<InboxEntry>>() {
          @Override
          public ListenableFuture<List<InboxEntry>> apply(String body)
              throws Exception {
            List<InboxEntry> entryList = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(body);
            for (int i = 0; i < jsonArray.length(); i++) {
              entryList.add( InboxEntry.fromJson(jsonArray.getJSONObject(i)));
            }
            return Futures.immediateFuture(entryList);
          }
        }, ThreadUtil.THREAD_POOL);
  }

  @Override
  public ListenableFuture<Void> move(final long gameId, final Turn turn) {
    ListenableFuture<String> bodyFuture = Futures.transform(
        Futures.<Void>immediateFuture(null),
        new AsyncFunction<Void, String>() {
          @Override
          public ListenableFuture<String> apply(Void input) throws Exception {
            return mHttpRequestFactory.newRequest()
                .setEndPoint(MOVE_END_POINT + gameId)
                .setMethod(HttpRequest.POST)
                .setAuthToken(getAuthToken())
                .setBody(turn.toJson().toString())
                .makeRequest();
          }
        });
    return ThreadUtil.voidFuture(bodyFuture);
  }

  @Override
  public ListenableFuture<List<Game>> history(final long startTimeSeconds) {
    AsyncFunction<String, List<Game>> bodyToGames =
        new AsyncFunction<String, List<Game>>() {
      @Override
      public ListenableFuture<List<Game>> apply(String body) throws Exception {
        List<Game> gameList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(body);
        for (int i = 0; i < jsonArray.length(); i++) {
          gameList.add(Game.fromJson(jsonArray.getJSONObject(i)));
        }
        return Futures.immediateFuture(gameList);
      }
    };

    ListenableFuture<String> bodyFuture =  mHttpRequestFactory
        .newRequest()
        .setEndPoint(HISTORY_END_POINT + startTimeSeconds)
        .setMethod(HttpRequest.GET)
        .setAuthToken(getAuthToken())
        .makeRequest();

    return Futures.transform(bodyFuture, bodyToGames, ThreadUtil.THREAD_POOL);
  }

  private ListenableFuture<Message> makeRequest(
      final Callable<ListenableFuture<String>> requestCallable) {
    return Futures.transform(
        Futures.<Void>immediateFuture(null),
        new AsyncFunction<Void, Message>() {
          @Override
          public ListenableFuture<Message> apply(Void input) throws Exception {
            ListenableFuture<String> bodyFuture = requestCallable.call();
            return Futures.transform(
                bodyFuture,
                new AsyncFunction<String, Message>() {
                  @Override
                  public ListenableFuture<Message> apply(String input)
                      throws JSONException {
                    return Futures.immediateFuture(
                        Message.fromJson(new  JSONObject(input)));
                  }
                });
          }
        }
    );
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
}
