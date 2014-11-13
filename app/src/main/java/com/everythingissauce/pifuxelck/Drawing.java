package com.everythingissauce.pifuxelck;

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
}
