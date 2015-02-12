package com.everythingissauce.pifuxelck.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * A bound Service that instantiates the authenticator when started.
 */
public class AuthenticatorService extends Service {
  private Authenticator mAuthenticator;

  @Override
  public void onCreate() {
    mAuthenticator = new Authenticator(this);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mAuthenticator.getIBinder();
  }
}
