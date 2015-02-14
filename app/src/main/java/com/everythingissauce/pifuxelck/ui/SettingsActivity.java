package com.everythingissauce.pifuxelck.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.auth.Identity;
import com.everythingissauce.pifuxelck.storage.IdentityProvider;

import org.json.JSONException;

public class SettingsActivity extends Activity implements View.OnClickListener {

  private static final String TAG = "SettingsActivity";

  private static final String EXPORT_ACCOUNT_FILE_TITLE = "account.json";

  private Button mExportAccountButton;
  private IdentityProvider mIdentityProvider;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    mExportAccountButton = (Button) findViewById(R.id.export_account_button);
    mExportAccountButton.setOnClickListener(this);
    mIdentityProvider = new IdentityProvider(this);
  }

  @Override
  public void onClick(View view) {
    switch(view.getId()) {
      case R.id.export_account_button:
        exportAccount();
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
}