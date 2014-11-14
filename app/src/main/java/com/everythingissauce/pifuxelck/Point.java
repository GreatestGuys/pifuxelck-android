package com.everythingissauce.pifuxelck;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * A point along a line segment. Points are defined as X-Y pairs where each
 * coordinate is a floating point value between 0 and 1 inclusive. The
 * coordinate (0, 0) corresponds to the upper left corner of the plane, while
 * the coordinate (1, 1) corresponds to the lower right.
 * <p>
 * This class is immutable.
 */
public class Point {

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
