package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.api.Api;
import com.everythingissauce.pifuxelck.api.ApiProvider;
import com.everythingissauce.pifuxelck.auth.Identity;
import com.everythingissauce.pifuxelck.storage.IdentityProvider;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WelcomeActivity extends Activity implements View.OnClickListener {

  private static final String TAG = "WelcomeActivity";

  private Button mLoginButton;
  private EditText mDisplayNameEditText;
  private View mProgressOverlay;

  private IdentityProvider mIdentityProvider;

  private final Api mApi = ApiProvider.getApi();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_welcome);

    mDisplayNameEditText = (EditText) findViewById(R.id.display_name_edit_text);
    mProgressOverlay = findViewById(R.id.progress_overlay);

    mLoginButton = (Button) findViewById(R.id.login_button);
    mLoginButton.setOnClickListener(this);

    mIdentityProvider = IdentityProvider.getInstance(this);
    if (mIdentityProvider.hasIdentity()) {
      openInbox();
    }
  }

  @Override
  public void onClick(View view) {
    String displayName = mDisplayNameEditText.getText().toString();

    if (displayName.length() == 0) {
      return;
    }

    showLoading();

    final Toast errorToast = Toast.makeText(
            WelcomeActivity.this, R.string.error_register, Toast.LENGTH_LONG);

    mApi.registerAccount(displayName, new Api.Callback<Identity>() {
      @Override
      public void onApiSuccess(Identity identity) {
        mIdentityProvider.setIdentity(identity);

        // Now that we have successfully created an account, attempt to login
        // with it!
        mApi.login(identity, new Api.Callback<String>() {
          @Override
          public void onApiSuccess(String token) {
            openInbox();
          }

          @Override
          public void onApiFailure() {
            errorToast.show();
            hideLoading();
          }
        });
      }

      @Override
      public void onApiFailure() {
        hideLoading();
        errorToast.show();
      }
    });
  }

  private void showLoading() {
    mProgressOverlay.setVisibility(View.VISIBLE);
  }

  private void hideLoading() {
    mProgressOverlay.setVisibility(View.INVISIBLE);
  }

  private void openInbox() {
    Intent intent = new Intent();
    intent.setClass(this.getApplicationContext(), InboxActivity.class);
    startActivity(intent);
  }
}
