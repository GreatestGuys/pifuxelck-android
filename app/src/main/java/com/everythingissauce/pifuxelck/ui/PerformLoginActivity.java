package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.ThreadUtil;
import com.everythingissauce.pifuxelck.api.Api;
import com.everythingissauce.pifuxelck.api.ApiProvider;
import com.everythingissauce.pifuxelck.auth.Identity;
import com.everythingissauce.pifuxelck.storage.IdentityProvider;
import com.everythingissauce.pifuxelck.R;

import com.google.common.util.concurrent.FutureCallback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

public class PerformLoginActivity extends Activity {

  public static final String EXTRA_IDENTITY = "identity";
  public static final String EXTRA_FAIL_TO_INBOX = "fail_to_inbox";

  private static final String TAG = "PerformLogin";

  private static final int REQUEST_CHANGE_PASSWORD = 0;

  private Identity mIdentity;
  private IdentityProvider mIdentityProvider;

  private final Api mApi = ApiProvider.getApi();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_perform_login);

    try {
      mIdentity = Identity.fromJson(
          new JSONObject(getIntent().getExtras().getString(EXTRA_IDENTITY)));
    } catch (Exception exception) {
      Log.e(TAG, "Unable to retrieve identity JSON.", exception);
      finish();
    }

    mIdentityProvider = IdentityProvider.getInstance(this);

    final boolean failToInbox =
        getIntent().getBooleanExtra(EXTRA_FAIL_TO_INBOX, false);

    ThreadUtil.callbackOnUi(
        mApi.login(mIdentity),
        new FutureCallback<Identity>() {
          @Override
          public void onSuccess(Identity identity) {
            mIdentityProvider.setIdentity(identity);
            openInbox();
          }

          @Override
          public void onFailure(Throwable t) {
            if (failToInbox) {
              openInbox();
            } else {
              finish();
            }
            Toast.makeText(
                PerformLoginActivity.this,
                R.string.error_login,
                Toast.LENGTH_LONG).show();
          }
        });
  }

  @Override
  public void onActivityResult(int request, int result, Intent data) {
    if (request == REQUEST_CHANGE_PASSWORD) {
      startActivity(new Intent(this, InboxActivity.class));
      finish();
    }
  }

  private void openInbox() {
    Identity identity = mIdentityProvider.getIdentity();
    if (identity != null && !identity.hasPassword()) {
      startActivityForResult(
          new Intent(this, PasswordChangeActivity.class),
          REQUEST_CHANGE_PASSWORD);
      return;
    }

    startActivity(new Intent(this, InboxActivity.class));
    finish();
  }
}
