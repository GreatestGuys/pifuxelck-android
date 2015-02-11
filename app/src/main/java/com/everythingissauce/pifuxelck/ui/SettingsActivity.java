package com.everythingissauce.pifuxelck.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.everythingissauce.pifuxelck.R;

public class SettingsActivity extends Activity implements View.OnClickListener {

  private static final Uri IDENTITY_URI = Uri.parse(
          "content://com.everythingissauce.pifuxelck.identity/");

  private Button mExportAccountButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    mExportAccountButton = (Button) findViewById(R.id.export_account_button);
    mExportAccountButton.setOnClickListener(this);
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
    startActivity(Intent.createChooser(
        new Intent(Intent.ACTION_SEND).setData(IDENTITY_URI),
        getString(R.string.settings_export_account_chooser_title)));
  }
}