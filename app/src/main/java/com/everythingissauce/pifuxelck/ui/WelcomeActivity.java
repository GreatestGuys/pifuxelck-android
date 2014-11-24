package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.auth.Identity;
import com.everythingissauce.pifuxelck.storage.IdentityProvider;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class WelcomeActivity extends Activity implements View.OnClickListener {

  private static final String TAG = "WelcomeActivity";

  private Button mLoginButton;
  private EditText mDisplayNameEditText;
  private View mProgressOverlay;

  private IdentityProvider mIdentityProvider;

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

    Identity.Partial partialIdentity = new Identity.Partial(displayName);
    mIdentityProvider.setIdentity(partialIdentity.build(0));
    openInbox();
  }

  private void openInbox() {
    Intent intent = new Intent();
    intent.setClass(this.getApplicationContext(), InboxActivity.class);
    startActivity(intent);
  }
}
