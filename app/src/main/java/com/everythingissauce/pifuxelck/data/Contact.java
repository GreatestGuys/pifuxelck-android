package com.everythingissauce.pifuxelck.data;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class Contact {

  private final String mDisplayName;
  private final long mUserId;

  public Contact(long userId, String displayName) {
    mDisplayName = displayName;
    mUserId = userId;
  }

  public String getDisplayName() {
    return mDisplayName;
  }

  public long getUserId() {
    return mUserId;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Contact")
        .add("id", mUserId)
        .add("displayName", mDisplayName)
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Contact)) {
      return false;
    }
    Contact otherContact = (Contact) other;
    return mUserId == otherContact.mUserId
        && mDisplayName.equals(otherContact.mDisplayName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mUserId, mDisplayName);
  }
}
