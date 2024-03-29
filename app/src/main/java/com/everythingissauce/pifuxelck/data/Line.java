package com.everythingissauce.pifuxelck.data;

import android.os.Bundle;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A styled list of line segments in a drawing. This class is immutable.
 */
public class Line implements AbstractLine {

  /**
   * A builder for Line objects. This class is not thread safe.
   */
  public static class Builder implements AbstractLine {

    private Color mColor;
    private final List<Point> mPoints;
    private double mSize;

    public Builder() {
      mColor = new Color(0, 0, 0);
      mPoints = new ArrayList<Point>();
      mSize = 0.01;
    }

    public Builder(AbstractLine line) {
      mColor = line.getColor();
      mSize = line.getSize();
      mPoints = new ArrayList<Point>();

      Iterator<LineSegment> iterator = line.iterator();
      LineSegment currentSegment = null;
      while (iterator.hasNext()) {
        currentSegment = iterator.next();
        mPoints.add(currentSegment.getFirst());
      }

      if (currentSegment != null) {
        mPoints.add(currentSegment.getSecond());
      }
    }

    public Builder setColor(Color color) {
      mColor = color;
      return this;
    }

    @Override
    public Color getColor() {
      return mColor;
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

    @Override
    public double getSize() {
      return mSize;
    }

    @Override
    public Point getPoint(int i) {
      return mPoints.get(i);
    }

    public Line build() {
      return new Line(mColor, mSize, mPoints);
    }

    @Override
    public Iterator<LineSegment> iterator() {
      return new Iterator<LineSegment>() {

        // mIndex points to the point in the array that will be the first point of
        // the next line segment returned by next().
        int mIndex = 0;

        @Override
        public boolean hasNext() {
          return mIndex < (mPoints.size() - 1);
        }

        @Override
        public LineSegment next() {
          LineSegment segment = new LineSegment(mPoints.get(mIndex), mPoints.get(mIndex + 1));
          mIndex++;
          return segment;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException(
              "LineSegments cannot be removed from a line!");
        }
      };
    }
  }

  private static final String COLOR_KEY = "color";
  private static final String POINTS_KEY = "points";
  private static final String SIZE_KEY = "size";

  private final Color mColor;
  private final Point[] mPoints;
  private final double mSize;

  private Line(Color color, double size, List<Point> points) {
    mColor = color;
    mSize = size;

    mPoints = new Point[points.size()];
    points.toArray(mPoints);
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

  @Override
  public Point getPoint(int i) {
    return mPoints[i];
  }

  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putBundle(COLOR_KEY, mColor.toBundle());
    bundle.putDouble(SIZE_KEY, mSize);

    ArrayList<Bundle> pointBundleList = new ArrayList<Bundle>();
    for (Point point : mPoints) {
      pointBundleList.add(point.toBundle());
    }
    bundle.putParcelableArrayList(POINTS_KEY, pointBundleList);

    return bundle;
  }

  public static Line fromBundle(Bundle bundle) {
    List<Point> pointList = new ArrayList<Point>();
    List<Bundle> pointBundleList = bundle.getParcelableArrayList(POINTS_KEY);
    for (Bundle pointBundle : pointBundleList) {
      pointList.add(Point.fromBundle(pointBundle));
    }
    return new Line(
        Color.fromBundle(bundle.getBundle(COLOR_KEY)),
        bundle.getDouble(SIZE_KEY),
        pointList);
  }

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(COLOR_KEY, mColor.toJson());
    json.put(SIZE_KEY, mSize);

    JSONArray pointArray = new JSONArray();
    for (Point point : mPoints) {
      pointArray.put(point.toJson());
    }
    json.put(POINTS_KEY, pointArray);

    return json;
  }

  public static Line fromJson(JSONObject json) throws JSONException {
    List<Point> pointList = new ArrayList<Point>();
    JSONArray pointJsonArray = json.getJSONArray(POINTS_KEY);
    for (int i = 0; i < pointJsonArray.length(); i++) {
      pointList.add(Point.fromJson(pointJsonArray.getJSONObject(i)));
    }

    return new Line(
        Color.fromJson(json.getJSONObject(COLOR_KEY)),
        json.getDouble(SIZE_KEY),
        pointList);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Line")
        .add("color", mColor)
        .add("size", mSize)
        .add("points", Arrays.asList(mPoints))
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Line)) {
      return false;
    }
    Line otherLine = (Line) other;
    return mColor.equals(otherLine.mColor)
        && mSize == otherLine.mSize
        && Arrays.equals(mPoints, otherLine.mPoints);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mColor, mSize, Arrays.asList(mPoints));
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
