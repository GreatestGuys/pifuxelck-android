package com.everythingissauce.pifuxelck.data;

import android.support.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An entry in a user's inbox. This class is immutable.
 */
public class InboxEntry {

  private static final String GAME_ID_KEY = "game_id";
  private static final String TURN_KEY = "turn";

  private final long mGameId;
  private final Turn mPreviousTurn;
  @Nullable private final Turn mCurrentTurn;

  public InboxEntry(long gameId, Turn previousTurn) {
    this(gameId, previousTurn, null);
  }

  public InboxEntry(long gameId, Turn previousTurn, Turn currentTurn) {
    mGameId = gameId;
    mPreviousTurn = previousTurn;
    mCurrentTurn = currentTurn;
  }

  public long getGameId() {
    return mGameId;
  }

  public Turn getPreviousTurn() {
    return mPreviousTurn;
  }

  public Turn getCurrentTurn() {
    return mCurrentTurn;
  }

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(GAME_ID_KEY, mGameId);
    json.put(TURN_KEY, mPreviousTurn);
    return json;
  }

  public static InboxEntry fromJson(JSONObject json) throws JSONException {
    return new InboxEntry(
        json.getLong(GAME_ID_KEY),
        Turn.fromJson(json.getJSONObject(TURN_KEY)));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("InboxEntry")
        .add("gameId", mGameId)
        .add("previousTurn", mPreviousTurn)
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof InboxEntry)) {
      return false;
    }
    InboxEntry otherEntry = (InboxEntry) other;
    return mGameId == otherEntry.mGameId
        && mPreviousTurn.equals(otherEntry.mPreviousTurn);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mGameId, mPreviousTurn);
  }
}

