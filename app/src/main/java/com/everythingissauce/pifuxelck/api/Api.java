package com.everythingissauce.pifuxelck.api;

import com.everythingissauce.pifuxelck.auth.Identity;
import com.everythingissauce.pifuxelck.data.Game;
import com.everythingissauce.pifuxelck.data.InboxEntry;
import com.everythingissauce.pifuxelck.data.Turn;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

/**
 * Represents the API that is used to communicate with an abstract backend.
 */
public interface Api {

  /**
   * Synchronously determine if the API has an authentication token. Having
   * an authentication token allows the API instance to make requests that
   * require the user to be logged in.
   * @return A boolean indicating if the API is currently logged in.
   */
  boolean loggedIn();

  /**
   * Registers a partial identity with the server.
   * @param displayName the display name to register
   * @return the user ID of the registered user
   */
  ListenableFuture<Identity> registerAccount(String displayName);

  /**
   * Attempts to login and obtain an authentication token.
   * @param identity the identity of the current user
   * @return the authentication token
   */
  ListenableFuture<String> login(Identity identity);

  /**
   * Attempt to resolve the user ID of a given display name.
   * @param displayName the display name of the user to lookup
   * @return the user ID of the given display name
   */
  ListenableFuture<Long> lookupUserId(String displayName);

  /**
   * Creates a new game.
   * @param label the initial label
   * @param players a list IDs of players that are to be included in the game
   * @return a future that resolves if the game was created
   */
  ListenableFuture<Void> newGame(String label, List<Long> players);

  /**
   * Queries the current logged in user's inbox.
   * @return the list of all current entries in a user's inbox
   */
  ListenableFuture<List<InboxEntry>> inbox();

  /**
   * Submit a turn for an active game.
   * @param gameId the ID of the game
   * @param turn the turn that is to be submitted. It is not necessary for
   *             the player ID of the turn to be filled in as it will be
   *             inferred by the logged in state of the user.
   * @return a future that resolves if submitting the move succeeded
   */
  ListenableFuture<Void> move(long gameId, Turn turn);

  /**
   * Retrieve a list of games that occurred after the given timestamp.
   * @param startTimeSeconds The unix timestamp to begin querying games at.
   * @return the list of completed games
   */
  ListenableFuture<List<Game>> history(long startTimeSeconds);
}
