package com.everythingissauce.pifuxelck;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

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
