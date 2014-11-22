package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class WelcomeActivity extends Activity implements View.OnClickListener {

  private Button mLoginButton;
  private EditText mDisplayNameEditText;
  private View mProgressOverlay;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_welcome);

    mDisplayNameEditText = (EditText) findViewById(R.id.display_name_edit_text);
    mProgressOverlay = findViewById(R.id.progress_overlay);

    mLoginButton = (Button) findViewById(R.id.login_button);
    mLoginButton.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    String displayName = mDisplayNameEditText.getText().toString();

    if (displayName.length() == 0) {
      return;
    }

    Intent intent = new Intent();
    intent.setClass(this.getApplicationContext(), DrawingActivity.class);
    startActivity(intent);
  }
}
