package com.everythingissauce.pifuxelck.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.Settings;
import com.everythingissauce.pifuxelck.auth.Identity;
import com.everythingissauce.pifuxelck.storage.IdentityProvider;

import org.json.JSONException;

public class SettingsActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

  private static final String TAG = "SettingsActivity";

  private static final String EXPORT_ACCOUNT_FILE_TITLE = "account.json";

  private CheckBox mShouldVibrate;
  private CheckBox mShouldConfirmSend;
  private View mChangePasswordLayout;
  private View mShouldVibrateLayout;
  private View mShouldConfirmSendLayout;

  private IdentityProvider mIdentityProvider;

  private Settings mSettings;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    mSettings = new Settings(this);

    mShouldConfirmSendLayout = findViewById(R.id.should_confirm_send_layout);
    mShouldConfirmSendLayout.setOnClickListener(this);

    mShouldVibrateLayout = findViewById(R.id.should_vibrate_layout);
    mShouldVibrateLayout.setOnClickListener(this);

    mChangePasswordLayout = findViewById(R.id.change_password_layout);
    mChangePasswordLayout.setOnClickListener(this);

    mIdentityProvider = new IdentityProvider(this);

    mShouldVibrate = (CheckBox) findViewById(R.id.should_vibrate);
    mShouldVibrate.setChecked(mSettings.shouldVibrate());
    mShouldVibrate.setOnCheckedChangeListener(this);

    mShouldConfirmSend = (CheckBox) findViewById(R.id.should_confirm_send);
    mShouldConfirmSend.setChecked(mSettings.shouldConfirmSend());
    mShouldConfirmSend.setOnCheckedChangeListener(this);
  }

  @Override
  public void onClick(View view) {
    boolean checked;
    switch(view.getId()) {
      case R.id.change_password_layout:
        startActivity(new Intent(this, PasswordChangeActivity.class));
        break;
      case R.id.should_confirm_send_layout:
        checked = !mShouldConfirmSend.isChecked();
        mShouldConfirmSend.setChecked(checked);
        mSettings.setShouldConfirmSend(checked);
        break;
      case R.id.should_vibrate_layout:
        checked = !mShouldVibrate.isChecked();
        mShouldVibrate.setChecked(checked);
        mSettings.setShouldVibrate(checked);
        break;
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
    switch (compoundButton.getId()) {
      case R.id.should_vibrate:
        mSettings.setShouldVibrate(checked);
        break;
      case R.id.should_confirm_send:
        mSettings.setShouldConfirmSend(checked);
        break;
    }
  }
}