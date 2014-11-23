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

  private static final boolean DEBUG = false;

  /**
   * A task that will render the drawing into a Bitmap on a background thread
   * and update the mDrawingCache field on the UI thread when the cache has been
   * rendered.
   */
  private class CreateDrawingCacheTask extends AsyncTask<Void, Void, Bitmap> {

    private int mSize;
    private Drawing mDrawing;

    public CreateDrawingCacheTask(
        int size,
        AbstractDrawing drawing) {
      mSize = size;
      mDrawing = new Drawing.Builder(drawing).build();
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
      Bitmap cache = Bitmap.createBitmap(mSize, mSize, Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(cache);
      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      drawDrawing(canvas, paint, mDrawing, mSize);
      return cache;
    }

    @Override
    protected void onPostExecute(Bitmap cache) {
      mDrawingCache = cache;
      mDrawingCacheTask = null;
      if (mQueuedDrawingCacheTask) {
        refreshDrawingCache();
      }

      if (DEBUG) Log.d(TAG, "Finished drawing, invalidating view: " + mDrawing);

      // Signal that the DrawingView should be redrawn now that the cache has
      // been updated.
      invalidate();
    }
  }

  /**
   * A task that will render the drawing into a Bitmap on a background thread
   * and update the mDrawingCache field on the UI thread when the cache has been
   * rendered.
   */
  private class CreateLineCacheTask extends AsyncTask<Void, Void, Bitmap> {

    private int mSize;
    private Line mLine;

    public CreateLineCacheTask(
        int size,
        AbstractLine line) {
      mSize = size;
      mLine = new Line.Builder(line).build();
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
      Bitmap cache = Bitmap.createBitmap(mSize, mSize, Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(cache);
      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      drawLine(canvas, paint, mLine, mSize);
      return cache;
    }

    @Override
    protected void onPostExecute(Bitmap cache) {
      mLineCache = cache;
      mLineCacheTask = null;
      if (mQueuedLineCacheTask) {
        refreshDrawingCache();
      }

      if (DEBUG) Log.d(TAG, "Finished drawing, invalidating view: " + mDrawing);

      // Signal that the DrawingView should be redrawn now that the cache has
      // been updated.
      invalidate();
    }
  }

  @Nullable private AbstractDrawing mDrawing;
  @Nullable private AbstractLine mInProgressLine;

  @Nullable private Bitmap mDrawingCache;
  @Nullable private CreateDrawingCacheTask mDrawingCacheTask;
  private boolean mQueuedDrawingCacheTask = false;

  @Nullable private Bitmap mLineCache;
  @Nullable private CreateLineCacheTask mLineCacheTask;
  private boolean mQueuedLineCacheTask = false;

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
  public void clearDrawingCache() {
    mDrawingCache = null;
    if (mDrawingCacheTask != null) {
      mDrawingCacheTask.cancel(true);
      mDrawingCacheTask = null;
      mQueuedDrawingCacheTask = false;
    }
  }

  /**
   * Clears the cache. This method MUST be called on the UI thread.
   */
  public void clearLineCache() {
    mLineCache = null;
    if (mLineCacheTask != null) {
      mLineCacheTask.cancel(true);
      mLineCacheTask = null;
      mQueuedLineCacheTask = false;
    }
  }

  /**
   * Refreshes the current drawing cache. This method MUST be called on the UI
   * thread.
   */
  public void refreshDrawingCache() {
    refreshDrawingCache(getWidth());
  }

  /**
   * Refreshes the current line cache. This method MUST be called on the UI
   * thread.
   */
  public void refreshLineCache() {
    refreshDrawingCache(getWidth());
  }

  /**
   * Refreshes the current cache. This method MUST be called on the UI thread.
   */
  private void refreshDrawingCache(int size) {
    // If there isn't a drawing, then refreshing the cache is pointless.
    if (mDrawing == null) {
      if (DEBUG) Log.d(TAG, "No drawing, not creating cache");
      return;
    }

    // No point in creating an empty bitmap.
    if (size == 0) {
      if (DEBUG) Log.d(TAG, "Size is 0: " + mDrawing);
      return;
    }

    // If there is an ongoing task to refresh the cache, then mark that another
    // refresh is desired after the current one completes.
    if (mDrawingCacheTask != null) {
      if (DEBUG) Log.d(TAG, "Already running, queueing cache task: " + mDrawing);
      mQueuedDrawingCacheTask = true;
      return;
    }

    if (DEBUG) Log.d(TAG, "Starting cache task: " + mDrawing);

    mQueuedDrawingCacheTask = false;
    mDrawingCacheTask = new CreateDrawingCacheTask(size, mDrawing);
    mDrawingCacheTask.execute();
  }

  /**
   * Refreshes the current cache. This method MUST be called on the UI thread.
   */
  private void refreshLineCache(int size) {
    // If there isn't a drawing, then refreshing the cache is pointless.
    if (mInProgressLine == null) {
      if (DEBUG) Log.d(TAG, "No Line, not creating cache");
      return;
    }

    // No point in creating an empty bitmap.
    if (size == 0) {
      if (DEBUG) Log.d(TAG, "Size is 0: " + mDrawing);
      return;
    }

    // If there is an ongoing task to refresh the cache, then mark that another
    // refresh is desired after the current one completes.
    if (mLineCacheTask != null) {
      if (DEBUG) Log.d(TAG, "Already running, queueing cache task: " + mDrawing);
      mQueuedLineCacheTask = true;
      return;
    }

    if (DEBUG) Log.d(TAG, "Starting line cache task: " + mDrawing);

    mQueuedLineCacheTask = false;
    mLineCacheTask = new CreateLineCacheTask(size, mInProgressLine);
    mLineCacheTask.execute();
  }

  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Force the DrawingView to always be square.
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);
    int size = width > height && height > 0 ? height : width;
    setMeasuredDimension(size, size);

    refreshDrawingCache(size);
    refreshLineCache(size);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Rect destination = new Rect(0, 0, getWidth(), getHeight());

    if (mDrawingCache == null && mDrawing != null) {
      if (DEBUG) Log.d(TAG, "Cache is null, drawing drawing: " + mDrawing);
      drawDrawing(canvas, paint, mDrawing, getWidth());
    } else if (mDrawingCache != null) {
      if (DEBUG) Log.d(TAG, "Drawing from the cache: " + mDrawing);
      canvas.drawBitmap(mDrawingCache, null, destination, null);
    }

    if (mLineCache == null && mInProgressLine != null) {
      if (DEBUG) Log.d(TAG, "Cache is null, drawing line: " + mDrawing);
      drawLine(canvas, paint, mInProgressLine, getWidth());
    } else if (mLineCache != null) {
      if (DEBUG) Log.d(TAG, "Drawing line from the cache: " + mDrawing);
      canvas.drawBitmap(mLineCache, null , destination, null);
    }
  }

  private static void drawDrawing(
      Canvas canvas, Paint paint, AbstractDrawing drawing, int size) {
    canvas.drawColor(drawing.getBackgroundColor().toAndroidColor());

    for (Line drawingLine : drawing) {
      drawLine(canvas, paint, drawingLine, size);
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
