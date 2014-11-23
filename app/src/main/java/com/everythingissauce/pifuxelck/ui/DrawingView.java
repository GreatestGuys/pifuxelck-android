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
      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

      canvas.drawColor(mDrawing.getBackgroundColor().toAndroidColor());

      for (Line line : mDrawing) {
        drawLine(canvas, paint, line);
      }

      if (mLine != null) {
        drawLine(canvas, paint, mLine);
      }

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

    private void drawLine(
        Canvas canvas, Paint paint, AbstractLine line) {
      paint.setColor(line.getColor().toAndroidColor());
      paint.setStrokeWidth((float) line.getSize() * mSize);

      boolean isFirst = true;
      for (LineSegment segment : line) {
        canvas.drawLine(
            (int) (segment.getFirst().getX() * mSize),
            (int) (segment.getFirst().getY() * mSize),
            (int) (segment.getSecond().getX() * mSize),
            (int) (segment.getSecond().getY() * mSize),
            paint);

        // Only draw a circle over the first point of the line segment for the
        // very first point in the sequence.
        if (isFirst) {
          canvas.drawCircle(
              (float) segment.getFirst().getX() * mSize,
              (float) segment.getFirst().getY() * mSize,
              (float) line.getSize() / 2 * mSize,
              paint);
        }

        canvas.drawCircle(
            (float) segment.getSecond().getX() * mSize,
            (float) segment.getSecond().getY() * mSize,
            (float) line.getSize() / 2 * mSize,
            paint);

        isFirst = false;
      }
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

    if (mCache == null) {
      Log.i(TAG, "Cache is null, not drawing: " + mDrawing);
      canvas.drawColor(Color.WHITE);
      return;
    }

    Log.i(TAG, "Drawing from the cache: " + mDrawing);

    Rect destination = new Rect(0, 0, getWidth(), getHeight());
    canvas.drawBitmap(mCache, null /* src */, destination, null /* paint */);
  }
}
