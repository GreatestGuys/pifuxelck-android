package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.AbstractDrawing;
import com.everythingissauce.pifuxelck.AbstractLine;
import com.everythingissauce.pifuxelck.Line;
import com.everythingissauce.pifuxelck.LineSegment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.support.annotation.Nullable;


/**
 * A View that is capable of rendering an {@link AbstractDrawing}.
 */
public class DrawingView extends View {

  @Nullable private AbstractDrawing mDrawing;
  @Nullable private AbstractLine mInProgressLine;

  public DrawingView(Context context) {
    super(context);
  }

  public DrawingView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void setInProgressLine(@Nullable AbstractLine line) {
    mInProgressLine = line;
  }

  public void setDrawing(@Nullable AbstractDrawing drawing) {
    mDrawing = drawing;
  }

  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Force the DrawingView to always be square.
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);
    int size = width > height ? height : width;
    setMeasuredDimension(size, size);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (mDrawing == null) {
      return;
    }

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    int viewSize = getWidth();

    canvas.drawColor(mDrawing.getBackgroundColor().toAndroidColor());

    for (Line line : mDrawing) {
      drawLine(canvas, paint, line);
    }

    if (mInProgressLine != null) {
      drawLine(canvas, paint, mInProgressLine);
    }
  }

  private void drawLine(
      Canvas canvas, Paint paint, AbstractLine line) {
    int viewSize = getWidth();

    paint.setColor(line.getColor().toAndroidColor());
    paint.setStrokeWidth((float) line.getSize() * viewSize);

    boolean isFirst = true;
    for (LineSegment segment : line) {
      canvas.drawLine(
          (int) (segment.getFirst().getX() * viewSize),
          (int) (segment.getFirst().getY() * viewSize),
          (int) (segment.getSecond().getX() * viewSize),
          (int) (segment.getSecond().getY() * viewSize),
          paint);

      // Only draw a circle over the first point of the line segment for the
      // very first point in the sequence.
      if (isFirst) {
        canvas.drawCircle(
            (float) segment.getFirst().getX() * viewSize,
            (float) segment.getFirst().getY() * viewSize,
            (float) line.getSize() / 2 * viewSize,
            paint);
      }

      canvas.drawCircle(
          (float) segment.getSecond().getX() * viewSize,
          (float) segment.getSecond().getY() * viewSize,
          (float) line.getSize() / 2 * viewSize,
          paint);

      isFirst = false;
    }
  }
}
