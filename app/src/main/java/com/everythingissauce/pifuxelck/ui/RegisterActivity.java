package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.api.Api;
import com.everythingissauce.pifuxelck.api.ApiProvider;
import com.everythingissauce.pifuxelck.auth.Identity;
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

public class RegisterActivity extends Activity implements View.OnClickListener {

  private static final String TAG = "Register";

  private Button mJoinButton;
  private EditText mDisplayNameText;
  private EditText mPasswordText;
  private EditText mPasswordConfirmText;

  private final Api mApi = ApiProvider.getApi();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    mDisplayNameText = (EditText) findViewById(R.id.display_name_text);
    mPasswordText = (EditText) findViewById(R.id.password_text);
    mPasswordConfirmText = (EditText) findViewById(R.id.password_confirm_text);
    mJoinButton = (Button) findViewById(R.id.join_button);
    mJoinButton.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    String displayName = mDisplayNameText.getText().toString();
    String password = mPasswordText.getText().toString();
    String passwordConfirm = mPasswordConfirmText.getText().toString();

    if (!password.equals(passwordConfirm)) {
      Toast.makeText(
          this, R.string.error_password_match, Toast.LENGTH_LONG).show();
      return;
    }

    Futures.addCallback(
        mApi.registerAccount(displayName, password),
        new FutureCallback<Identity>() {
          @Override
          public void onSuccess(Identity result) {
            login(result);
          }

          @Override
          public void onFailure(Throwable t) {
            Toast.makeText(
                RegisterActivity.this,
                R.string.error_register,
                Toast.LENGTH_LONG).show();
          }
        });
  }

  private void login(Identity identity) {
    try {
      Intent intent = new Intent(this, PerformLoginActivity.class);
      intent.putExtra(
          PerformLoginActivity.EXTRA_IDENTITY,
          identity.toJson().toString());
      startActivity(intent);
    } catch (JSONException exception) {
      Log.e(TAG, "Unable to marshall identity to JSON.", exception);
    }
  }
}
