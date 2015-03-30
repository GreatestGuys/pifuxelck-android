package com.everythingissauce.pifuxelck.api.message;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {

  private static final String USER_KEY = "user";
  private static final String META_KEY = "meta";

  public static class Builder {

    private Meta mMeta;
    private User mUser;

    public Builder setUser(User user) {
      mUser = user;
      return this;
    }

    public Builder setMeta(Meta meta) {
      mMeta = meta;
      return this;
    }

    public Message build() {
      return new Message(mMeta, mUser);
    }

    public String buildJsonString() throws JSONException {
      return new Message(mMeta, mUser).toJson().toString();
    }
  }

  private final Meta mMeta;
  private final User mUser;

  private Message(
      Meta meta,
      User user) {
    mMeta = meta;
    mUser = user;
  }

  public Meta getMeta() {
    return mMeta;
  }

  public User getUser() {
    return mUser;
  }

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    if (mMeta != null) json.put(META_KEY, mMeta.toJson());
    if (mUser != null) json.put(USER_KEY, mUser.toJson());
    return json;
  }

  public static Message fromJson(JSONObject json) throws JSONException {
    return new Message(
        Meta.fromJson(json.optJSONObject(META_KEY)),
        User.fromJson(json.optJSONObject(USER_KEY)));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Message")
        .add("meta", mMeta)
        .add("user", mUser)
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Message)) {
      return false;
    }
    Message otherMessage = (Message) other;
    return Objects.equal(mMeta, otherMessage.mMeta)
        && Objects.equal(mUser, otherMessage.mUser);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mMeta, mUser);
  }
}
