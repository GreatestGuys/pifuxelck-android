package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.auth.Identity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;

public class LoginActivity extends Activity implements View.OnClickListener {

  private static final String TAG = "Login";

  private Button mLoginButton;
  private EditText mDisplayNameText;
  private EditText mPasswordText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    mDisplayNameText = (EditText) findViewById(R.id.display_name_text);
    mPasswordText = (EditText) findViewById(R.id.password_text);
    mLoginButton = (Button) findViewById(R.id.login_button);
    mLoginButton.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    try {
      Identity identity = new Identity(
          -1,
          mDisplayNameText.getText().toString(),
          mPasswordText.getText().toString());

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
