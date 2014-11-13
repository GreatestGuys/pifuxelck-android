package com.everythingissauce.pifuxelck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A styled list of line segments in a drawing. This class is immutable.
 */
public class Line implements Iterable<LineSegment> {

  public static class Builder {

    private Color mColor = new Color(0, 0, 0);
    private final List<Point> mPoints = new ArrayList<Point>();
    private double mSize = 0.01;

    public Builder setColor(Color color) {
      mColor = color;
      return this;
    }

    public Builder addPoint(double x, double y) {
      mPoints.add(new Point(x, y));
      return this;
    }

    public Builder addPoint(Point point) {
      mPoints.add(point);
      return this;
    }

    public Builder setSize(double size) {
      mSize = size < 0 ? 0 : size;
      return this;
    }

    public Line build() {
      return new Line(mColor, mSize, mPoints);
    }
  }

  private final Color mColor;
  private final Point[] mPoints;
  private final double mSize;

  private Line(Color color, double size, List<Point> points) {
    mColor = color;
    mSize = size;

    mPoints = new Point[points.size()];
    for (int i = 0; i < points.size(); i++) {
      mPoints[i] = points.get(i);
    }
  }

  public Color getColor() {
    return mColor;
  }

  @Override
  public Iterator<LineSegment> iterator() {
    return new LineSegmentIterator();
  }

  public double getSize() {
    return mSize;
  }

  private class LineSegmentIterator implements Iterator<LineSegment> {

    // mIndex points to the point in the array that will be the first point of
    // the next line segment returned by next().
    int mIndex = 0;

    @Override
    public boolean hasNext() {
      return mIndex < (mPoints.length - 1);
    }

    @Override
    public LineSegment next() {
      LineSegment segment = new LineSegment(mPoints[mIndex], mPoints[mIndex + 1]);
      mIndex++;
      return segment;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException(
          "LineSegments cannot be removed from a line!");
    }
  }
}
