package com.everythingissauce.pifuxelck;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

  private static final String PREFS_FILE = "settings";

  private static final String SETTING_VIBRATE = "should_vibrate";

  private final Context mContext;

  public Settings(Context context) {
    mContext = context;
  }

  private SharedPreferences getPrefs() {
    return mContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
  }

  public boolean shouldVibrate() {
    return getPrefs().getBoolean(SETTING_VIBRATE, true);
  }

  public void setShouldVibrate(boolean shouldVibrate) {
    getPrefs().edit().putBoolean(SETTING_VIBRATE, shouldVibrate).apply();
  }
}
