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
  private static final boolean DEBUG = false;

  // Must only be accessed from the UI thread.
  private final WeakHashMap<View, DrawingPlacingRunnable> mViewToRunnable;

  public DrawingPlacer() {
    mViewToRunnable = new WeakHashMap<>();
  }

  public void clearView(ImageView view) {
    ThreadUtil.checkUI();
    Preconditions.checkNotNull(view);

    view.setImageBitmap(null);
    mViewToRunnable.remove(view);
    view.removeOnLayoutChangeListener(this);
  }

  public void placeDrawingInView(Drawing drawing, ImageView view) {
    ThreadUtil.checkUI();
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
    ThreadUtil.checkUI();

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
          ThreadUtil.checkUI();

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
}
