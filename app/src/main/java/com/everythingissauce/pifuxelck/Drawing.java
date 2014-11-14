package com.everythingissauce.pifuxelck;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Drawing implements Iterable<Line> {

  private final Object mLock = new Object();
  private final List<Line> mLines = new ArrayList<Line>();

  public void pushLine(Line line) {
    synchronized (mLock) {
      mLines.add(line);
    }
  }

  public void popLine() {
    synchronized (mLock) {
      int size = mLines.size();
      if (size != 0) {
        mLines.remove(size - 1);
      }
    }
  }

  @Override
  public Iterator<Line> iterator() {
    synchronized (mLock) {
      return mLines.iterator();
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Drawing")
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
