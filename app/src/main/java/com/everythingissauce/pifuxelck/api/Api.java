package com.everythingissauce.pifuxelck.api;

import com.everythingissauce.pifuxelck.auth.Identity;
import com.everythingissauce.pifuxelck.data.InboxEntry;
import com.everythingissauce.pifuxelck.data.Turn;

import java.util.List;

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

  /**
   * Creates a new game.
   * @param label The initial label.
   * @param players A list IDs of players that are to be included in the game.
   * @param callback A callback that can be used to determine the success or
   *                 failure of the call.
   */
  void newGame(String label, List<Long> players, Callback<Void> callback);

  /**
   * Queries the current logged in user's inbox.
   * @param callback A callback that will return the list of all current
   *                 entries in a user's inbox.
   */
  void inbox(Callback<List<InboxEntry>> callback);

  /**
   * Submit a turn for an active game.
   * @param gameId The ID of the game.
   * @param turn The turn that is to be submitted. It is not necessary for
   *             the player ID of the turn to be filled in as it will be
   *             inferred by the logged in state of the user.
   * @param callback A callback that can be used to determine the success or
   *                 failure of the call.
   */
  void move(long gameId, Turn turn, Callback<Void> callback);
}
