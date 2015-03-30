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

  private Button mExportAccountButton;
  private Button mChangePasswordButton;
  private IdentityProvider mIdentityProvider;
  private CheckBox mShouldVibrate;
  private CheckBox mShouldConfirmSend;

  private Settings mSettings;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    mSettings = new Settings(this);

    mExportAccountButton = (Button) findViewById(R.id.export_account_button);
    mExportAccountButton.setOnClickListener(this);

    mChangePasswordButton = (Button) findViewById(R.id.change_password_button);
    mChangePasswordButton.setOnClickListener(this);

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
    switch(view.getId()) {
      case R.id.export_account_button:
        exportAccount();
        break;
      case R.id.change_password_button:
        startActivity(new Intent(this, PasswordChangeActivity.class));
        break;
    }
  }

  private void exportAccount() {
    Identity identity = mIdentityProvider.getIdentity();
    if (identity == null) {
      return;
    }

    try {
      startActivity(Intent.createChooser(
          new Intent(Intent.ACTION_SEND)
              .setType("text/plain")
              .putExtra(Intent.EXTRA_TEXT, identity.toJson().toString())
              .putExtra(Intent.EXTRA_SUBJECT, EXPORT_ACCOUNT_FILE_TITLE),
          getString(R.string.settings_export_account_chooser_title)));
    } catch (JSONException exception) {
      Log.e(TAG, "Unable to serialize account JSON.", exception);
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