package com.everythingissauce.pifuxelck.api.message;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.json.JSONException;
import org.json.JSONObject;

public class Meta {

  private static final String AUTH_KEY = "auth";

  private final String mAuth;

  public Meta() {
    mAuth = null;
  }

  public Meta(String auth) {
    mAuth = auth;
  }

  public String getAuth() {
    return mAuth;
  }

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    if (mAuth != null) json.put(AUTH_KEY, mAuth);
    return json;
  }

  public static Meta fromJson(JSONObject json) throws JSONException {
    if (json == null) return null;
    return new Meta(json.optString(AUTH_KEY));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Meta")
        .add("auth", mAuth)
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Meta)) {
      return false;
    }
    Meta otherMeta = (Meta) other;
    return Objects.equal(mAuth, otherMeta.mAuth);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mAuth);
  }
}
