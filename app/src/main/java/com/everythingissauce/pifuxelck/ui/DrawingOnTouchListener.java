package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.Color;
import com.everythingissauce.pifuxelck.Drawing;
import com.everythingissauce.pifuxelck.Line;

import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;

/**
 * An {@link View.OnTouchListener} that will listen to touch events and update a
 * {@link Drawing.Builder}.
 */
public class DrawingOnTouchListener implements View.OnTouchListener {

  /**
   * The minimum distance that must exist between points before a line segment
   * will be created to join them.
   */
  private static final double MIN_POINT_DISTANCE_SQUARED_PIXEL = 400;

  private final DrawingView mDrawingView;
  private final Drawing.Builder mDrawingBuilder;
  @Nullable private Line.Builder mInProgressLine;
  private Color mCurrentColor;
  private double mCurrentSize;

  private double mLastX;
  private double mLastY;

  private DrawingOnTouchListener(DrawingView view, Drawing.Builder builder) {
    mDrawingView = view;
    mDrawingBuilder = builder;
    mInProgressLine = null;
    mCurrentColor = Color.BLACK;
    mCurrentSize = 0.025;
    mLastX = 0;
    mLastY = 0;
  }

  /**
   * Creates a {@code DrawingOnTouchListener} and sets it as the OnTouchListener
   * for the given DrawingView.
   */
  public static DrawingOnTouchListener install(
      DrawingView view,
      Drawing.Builder builder) {
    DrawingOnTouchListener listener = new DrawingOnTouchListener(view, builder);
    view.setOnTouchListener(listener);
    return listener;
  }

  /**
   * Sets the associated {@link DrawingView}'s touch listener to be null.
   */
  public void uninstall() {
    mDrawingView.setOnTouchListener(null);
  }

  public void setCurrentColor(Color color) {
    mCurrentColor = color;
  }

  @Override
  public boolean onTouch(View view, MotionEvent motionEvent) {
    double viewSize = view.getWidth();
    double x = motionEvent.getAxisValue(MotionEvent.AXIS_X);
    double y = motionEvent.getAxisValue(MotionEvent.AXIS_Y);
    double scaledX = x / viewSize;
    double scaledY = y / viewSize;

    switch (motionEvent.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        mInProgressLine = new Line.Builder()
            .setSize(mCurrentSize)
            .setColor(mCurrentColor)
            .addPoint(scaledX, scaledY);
        mDrawingView.setInProgressLine(mInProgressLine);

        mLastX = x;
        mLastY = y;
        return true;

      case MotionEvent.ACTION_UP:
        mInProgressLine.addPoint(scaledX, scaledY);
        mDrawingBuilder.pushLine(mInProgressLine.build());
        mInProgressLine = null;
        mDrawingView.setInProgressLine(null);
        mDrawingView.invalidate();
        return true;

      case MotionEvent.ACTION_MOVE:
        double distance = Math.pow(mLastX - x, 2)
                        + Math.pow(mLastY - y, 2);
        if (MIN_POINT_DISTANCE_SQUARED_PIXEL > distance) {
          return true;
        }

        mInProgressLine.addPoint(scaledX, scaledY);
        mDrawingView.invalidate();

        mLastX = x;
        mLastY = y;
        return true;
    }

    // Return false to indicate that we are not interested in this message or
    // any future related message.
    return false;
  }
}
