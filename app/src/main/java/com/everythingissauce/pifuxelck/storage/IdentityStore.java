package com.everythingissauce.pifuxelck.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.everythingissauce.pifuxelck.Base64Util;
import com.everythingissauce.pifuxelck.auth.Identity;

class IdentityStore {

  private static final String IDENTITY_SHARED_PREFERENCES = "identity";

  private static final String FIELD_ID = "user_id";
  private static final String FIELD_KEY_MODULUS = "modulus";
  private static final String FIELD_KEY_PRIVATE = "private_exponent";
  private static final String FIELD_KEY_PUBLIC = "public_exponent";
  private static final String FIELD_NAME = "name";

  private final SharedPreferences mSharedPrefs;

  public IdentityStore(Context context) {
    mSharedPrefs = context.getSharedPreferences(
        IDENTITY_SHARED_PREFERENCES, Context.MODE_PRIVATE);
  }

  /**
   * Read the user's identity from storage. If the user does not have an
   * identity saved then {@code null} will be returned.
   */
  @Nullable
  public Identity getIdentity() {
    long userId = mSharedPrefs.getLong(FIELD_ID, -1);
    if (userId == -1) return null;

    String displayName = mSharedPrefs.getString(FIELD_NAME, null);
    if (displayName == null) return null;

    String modulus = mSharedPrefs.getString(FIELD_KEY_MODULUS, null);
    if (modulus == null) return null;

    String privateExponent = mSharedPrefs.getString(FIELD_KEY_PRIVATE, null);
    if (privateExponent == null) return null;

    String publicExponent = mSharedPrefs.getString(FIELD_KEY_PUBLIC, null);
    if (publicExponent == null) return null;

    return new Identity(
        userId, displayName, modulus, publicExponent, privateExponent);
  }

  public void setIdentity(Identity identity) {
    mSharedPrefs.edit()
        .putLong(FIELD_ID, identity.getId())
        .putString(FIELD_NAME, identity.getDisplayName())
        .putString(FIELD_KEY_PUBLIC, identity.getPublicExponentBase64())
        .putString(FIELD_KEY_PRIVATE, identity.getPrivateExponentBase64())
        .putString(FIELD_KEY_MODULUS, identity.getModulusBase64())
        .apply();
  }

  @Nullable
  private byte[] getBytes(String field) {
    String encodedBytes = mSharedPrefs.getString(field, null);
    if (encodedBytes == null) return null;
    return Base64Util.decode(encodedBytes);
  }
}