package com.everythingissauce.pifuxelck.data;

import android.os.Bundle;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * The color of a line segment in a drawing. This class uses floating point
 * values on between 0 and 1 inclusive to represent colors.
 * <p>
 * This class is immutable.
 */
public class Color {

  private static final String ALPHA_KEY = "alpha";
  private static final String BLUE_KEY = "blue";
  private static final String GREEN_KEY = "green";
  private static final String RED_KEY = "red";

  public static final Color WHITE = new Color(1, 1, 1);
  public static final Color BLACK = new Color(0, 0, 0);

  private final double mAlpha;
  private final double mBlue;
  private final double mGreen;
  private final double mRed;

  public Color(double red, double green, double blue) {
    mAlpha = 1;
    mRed = clamp(red);
    mGreen = clamp(green);
    mBlue = clamp(blue);
  }

  public Color(double red, double green, double blue, double alpha) {
    mAlpha = clamp(alpha);
    mRed = clamp(red);
    mGreen = clamp(green);
    mBlue = clamp(blue);
  }

  private double clamp(double value) {
    return value > 1 ? 1 : (value < 0 ? 0 : value);
  }

  public double getRed() {
    return mRed;
  }

  public double getGreen() {
    return mGreen;
  }

  public double getBlue() {
    return mBlue;
  }

  public double getAlpha() {
    return mAlpha;
  }

  public int toAndroidColor() {
    return android.graphics.Color.argb(
        to255(mAlpha),
        to255(mRed),
        to255(mGreen),
        to255(mBlue));
  }

  public Color invert() {
    return new Color(1 - mRed, 1 - mGreen, 1 - mBlue, mAlpha);
  }

  private int to255(double colorValue) {
    return (int) (colorValue * 255);
  }

  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putDouble(RED_KEY, mRed);
    bundle.putDouble(GREEN_KEY, mGreen);
    bundle.putDouble(BLUE_KEY, mBlue);
    bundle.putDouble(ALPHA_KEY, mAlpha);
    return bundle;
  }

  public static Color fromBundle(Bundle bundle) {
    return new Color(
        bundle.getDouble(RED_KEY),
        bundle.getDouble(GREEN_KEY),
        bundle.getDouble(BLUE_KEY),
        bundle.getDouble(ALPHA_KEY, 1.0));
  }

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(RED_KEY, mRed);
    json.put(GREEN_KEY, mGreen);
    json.put(BLUE_KEY, mBlue);
    json.put(ALPHA_KEY, mAlpha);
    return json;
  }

  public static Color fromJson(JSONObject json) throws JSONException {
    return new Color(
        json.getDouble(RED_KEY),
        json.getDouble(GREEN_KEY),
        json.getDouble(BLUE_KEY),
        json.getDouble(ALPHA_KEY));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Color")
        .add("red", mRed)
        .add("green", mGreen)
        .add("blue", mBlue)
        .add("alpha", mAlpha)
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Color)) {
      return false;
    }
    Color otherColor = (Color) other;
    return mRed == otherColor.mRed
        && mGreen == otherColor.mGreen
        && mBlue == otherColor.mBlue
        && mAlpha == otherColor.mAlpha;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mRed, mGreen, mBlue, mAlpha);
  }
}
