package com.everythingissauce.pifuxelck.data;

import android.os.Bundle;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * A point along a line segment. Points are defined as X-Y pairs where each
 * coordinate is a floating point value between 0 and 1 inclusive. The
 * coordinate (0, 0) corresponds to the upper left corner of the plane, while
 * the coordinate (1, 1) corresponds to the lower right.
 * <p>
 * This class is immutable.
 */
public class Point {

  private static final String X_KEY = "x";
  private static final String Y_KEY = "y";

  private final double mX;
  private final double mY;

  public Point(double x, double y) {
    mX = x;
    mY = y;
  }

  private double clamp(double value) {
    return value > 1 ? 1 : (value < 0 ? 0 : value);
  }

  public double getX() {
    return mX;
  }

  public double getY() {
    return mY;
  }

  public Point offset(double x, double y) {
    return new Point(mX + x, mY + y);
  }

  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putDouble(X_KEY, mX);
    bundle.putDouble(Y_KEY, mY);
    return bundle;
  }

  public static Point fromBundle(Bundle bundle) {
    return new Point(bundle.getDouble(X_KEY), bundle.getDouble(Y_KEY));
  }

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(X_KEY, mX);
    json.put(Y_KEY, mY);
    return json;
  }

  public static Point fromJson(JSONObject json) throws JSONException {
    return new Point(json.getDouble(X_KEY), json.getDouble(Y_KEY));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Point")
        .add("x", mX)
        .add("y", mY)
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Point)) {
      return false;
    }
    Point otherPoint = (Point) other;
    return mX == otherPoint.mX
        && mY == otherPoint.mY;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mX, mY);
  }
}
