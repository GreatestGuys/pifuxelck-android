package com.everythingissauce.pifuxelck.api;

import com.everythingissauce.pifuxelck.auth.Identity;

/**
 * Represents the API that is used to communicate with an abstract backend.
 */
public interface Api {

  /**
   * An interface for being notified of the result of API calls. All of the
   * callback methods are guaranteed to be called on the UI thread.
   */
  public interface Callback<T> {

    /**
     * Called when an API call succeeds.
     * @param result The result of the API call.
     */
    void onApiSuccess(T result);

    /**
     * Called when an API call fails.
     */
    void onApiFailure();
  }

  /**
   * Registers a partial identity with the server.
   * @param displayName The display name to register.
   * @param callback A callback that will return the user ID of the registered
   *                 user on success.
   */
  void registerAccount(String displayName, Callback<Identity> callback);

  /**
   * Attempts to login and obtain an authentication token.
   * @param identity The identity of the current user.
   * @param callback A callback that will return the authentication token on
   *                 success.
   */
  void login(Identity identity, Callback<String> callback);

  /**
   * Attempt to resolve the user ID of a given display name.
   * @param displayName The display name of the user to lookup.
   * @param callback A callback that will return the user ID of the given
   *                 display name.
   */
  void lookupUserId(String displayName, Callback<Long> callback);
}
