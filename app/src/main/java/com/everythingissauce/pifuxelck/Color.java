package com.everythingissauce.pifuxelck;

/**
 * The color of a line segment in a drawing. This class uses floating point
 * values on between 0 and 1 inclusive to represent colors.
 * <p>
 * This class is immutable.
 */
public class Color {

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

  private int to255(double colorValue) {
    return (int) (colorValue * 255);
  }
}
