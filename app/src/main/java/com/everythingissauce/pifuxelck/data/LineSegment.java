package com.everythingissauce.pifuxelck.data;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * A line segment represented by two points. This class is immutable.
 */
public class LineSegment {

  private final Point mFirst;
  private final Point mSecond;

  public LineSegment(Point first, Point second) {
    mFirst = first;
    mSecond = second;
  }

  public Point getFirst() {
    return mFirst;
  }

  public Point getSecond() {
    return mSecond;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("LineSegment")
        .add("first", mFirst)
        .add("second", mSecond)
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof LineSegment)) {
      return false;
    }
    LineSegment otherLine = (LineSegment) other;
    return mFirst.equals(otherLine.mFirst)
        && mSecond.equals(otherLine.mSecond);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mFirst, mSecond);
  }
}
