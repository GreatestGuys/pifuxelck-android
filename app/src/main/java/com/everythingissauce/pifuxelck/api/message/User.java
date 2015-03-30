package com.everythingissauce.pifuxelck.api.message;

import com.everythingissauce.pifuxelck.auth.Identity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

  private static final String ID_KEY = "id";
  private static final String DISPLAY_NAME_KEY = "display_name";
  private static final String PASSWORD_KEY = "password";

  private final long mId;
  private final String mDisplayName;
  private final String mPassword;

  public User(String displayName, String password) {
    mId = -1;
    mDisplayName = displayName;
    mPassword = password;
  }

  public User(long id, String displayName, String password) {
    mId = id;
    mDisplayName = displayName;
    mPassword = password;
  }

  public User(Identity identity) {
    mId = identity.getId();
    mDisplayName = identity.getDisplayName();
    mPassword = identity.getPassword();
  }

  public long getId() {
    return mId;
  }

  public String getDisplayName() {
    return mDisplayName;
  }

  public String getPassword() {
    return mPassword;
  }

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    if (mId != -1) json.put(ID_KEY, mId);
    json.put(DISPLAY_NAME_KEY, mDisplayName);
    json.put(PASSWORD_KEY, mPassword);
    return json;
  }

  public static User fromJson(JSONObject json) throws JSONException {
    if (json == null) return null;
    return new User(
        json.optLong(ID_KEY),
        json.getString(DISPLAY_NAME_KEY),
        json.optString(PASSWORD_KEY));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("User")
        .add("id", mId)
        .add("displayName", mDisplayName)
        .add("password", mPassword)
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof User)) {
      return false;
    }
    User otherUser = (User) other;
    return Objects.equal(mId, otherUser.mId)
        && Objects.equal(mDisplayName, otherUser.mDisplayName)
        && Objects.equal(mPassword, otherUser.mPassword);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mId, mDisplayName, mPassword);
  }
}
