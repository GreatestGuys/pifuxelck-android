package com.everythingissauce.pifuxelck.drawing;

import android.graphics.Bitmap;

import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.ThreadUtil;
import com.everythingissauce.pifuxelck.auth.Identity;
import com.everythingissauce.pifuxelck.data.Drawing;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.WeakHashMap;
import java.util.concurrent.FutureTask;

public class DrawingPlacer implements View.OnLayoutChangeListener {

  private static final String TAG = "DrawingPlacer";
  private static final boolean DEBUG = true;

  // Must only be accessed from the UI thread.
  private final WeakHashMap<View, DrawingPlacingRunnable> mViewToRunnable;

  public DrawingPlacer() {
    mViewToRunnable = new WeakHashMap<>();
  }

  private void checkUI() {
    Preconditions.checkState(
        Looper.getMainLooper().equals(Looper.myLooper()),
        "Must be called from the UI thread.");
  }

  public void clearView(ImageView view) {
    checkUI();
    Preconditions.checkNotNull(view);

    view.setImageBitmap(null);
    mViewToRunnable.remove(view);
    view.removeOnLayoutChangeListener(this);
  }

  public void placeDrawingInView(Drawing drawing, ImageView view) {
    checkUI();
    Preconditions.checkNotNull(drawing);
    Preconditions.checkNotNull(view);

    DrawingPlacingRunnable runnable = mViewToRunnable.get(view);
    if (runnable != null && runnable.mDrawing.equals(drawing)) {
      return;
    }

    view.setImageBitmap(null);
    view.addOnLayoutChangeListener(this);
    mViewToRunnable.put(view, new DrawingPlacingRunnable(drawing, view));
    placeDrawingInViewInBackground(view);
  }

  @Override
  public void onLayoutChange(
      View view,
      int left, int top, int right, int bottom,
      int oldLeft, int oldTop, int oldRight, int oldBottom) {
    // Do not bother redrawing the image if the size has not changed.
    if (oldRight - oldLeft == right - left
        && oldBottom - oldTop == bottom - top) {
      return;
    }

    placeDrawingInViewInBackground(view);
  }

  private void placeDrawingInViewInBackground(View view) {
    checkUI();

    Runnable runnable = mViewToRunnable.get(view);
    if (runnable != null) {
      runnable.run();
    }
  }

  private class DrawingPlacingRunnable implements Runnable {

    private final Drawing mDrawing;
    private final ImageView mView;

    public DrawingPlacingRunnable(Drawing drawing, ImageView view) {
      mDrawing = drawing;
      mView = view;
    }

    @Override
    public void run() {
      int size = mView.getWidth();
      if (size == 0) {
        return;
      }

      ListenableFuture<Bitmap> bitmap =
          DrawingUtil.renderDrawing(mDrawing, size);

      ThreadUtil.callbackOnUi(bitmap, new FutureCallback<Bitmap>() {
        @Override
        public void onSuccess(Bitmap result) {
          checkUI();

          if (mViewToRunnable.get(mView) != DrawingPlacingRunnable.this) {
            return;
          }

          mView.setImageBitmap(result);
          mView.clearAnimation();
          mView.startAnimation(
              AnimationUtils.loadAnimation(
                  mView.getContext(),
                  R.anim.drawing_placer_fade_in));
        }

        @Override
        public void onFailure(Throwable t) {
        }
      });
    }
  }

//  public void clearView(ImageView view) {
//    checkUI();
//
//    synchronized (mLock) {
//      mViewToDrawing.remove(view);
//      mViewToBitmap.remove(view);
//      setFutureForView(view, null);
//      setListener(view, null);
//    }
//
//    view.setImageBitmap(null);
//  }
//
//  public void placeDrawingInView(Drawing drawing, ImageView view) {
//    checkUI();
//
//    if (DEBUG) Log.i(TAG, "placeDrawingInView (" + view + ")");
//
//    synchronized (mLock) {
//      Drawing oldDrawing = mViewToDrawing.get(view);
//      // Do nothing if this drawing is already placed/in the process of being
//      // placed in this image view.
//      if (oldDrawing != null && oldDrawing.equals(drawing)) {
//        if (DEBUG) Log.i(TAG, "Drawing is already placed (" + view + ").");
//        return;
//      }
//      mViewToDrawing.put(view, drawing);
//
//      view.clearAnimation();
//      view.setImageBitmap(null);
//
//      // Delay placing the drawing into the image view until a layout pass
//      // has occurred. This will ensure that the size obtained via getWidth()
//      // is the most up to date value.
//      if (DEBUG) Log.i(TAG, "Registering on layout listener (" + view + ").");
//      PlacingLayoutListener listener = new PlacingLayoutListener(view, drawing);
//      setListener(view, listener);
//      view.postInvalidate();
//      view.post(listener);
//    }
//  }
//
//  private void setListener(
//      View view,
//      PlacingLayoutListener listener) {
//    checkUI();
//
//    synchronized (mLock) {
//      PlacingLayoutListener oldListener = mViewToListener.get(view);
//      if (oldListener != null) {
//        view.removeOnLayoutChangeListener(oldListener);
//        //view.getViewTreeObserver().removeGlobalOnLayoutListener(oldListener);
//      }
//
//      if (listener != null) {
//        view.addOnLayoutChangeListener(listener);
//        //view.getViewTreeObserver().addOnGlobalLayoutListener(listener);
//        mViewToListener.put(view, listener);
//      } else {
//        mViewToListener.remove(view);
//      }
//    }
//  }
//
//  private void placeDrawingInViewHeavy(final Drawing drawing, final ImageView view) {
//    final ListenableFuture<Bitmap> bitmapFuture;
//    synchronized (mLock) {
//      final Bitmap bitmap = getBitmap(view);
//      ListenableFuture<Bitmap> previousFuture = mViewToFuture.get(view);
//      final SettableFuture<Void> signal = SettableFuture.create();
//      if (previousFuture != null) {
//        previousFuture.addListener(new Runnable() {
//          @Override
//          public void run() {
//            signal.set(null);
//          }
//        }, ThreadUtil.THREAD_POOL);
//        previousFuture.cancel(true);
//      } else {
//        signal.set(null);
//      }
//      bitmapFuture = Futures.transform(
//          signal,
//          new AsyncFunction<Void, Bitmap>() {
//            @Override
//            public ListenableFuture<Bitmap> apply(Void input) throws Exception {
//              return DrawingUtil.renderDrawing(drawing, bitmap);
//            }
//          }, ThreadUtil.THREAD_POOL);
//      setFutureForView(view, bitmapFuture);
//    }
//
//    ThreadUtil.callbackOnUi(bitmapFuture, new FutureCallback<Bitmap>() {
//      @Override
//      public void onSuccess(final Bitmap result) {
//        synchronized (mLock) {
//          ListenableFuture<Bitmap> previousFuture = mViewToFuture.get(view);
//          if (previousFuture != bitmapFuture) {
//            return;
//          }
//          mViewToFuture.remove(bitmapFuture);
//          view.clearAnimation();
//          view.startAnimation(
//              AnimationUtils.loadAnimation(
//                  view.getContext(),
//                  R.anim.drawing_placer_fade_in));
//          view.setImageBitmap(result);
//        }
//      }
//
//      @Override
//      public void onFailure(Throwable t) {
//      }
//    });
//  }
//
//  private void setFutureForView(
//      ImageView view, ListenableFuture<Bitmap> future) {
//    synchronized (mLock) {
//      ListenableFuture<Bitmap> previousFuture = mViewToFuture.get(view);
//      if (previousFuture != null) {
//        previousFuture.cancel(true);
//      }
//
//      if (future != null) {
//        mViewToFuture.put(view, future);
//      } else {
//        mViewToFuture.remove(view);
//      }
//    }
//  }
//
//  private Bitmap getBitmap(ImageView view) {
//    synchronized (mLock) {
//      Bitmap bitmap = mViewToBitmap.get(view);
//      if (bitmap == null || bitmap.getWidth() < view.getWidth()) {
//        bitmap = DrawingUtil.newDrawingBitmap(view.getWidth());
//        mViewToBitmap.put(view, bitmap);
//        if (DEBUG) Log.i(TAG, "Creating new bitmap (" + view + ").");
//      }
//      return bitmap;
//    }
//  }
//
//  private class PlacingLayoutListener implements
//      View.OnLayoutChangeListener,
//      ViewTreeObserver.OnGlobalLayoutListener,
//      Runnable {
//
//    private final Drawing mDrawing;
//    private final ImageView mView;
//
//    public PlacingLayoutListener(ImageView view, Drawing drawing) {
//      mView = view;
//      mDrawing = drawing;
//    }
//
//    @Override
//    public void onLayoutChange(
//        final View view,
//        int left, int top, int right, int bottom,
//        int oldLeft, int oldTop, int oldRight, int oldBottom) {
//      if (DEBUG) Log.i(TAG, "On layout changed (" + mView + ").");
//      run();
//    }
//
//    @Override
//    public void onGlobalLayout() {
//      if (DEBUG) Log.i(TAG, "On global layout (" + mView + ").");
//      run();
//    }
//
//    @Override
//    public void run() {
//      checkUI();
//
//      if (DEBUG) Log.i(TAG, "Listener running (" + mView + ").");
//
//      if (mView.getWidth() == 0) {
//        if (DEBUG) Log.i(TAG, "View has no size, waiting for layout pass.");
//        return;
//      }
//
//      synchronized (mLock) {
//        PlacingLayoutListener currentListener = mViewToListener.get(mView);
//        if (currentListener != this) {
//          if (DEBUG) Log.i(TAG, "Not the current listener (" + mView + ").");
//          return;
//        }
//        Drawing drawing = mViewToDrawing.get(mView);
//        if (!mDrawing.equals(drawing)) {
//          if (DEBUG) Log.i(TAG, "Not the current drawing (" + mView + ").");
//          return;
//        }
//
//        setListener(mView, null);
//
//        ThreadUtil.THREAD_POOL.submit(new Runnable() {
//          @Override
//          public void run() {
//            placeDrawingInViewHeavy(mDrawing, mView);
//          }
//        });
//      }
//    }
//  }
}
