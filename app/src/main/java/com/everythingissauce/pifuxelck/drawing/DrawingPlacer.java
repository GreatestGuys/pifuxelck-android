package com.everythingissauce.pifuxelck.drawing;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.data.Drawing;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.WeakHashMap;
import java.util.concurrent.Executors;

public class DrawingPlacer {

  private static final String TAG = "DrawingPlacer";
  private static final boolean DEBUG = false;

  private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());
  private static final ListeningExecutorService sThreadPool =
      MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

  private final Object mLock = new Object();
  private final WeakHashMap<View, ListenableFuture<Bitmap>> mViewToFuture;
  private final WeakHashMap<View, Bitmap> mViewToBitmap;
  private final WeakHashMap<View, Drawing> mViewToDrawing;
  private final WeakHashMap<View, View.OnLayoutChangeListener> mViewToListener;

  public DrawingPlacer() {
    mViewToFuture = new WeakHashMap<>();
    mViewToBitmap = new WeakHashMap<>();
    mViewToDrawing = new WeakHashMap<>();
    mViewToListener = new WeakHashMap<>();
  }

  private void checkUI() {
    Preconditions.checkState(
        Looper.getMainLooper().equals(Looper.myLooper()),
        "Must be called from the UI thread.");
  }

  public void placeDrawingInView(Drawing drawing, ImageView view) {
    checkUI();

    if (DEBUG) Log.i(TAG, "placeDrawingInView (" + view + ")");

    synchronized (mLock) {
      Drawing oldDrawing = mViewToDrawing.get(view);
      // Do nothing if this drawing is already placed/in the process of being
      // placed in this image view.
      if (oldDrawing != null && oldDrawing.equals(drawing)) {
        if (DEBUG) Log.i(TAG, "Drawing is already placed (" + view + ").");
        return;
      }
      mViewToDrawing.put(view, drawing);

      view.clearAnimation();
      view.setImageBitmap(null);

      // Delay placing the drawing into the image view until a layout pass
      // has occurred. This will ensure that the size obtained via getWidth()
      // is the most up to date value.
      if (DEBUG) Log.i(TAG, "Registering on layout listener (" + view + ").");
      setOnLayoutChangeListener(view, new PlacingLayoutListener());
      view.invalidate();
    }
  }

  private void setOnLayoutChangeListener(
      View view,
      View.OnLayoutChangeListener listener) {
    checkUI();

    synchronized (mLock) {
      View.OnLayoutChangeListener oldListener = mViewToListener.get(view);
      if (oldListener != null) {
        view.removeOnLayoutChangeListener(oldListener);
      }
      view.addOnLayoutChangeListener(listener);
      mViewToListener.put(view, listener);
    }
  }

  private void placeDrawingInViewHeavy(Drawing drawing, final ImageView view) {
    final ListenableFuture<Bitmap> bitmapFuture;
    synchronized (mLock) {
      Bitmap bitmap = getBitmap(view);
      bitmapFuture = DrawingUtil.renderDrawing(drawing, bitmap);
      setFutureForView(view, bitmapFuture);
    }

    Futures.addCallback(bitmapFuture, new FutureCallback<Bitmap>() {
      @Override
      public void onSuccess(final Bitmap result) {
        UI_HANDLER.post(new Runnable() {
          @Override
          public void run() {
            ListenableFuture<Bitmap> previousFuture = mViewToFuture.get(view);
            if (previousFuture != bitmapFuture) {
              return;
            }
            mViewToFuture.remove(bitmapFuture);
            view.clearAnimation();
            view.startAnimation(
                AnimationUtils.loadAnimation(
                    view.getContext(),
                    R.anim.drawing_placer_fade_in));
            view.setImageBitmap(result);
          }
        });
      }

      @Override
      public void onFailure(Throwable t) {
      }
    });
  }

  private void setFutureForView(
      ImageView view, ListenableFuture<Bitmap> future) {
    synchronized (mLock) {
      ListenableFuture<Bitmap> previousFuture = mViewToFuture.get(view);
      if (previousFuture != null) {
        previousFuture.cancel(true);
      }
      mViewToFuture.put(view, future);
    }
  }

  private Bitmap getBitmap(ImageView view) {
    synchronized (mLock) {
      Bitmap bitmap = mViewToBitmap.get(view);
      if (bitmap == null || bitmap.getWidth() < view.getWidth()) {
        bitmap = DrawingUtil.newDrawingBitmap(view.getWidth());
        mViewToBitmap.put(view, bitmap);
        if (DEBUG) Log.i(TAG, "Creating new bitmap (" + view + ").");
      }
      return bitmap;
    }
  }

  private class PlacingLayoutListener implements View.OnLayoutChangeListener {

    @Override
    public void onLayoutChange(
        final View view,
        int left, int top, int right, int bottom,
        int oldLeft, int oldTop, int oldRight, int oldBottom) {
      checkUI();

      if (DEBUG) Log.i(TAG, "Layout changed (" + view + ").");

      synchronized (mLock) {
        View.OnLayoutChangeListener currentListener = mViewToListener.get(view);
        if (currentListener != this) {
          if (DEBUG) Log.i(TAG, "Not the current listener (" + view + ").");
          return;
        }
        mViewToListener.remove(view);
        view.removeOnLayoutChangeListener(this);

        final Drawing drawing = mViewToDrawing.get(view);
        if (drawing == null) {
          if (DEBUG) Log.i(TAG, "No drawing (" + view +  ").");
          return;
        }

        sThreadPool.submit(new Runnable() {
          @Override
          public void run() {
            placeDrawingInViewHeavy(drawing, (ImageView) view);
          }
        });
      }
    }
  }
}
