package com.everythingissauce.pifuxelck.storage;

import android.content.Context;
import android.support.annotation.Nullable;

import com.everythingissauce.pifuxelck.Base64Util;
import com.everythingissauce.pifuxelck.auth.Identity;

/**
 * This class is a singleton that provides access to the user's identity.
 */
public class IdentityProvider {

  private static final Object sLock = new Object();
  private static IdentityProvider sInstance = null;

  public static IdentityProvider getInstance(Context context) {
    synchronized (sLock) {
      if (sInstance == null) {
        sInstance = new IdentityProvider(context.getApplicationContext());
      }
    }
    return sInstance;
  }

  private final Object mLock;
  private final IdentityStore mIdentityStore;
  @Nullable private Identity mIdentity;

  public IdentityProvider(Context context) {
    mLock = new Object();
    mIdentityStore = new IdentityStore(context);
    mIdentity = mIdentityStore.getIdentity();
  }

  public boolean hasIdentity() {
    synchronized (mLock) {
      return mIdentity != null;
    }
  }

  public Identity getIdentity() {
    synchronized (mLock) {
      return mIdentity;
    }
  }

  public void setIdentity(Identity identity) {
    synchronized (mLock) {
      mIdentity = identity;
      mIdentityStore.setIdentity(identity);
    }
  }
}
