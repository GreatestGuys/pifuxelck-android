package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.ThreadUtil;
import com.everythingissauce.pifuxelck.api.Api;
import com.everythingissauce.pifuxelck.api.ApiProvider;
import com.everythingissauce.pifuxelck.auth.Identity;
import com.everythingissauce.pifuxelck.storage.IdentityProvider;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

public class PasswordChangeActivity extends Activity implements View.OnClickListener {

  private static final String TAG = "PasswordChangeActivity";

  private Button mUpdateButton;
  private EditText mPasswordText;
  private EditText mPasswordConfirmText;

  private IdentityProvider mIdentityProvider;

  private final Api mApi = ApiProvider.getApi();

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_password_change);

    mPasswordText = (EditText) findViewById(R.id.password_text);
    mPasswordConfirmText = (EditText) findViewById(R.id.password_confirm_text);
    mUpdateButton = (Button) findViewById(R.id.update_button);
    mUpdateButton.setOnClickListener(this);

    // If there is no identity, then we cannot make the call to change the
    // password.
    mIdentityProvider = new IdentityProvider(this);
    if (!mIdentityProvider.hasIdentity()) {
      finish();
    }
  }

  @Override
  public void onBackPressed() {
    setResult(RESULT_CANCELED);
    super.onBackPressed();
  }

  @Override
  public void onClick(View view) {
    String newPassword = mPasswordText.getText().toString();
    String newPasswordConfirm = mPasswordText.getText().toString();

    if (!newPassword.equals(newPasswordConfirm)) {
      showToast(R.string.error_password_match);
      return;
    }

    ThreadUtil.callbackOnUi(
        mApi.changePassword(mIdentityProvider.getIdentity(), newPassword),
        new FutureCallback<Identity>() {
          @Override
          public void onSuccess(Identity result) {
            mIdentityProvider.setIdentity(result);
            showToast(R.string.password_change_success);
            setResult(RESULT_OK);
            finish();
          }

          @Override
          public void onFailure(Throwable t) {
            Log.e(TAG, "password change failed.", t);
            showToast(R.string.password_change_failure);
          }
        });
  }

  private void showToast(int message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }
}
