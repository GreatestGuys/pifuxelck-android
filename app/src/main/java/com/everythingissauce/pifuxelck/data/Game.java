package com.everythingissauce.pifuxelck.data;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A completed game. This class is immutable.
 */
public class Game implements Iterable<Turn> {

  /**
   * A builder for games. This class is not thread safe.
   */
  public static class Builder {
    private final List<Turn> mTurns;
    private final long mCompletedAt;
    private final long mId;

    public Builder(long id, long completedAt) {
      mId = id;
      mCompletedAt = completedAt;
      mTurns = new ArrayList<Turn>();
    }

    public Builder addTurn(Turn turn) {
      mTurns.add(turn);
      return this;
    }

    public Game build() {
      return new Game(mId, mCompletedAt, mTurns);
    }
  }

  private static final String ID_KEY = "game_id";
  private static final String TURNS_KEY = "turns";
  private static final String COMPLETED_AT_KEY = "completed_at";

  private final long mId;
  private final Turn[] mTurns;
  private final long mCompletedAt;

  private Game(long id, long completedAt, List<Turn> turns) {
    mId = id;
    mCompletedAt = completedAt;
    mTurns = new Turn[turns.size()];
    turns.toArray(mTurns);
  }

  public long getId() {
    return mId;
  }

  public long getTimeCompleted() {
    return mCompletedAt;
  }

  public int getNumberOfTurns() {
    return mTurns.length;
  }

  @Override
  public Iterator<Turn> iterator() {
    return Arrays.asList(mTurns).iterator();
  }

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(ID_KEY, mId);
    json.put(COMPLETED_AT_KEY, mCompletedAt);
    JSONArray turnArray = new JSONArray();
    for (Turn turn : mTurns) {
      turnArray.put(turn.toJson());
    }
    json.put(TURNS_KEY, turnArray);
    return json;
  }

  public static Game fromJson(JSONObject json) throws JSONException {
    long id = json.getLong(ID_KEY);
    long completedAt = json.getLong(COMPLETED_AT_KEY);
    Builder builder = new Builder(id, completedAt);
    JSONArray turnArray = json.getJSONArray(TURNS_KEY);
    for (int i = 0; i < turnArray.length(); i++) {
      builder.addTurn(Turn.fromJson(turnArray.getJSONObject(i)));
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Game")
        .add("id", mId)
        .add("completedAt", mCompletedAt)
        .add("turns", mTurns.toString())
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Game)) {
      return false;
    }
    Game otherGame = (Game) other;
    return mId == otherGame.mId
        && mCompletedAt == otherGame.mCompletedAt
        && Arrays.equals(mTurns, otherGame.mTurns);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mCompletedAt, mTurns);
  }
}
