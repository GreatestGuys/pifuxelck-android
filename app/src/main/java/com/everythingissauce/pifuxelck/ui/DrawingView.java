package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.AbstractDrawing;
import com.everythingissauce.pifuxelck.AbstractLine;
import com.everythingissauce.pifuxelck.Drawing;
import com.everythingissauce.pifuxelck.Line;
import com.everythingissauce.pifuxelck.LineSegment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * A View that is capable of rendering an {@link AbstractDrawing}.
 */
public class DrawingView extends View {

  private static final String TAG = "DrawingView";

  /**
   * A task that will render the drawing into a Bitmap on a background thread
   * and update the mCache field on the UI thread when the cache has been
   * rendered.
   */
  private class CreateCacheTask extends AsyncTask<Void, Void, Bitmap> {

    private int mSize;
    private Drawing mDrawing;
    @Nullable private Line mLine;

    public CreateCacheTask(
        int size,
        AbstractDrawing drawing,
        AbstractLine line) {
      mSize = size;
      mDrawing = new Drawing.Builder(drawing).build();
      mLine = line == null ? null : new Line.Builder(line).build();
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
      // TODO(will): Consider using RGB_565 on low memory devices...
      Bitmap cache = Bitmap.createBitmap(mSize, mSize, Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(cache);
      drawDrawingAndLine(canvas, mDrawing, mLine, mSize);
      return cache;
    }

    @Override
    protected void onPostExecute(Bitmap cache) {
      mCache = cache;
      mCacheTask = null;
      if (mQueuedCacheTask) {
        refreshCache();
      }

      Log.i(TAG, "Finished drawing, invalidating view: " + mDrawing);

      // Signal that the DrawingView should be redrawn now that the cache has
      // been updated.
      invalidate();
    }
  }

  @Nullable private AbstractDrawing mDrawing;
  @Nullable private AbstractLine mInProgressLine;
  @Nullable private Bitmap mCache;

  @Nullable private CreateCacheTask mCacheTask;
  private boolean mQueuedCacheTask = false;

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

  /**
   * Clears the cache. This method MUST be called on the UI thread.
   */
  public void clearCache() {
    mCache = null;
    if (mCacheTask != null) {
      mCacheTask.cancel(true);
      mCacheTask = null;
      mQueuedCacheTask = false;
    }
  }

  /**
   * Refreshes the current cache. This method MUST be called on the UI thread.
   */
  public void refreshCache() {
    refreshCache(getWidth());
  }

  /**
   * Refreshes the current cache. This method MUST be called on the UI thread.
   */
  private void refreshCache(int size) {
    // If there isn't a drawing, then refreshing the cache is pointless.
    if (mDrawing == null) {
      Log.i(TAG, "No drawing, not creating cache");
      return;
    }

    // No point in creating an empty bitmap.
    if (size == 0) {
      Log.i(TAG, "Size is 0: " + mDrawing);
      return;
    }

    // If there is an ongoing task to refresh the cache, then mark that another
    // refresh is desired after the current one completes.
    if (mCacheTask != null) {
      Log.i(TAG, "Already running, queueing cache task: " + mDrawing);
      mQueuedCacheTask = true;
      return;
    }

    Log.i(TAG, "Starting cache task: " + mDrawing);

    mQueuedCacheTask = false;
    mCacheTask = new CreateCacheTask(size, mDrawing, mInProgressLine);
    mCacheTask.execute();
  }

  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Force the DrawingView to always be square.
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);
    int size = width > height && height > 0 ? height : width;
    setMeasuredDimension(size, size);

    refreshCache(size);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (mDrawing == null) {
      Log.i(TAG, "No drawing, clearing canvas.");
      canvas.drawColor(Color.WHITE);
    }

    if (mCache == null) {
      Log.i(TAG, "Cache is null, not drawing: " + mDrawing);
      drawDrawingAndLine(canvas, mDrawing, mInProgressLine, getWidth());
      return;
    }

    Log.i(TAG, "Drawing from the cache: " + mDrawing);

    Rect destination = new Rect(0, 0, getWidth(), getHeight());
    canvas.drawBitmap(mCache, null /* src */, destination, null /* paint */);
  }

  private static void drawDrawingAndLine(
      Canvas canvas, AbstractDrawing drawing, AbstractLine line, int size) {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    canvas.drawColor(drawing.getBackgroundColor().toAndroidColor());

    for (Line drawingLine : drawing) {
      drawLine(canvas, paint, drawingLine, size);
    }

    if (line != null) {
      drawLine(canvas, paint, line, size);
    }
  }

  private static void drawLine(
      Canvas canvas, Paint paint, AbstractLine line, int size) {
    paint.setColor(line.getColor().toAndroidColor());
    paint.setStrokeWidth((float) line.getSize() * size);

    boolean isFirst = true;
    for (LineSegment segment : line) {
      canvas.drawLine(
          (int) (segment.getFirst().getX() * size),
          (int) (segment.getFirst().getY() * size),
          (int) (segment.getSecond().getX() * size),
          (int) (segment.getSecond().getY() * size),
          paint);

      // Only draw a circle over the first point of the line segment for the
      // very first point in the sequence.
      if (isFirst) {
        canvas.drawCircle(
            (float) segment.getFirst().getX() * size,
            (float) segment.getFirst().getY() * size,
            (float) line.getSize() / 2 * size,
            paint);
      }

      canvas.drawCircle(
          (float) segment.getSecond().getX() * size,
          (float) segment.getSecond().getY() * size,
          (float) line.getSize() / 2 * size,
          paint);

      isFirst = false;
    }
  }
}
