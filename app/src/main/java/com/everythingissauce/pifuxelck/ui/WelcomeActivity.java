package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.ThreadUtil;
import com.everythingissauce.pifuxelck.api.Api;
import com.everythingissauce.pifuxelck.api.ApiProvider;
import com.everythingissauce.pifuxelck.auth.Identity;
import com.everythingissauce.pifuxelck.storage.IdentityProvider;
import com.everythingissauce.pifuxelck.storage.IdentityStore;
import com.google.common.util.concurrent.FutureCallback;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class WelcomeActivity extends Activity implements View.OnClickListener {

  private static final String TAG = "WelcomeActivity";

  private static final int RESULT_IMPORT = 0;

  private static final String ACCOUNT = "pifuxelck";
  private static final String ACCOUNT_TYPE = "pifuxelck.everythingissauce.com";
  private static final String AUTHORITY =
      "com.everythingissauce.pifuxelck.content";

  private static final long SYNC_RATE_SECS = TimeUnit.MINUTES.toSeconds(15);

  private Button mLoginButton;
  private Button mJoinButton;
  private Button mImportButton;

  private IdentityProvider mIdentityProvider;

  private final Api mApi = ApiProvider.getApi();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_welcome);

    mLoginButton = (Button) findViewById(R.id.login_button);
    mLoginButton.setOnClickListener(this);

    mJoinButton = (Button) findViewById(R.id.join_button);
    mJoinButton.setOnClickListener(this);

    mImportButton = (Button) findViewById(R.id.import_button);
    mImportButton.setOnClickListener(this);

    mIdentityProvider = IdentityProvider.getInstance(this);
    if (mIdentityProvider.hasIdentity()) {
      login(mIdentityProvider.getIdentity(), true);
    }

    // Set up a dummy account. This is required to placate the Android
    // syncing framework which makes several restrictive assumptions about
    // the structure of an application that will be syncing data with a
    // server. Mainly, that there will be an account and content provider
    // associated with the syncing.
    Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
    AccountManager accountManager =
        (AccountManager) getSystemService(ACCOUNT_SERVICE);
    accountManager.addAccountExplicitly(newAccount, null, null);

    // Schedule periodic syncing with the backend. This will pull in new
    // inbox entries and new recently finished games.
    getContentResolver();
    ContentResolver.setSyncAutomatically(newAccount, AUTHORITY, true);
    ContentResolver.addPeriodicSync(
        newAccount,
        AUTHORITY,
        Bundle.EMPTY,
        SYNC_RATE_SECS);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.join_button:
        startActivity(new Intent(this, RegisterActivity.class));
        return;
      case R.id.login_button:
        startActivity(new Intent(this, LoginActivity.class));
        return;
      case R.id.import_button:
        onImportClick();
        return;
    }
  }

  private void onImportClick() {
    startActivityForResult(
        Intent.createChooser(
            new Intent(Intent.ACTION_GET_CONTENT).setType("*/*"),
            getString(R.string.welcome_import_account_chooser_title)),
        RESULT_IMPORT);
  }

  @Override
  public void onActivityResult(int request, int result, Intent data) {
    if (result != RESULT_OK || data == null) {
      return;
    }

    Uri dataUri = data.getData();
    if (dataUri == null) {
      return;
    }

    try {
      InputStream inputStream = getContentResolver().openInputStream(dataUri);
      String identityString = IOUtils.toString(inputStream, "UTF-8");
      Identity identity = Identity.fromJson(new JSONObject(identityString));
      login(identity, false);
    } catch (Exception exception) {
      Log.e(TAG, "Unable to import identity.", exception);
      Toast.makeText(
          WelcomeActivity.this, R.string.error_import, Toast.LENGTH_LONG)
          .show();
    }
  }

  private void login(Identity identity, boolean failToInbox) {
    try {
      Intent intent = new Intent(this, PerformLoginActivity.class);
      intent.putExtra(PerformLoginActivity.EXTRA_FAIL_TO_INBOX, failToInbox);
      intent.putExtra(
          PerformLoginActivity.EXTRA_IDENTITY,
          identity.toJson().toString());
      startActivity(intent);
    } catch (JSONException exception) {
      Log.e(TAG, "Unable to marshal identity to JSON.", exception);
    }
  }
}
