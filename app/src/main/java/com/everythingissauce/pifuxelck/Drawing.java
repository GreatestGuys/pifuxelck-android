package com.everythingissauce.pifuxelck;

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
 * A collection of lines and a background color. This class is immutable.
 */
public class Drawing implements AbstractDrawing {

  /**
   * A builder for Drawing objects. This class is not thread safe.
   */
  public static class Builder implements AbstractDrawing {

    private Color mBackgroundColor = Color.WHITE;
    private final List<Line> mLines = new ArrayList<Line>();

    public static Builder fromDrawing(AbstractDrawing drawing) {
      Builder builder = new Builder();
      builder.setBackgroundColor(drawing.getBackgroundColor());
      for (Line line : drawing) {
        builder.pushLine(line);
      }
      return builder;
    }

    public Builder setBackgroundColor(Color backgroundColor) {
      mBackgroundColor = backgroundColor;
      return this;
    }

    @Override
    public Color getBackgroundColor() {
      return mBackgroundColor;
    }

    public Builder pushLine(Line line) {
      mLines.add(line);
      return this;
    }

    public Builder popLine() {
      int size = mLines.size();
      if (size != 0) {
        mLines.remove(size - 1);
      }
      return this;
    }

    public Drawing build() {
      return new Drawing(mBackgroundColor, mLines);
    }

    @Override
    public Iterator<Line> iterator() {
      return mLines.iterator();
    }
  }

  private static final String BACKGROUND_KEY = "background_color";
  private static final String LINES_KEY = "lines";

  private final Color mBackgroundColor;
  private final Line[] mLines;

  public Drawing(Color backgroundColor, List<Line> lines) {
    mBackgroundColor = backgroundColor;

    mLines = new Line[lines.size()];
    for (int i = 0; i < lines.size(); i++) {
      mLines[i] = lines.get(i);
    }
  }

  @Override
  public Color getBackgroundColor() {
    return mBackgroundColor;
  }

  @Override
  public Iterator<Line> iterator() {
    return Arrays.asList(mLines).iterator();
  }

  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putBundle(BACKGROUND_KEY, mBackgroundColor.toBundle());

    ArrayList<Bundle> lineBundleList = new ArrayList<Bundle>();
    for (Line line : mLines) {
      lineBundleList.add(line.toBundle());
    }
    bundle.putParcelableArrayList(LINES_KEY, lineBundleList);

    return bundle;
  }

  public static Drawing fromBundle(Bundle bundle) {
    List<Line> lineList = new ArrayList<Line>();
    List<Bundle> lineBundleList = bundle.getParcelableArrayList(LINES_KEY);
    for (Bundle lineBundle : lineBundleList) {
      lineList.add(Line.fromBundle(lineBundle));
    }
    return new Drawing(
        Color.fromBundle(bundle.getBundle(BACKGROUND_KEY)),
        lineList);
  }

  public JSONObject toJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(BACKGROUND_KEY, mBackgroundColor.toJson());

    JSONArray lineArray = new JSONArray();
    for (Line line : mLines) {
      lineArray.put(line.toJson());
    }
    json.put(LINES_KEY, lineArray);

    return json;
  }

  public static Drawing fromJson(JSONObject json) throws JSONException {
    List<Line> lineList = new ArrayList<Line>();
    JSONArray lineJsonArray = json.getJSONArray(LINES_KEY);
    for (int i = 0; i < lineJsonArray.length(); i++) {
      lineList.add(Line.fromJson(lineJsonArray.getJSONObject(i)));
    }

    return new Drawing(
        Color.fromJson(json.getJSONObject(BACKGROUND_KEY)),
        lineList);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Drawing")
        .add("background", mBackgroundColor)
        .add("lines", mLines)
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Drawing)) {
      return false;
    }
    Drawing otherDrawing = (Drawing) other;
    return mLines.equals(otherDrawing.mLines);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mLines);
  }
}
