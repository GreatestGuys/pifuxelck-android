package com.everythingissauce.pifuxelck.data;

/**
 * A common interface for objects that can be treated like a Drawing.
 */
public interface AbstractDrawing extends Iterable<Line> {

  Color getBackgroundColor();
}
