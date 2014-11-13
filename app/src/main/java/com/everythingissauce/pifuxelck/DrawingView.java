package com.everythingissauce.pifuxelck;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by will on 11/10/14.
 *
 * This class should be re-written to separate out the notion of drawing from the view. But for
 * fucking around. this is fine.
 */
public class DrawingView extends View {

  private static String TAG = "DrawingView";

  Drawing mDrawing = new Drawing();
  Line.Builder mCurrentLine = null;

  public DrawingView(Context context) {
    super(context);
    installTouchListener();
  }

  public DrawingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    installTouchListener();
  }

  public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    installTouchListener();
  }

  public void installTouchListener() {
    setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        double viewSize = view.getWidth();
        double scaledX = motionEvent.getAxisValue(MotionEvent.AXIS_X) / viewSize;
        double scaledY = motionEvent.getAxisValue(MotionEvent.AXIS_Y) / viewSize;

        switch (motionEvent.getActionMasked()) {
          case MotionEvent.ACTION_DOWN:
            mCurrentLine = new Line.Builder()
                .setSize(0.05)
                .setColor(new Color(1, 0, 0))
                .addPoint(scaledX, scaledY);
            Log.e(TAG, "DOWN: " + scaledX + ", " + scaledY);
            return true;
          case MotionEvent.ACTION_UP:
            mCurrentLine.addPoint(scaledX, scaledY);
            mDrawing.pushLine(mCurrentLine.build());
            mCurrentLine = null;
            Log.e(TAG, "UP: " + scaledX + ", " + scaledY);
            view.invalidate();
            return true;
          case MotionEvent.ACTION_MOVE:
            mCurrentLine.addPoint(scaledX, scaledY);
            Log.e(TAG, "MOVE: " + scaledX + ", " + scaledY);
            return true;
        }
        return false;
      }
    });
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

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    int viewSize = getWidth();

    canvas.drawColor(android.graphics.Color.WHITE);
    for (Line line : mDrawing) {
      paint.setColor(line.getColor().toAndroidColor());
      paint.setStrokeWidth((float) line.getSize() * viewSize);
      for (LineSegment segment : line) {
        canvas.drawLine(
            (int) (segment.getFirst().getX() * viewSize),
            (int) (segment.getFirst().getY() * viewSize),
            (int) (segment.getSecond().getX() * viewSize),
            (int) (segment.getSecond().getY() * viewSize),
            paint);
      }
    }
  }
}
