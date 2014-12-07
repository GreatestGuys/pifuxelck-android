package com.everythingissauce.pifuxelck;

import android.util.Base64;

/**
 * Provides static utility methods that wrap Android's base64 encoding routines
 * using the standard Pifuxelck encoding flags.
 */
public class Base64Util {

  private static final int BASE64_FLAGS
      = Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE;

  public static byte[] decode(String string) {
    return Base64.decode(string, BASE64_FLAGS);
  }

  public static String encode(byte[] data) {
    return Base64.encodeToString(data, BASE64_FLAGS);
  }
}
