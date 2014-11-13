package com.everythingissauce.pifuxelck;

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
}
