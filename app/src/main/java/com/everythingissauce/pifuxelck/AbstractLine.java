package com.everythingissauce.pifuxelck;

/**
 * A common interface for objects that can be treated like a Line.
 */
public interface AbstractLine extends Iterable<LineSegment> {

  Color getColor();

  double getSize();
}
