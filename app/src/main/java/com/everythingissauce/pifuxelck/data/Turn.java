package com.everythingissauce.pifuxelck.data;

import android.support.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * A turn in a game. A turn can either be a label or a drawing.
 * <p>
 * This class is immutable.
 */
public class Turn {

  private static final String TYPE_KEY = "type";
  private static final String CONTENTS_KEY = "contents";
  private static final String PLAYER_ID_KEY = "player";

  private static final String TYPE_DRAWING = "drawing";
  private static final String TYPE_LABEL = "label";

  private final boolean mIsLabel;
  private final Drawing mDrawing;
  private final String mLabel;
  @Nullable private final String mPlayerId;

  public Turn(String playerId, Drawing drawing) {
    mIsLabel = false;
    mPlayerId = playerId;
    mDrawing = drawing;
    mLabel = null;
  }

  public Turn(String playerId, String label) {
    mIsLabel = true;
    mPlayerId = playerId;
    mDrawing = null;
    mLabel = label;
  }

  public boolean isDrawingTurn() {
    return !mIsLabel;
  }

  public boolean isLabelTurn() {
    return mIsLabel;
  }

  /**
   * Retrieve the drawing object for this turn. This method should only be
   * called if isDrawingTurn returns true.
   */
  public Drawing getDrawing() {
    return mDrawing;
  }

  /**
   * Retrieve the label object for this turn. This method should only be called
   * if isLabelTurn returns true.
   */
  public String getLabel() {
    return mLabel;
  }

  @Nullable
  public String getPlayerId() {
    return mPlayerId;
  }

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(TYPE_KEY, mIsLabel ? TYPE_LABEL: TYPE_DRAWING);
    if (mIsLabel) {
      json.put(CONTENTS_KEY, mLabel);
    } else {
      json.put(CONTENTS_KEY, mDrawing.toJson());
    }
    if (mPlayerId != null) {
      json.put(PLAYER_ID_KEY, mPlayerId);
    }
    return json;
  }

  public static Turn fromJson(JSONObject json) throws JSONException {
    String type = json.getString(TYPE_KEY);
    String playerId = json.optString(PLAYER_ID_KEY, null);

    if (type.equals(TYPE_LABEL)) {
      return new Turn(playerId, json.getString(CONTENTS_KEY));
    }

    if (type.equals(TYPE_DRAWING)) {
      return new Turn(
        playerId, Drawing.fromJson(json.getJSONObject(CONTENTS_KEY)));
    }

    throw new JSONException(
      "Unexpected value for 'type' field. Got '" + type + "'.");
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Turn")
        .add("isLabel", mIsLabel)
        .add("drawing", mDrawing)
        .add("label", mLabel)
        .add("playerId", mPlayerId)
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Turn)) {
      return false;
    }
    Turn otherTurn = (Turn) other;
    return mIsLabel == otherTurn.mIsLabel
        && Objects.equal(mDrawing, otherTurn.mDrawing)
        && Objects.equal(mLabel, otherTurn.mLabel)
        && Objects.equal(mPlayerId, otherTurn.mPlayerId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mIsLabel, mDrawing, mLabel, mPlayerId);
  }
}

