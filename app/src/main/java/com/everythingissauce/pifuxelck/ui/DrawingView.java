package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.ThreadUtil;
import com.everythingissauce.pifuxelck.data.AbstractDrawing;
import com.everythingissauce.pifuxelck.data.AbstractLine;
import com.everythingissauce.pifuxelck.data.Drawing;
import com.everythingissauce.pifuxelck.data.Line;
import com.everythingissauce.pifuxelck.data.LineSegment;
import com.everythingissauce.pifuxelck.drawing.DrawingUtil;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
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

  @Nullable private AbstractDrawing mDrawing;
  @Nullable private AbstractLine mInProgressLine;

  @Nullable private Bitmap mDrawingCache;
  @Nullable private ListenableFuture<Bitmap> mDrawingFuture;
  private boolean mDrawingQueued;

  @Nullable private Bitmap mLineCache;
  @Nullable private ListenableFuture mLineFuture;
  private boolean mLineQueued;

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
    ThreadUtil.checkUI();

    mInProgressLine = line;
    if (line == null) {
      mLineCache = null;
      if (mLineFuture != null) mLineFuture.cancel(true);
    } else {
      onInProgressLineChanged();
    }
  }

  public void setDrawing(@Nullable AbstractDrawing drawing) {
    ThreadUtil.checkUI();

    mDrawing = drawing;
    onDrawingChanged();
  }

  public void onInProgressLineChanged() {
    ThreadUtil.checkUI();

    if (mLineFuture != null) {
      mLineQueued = true;
      return;
    }

    mLineFuture = DrawingUtil.renderLine(mInProgressLine, getWidth());
    ThreadUtil.callbackOnUi(mLineFuture, new FutureCallback<Bitmap>() {
      @Override
      public void onSuccess(final Bitmap result) {
        ThreadUtil.UI_HANDLER.post(new Runnable() {
          @Override
          public void run() {
            if (DEBUG) Log.i(TAG, "Line future rendered.");
            mLineCache = result;
            mLineFuture = null;

            maybeRenderQueuedLine();
            invalidate();
          }
        });
      }

      @Override
      public void onFailure(Throwable t) {
        if (DEBUG) Log.i(TAG, "Line future failed.");
        mLineFuture = null;
        maybeRenderQueuedLine();
      }
    });
  }

  private void maybeRenderQueuedLine() {
    if (mLineQueued) {
      onInProgressLineChanged();
    }
    mLineQueued = false;
  }

  public void onDrawingChanged() {
    ThreadUtil.checkUI();

    if (mDrawingFuture != null) {
      mDrawingQueued = true;
      return;
    }

    mDrawingFuture = DrawingUtil.renderDrawing(mDrawing, getWidth());
    ThreadUtil.callbackOnUi(mDrawingFuture, new FutureCallback<Bitmap>() {
      @Override
      public void onSuccess(final Bitmap result) {
        ThreadUtil.UI_HANDLER.post(new Runnable() {
          @Override
          public void run() {
            if (DEBUG) Log.i(TAG, "Drawing future rendered.");
            mDrawingCache = result;
            mDrawingFuture = null;

            invalidate();
            maybeRenderQueuedDrawing();
          }
        });
      }

      @Override
      public void onFailure(Throwable t) {
        if (DEBUG) Log.i(TAG, "Drawing future failed.");
        mDrawingFuture = null;
        maybeRenderQueuedDrawing();
      }
    });
  }

  private void maybeRenderQueuedDrawing() {
    if (mDrawingQueued) {
      onDrawingChanged();
    }
    mDrawingQueued = false;
  }

  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Force the DrawingView to always be square.
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);
    int size = width > height && height > 0 ? height : width;
    setMeasuredDimension(size, size);

    onInProgressLineChanged();
    onDrawingChanged();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    Rect destination = new Rect(0, 0, getWidth(), getHeight());

    if (mDrawingCache != null) {
      if (DEBUG) Log.i(TAG, "Drawing drawing cache.");
      canvas.drawBitmap(mDrawingCache, null, destination, null);
    }

    if (mLineCache != null) {
      if (DEBUG) Log.i(TAG, "Drawing line cache.");
      canvas.drawBitmap(mLineCache, null , destination, null);
    }
  }
}
