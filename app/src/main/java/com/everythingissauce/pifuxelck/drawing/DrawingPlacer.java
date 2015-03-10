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
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;

public class DrawingPlacer implements View.OnLayoutChangeListener {

  private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());
  private static final ListeningExecutorService sThreadPool =
      MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));

  private final Object mLock = new Object();
  private final WeakHashMap<View, ListenableFuture<Bitmap>> mViewToFuture;
  private final WeakHashMap<View, Bitmap> mViewToBitmap;
  private final WeakHashMap<View, Drawing> mViewToDrawing;

  public DrawingPlacer() {
    mViewToFuture = new WeakHashMap<>();
    mViewToBitmap = new WeakHashMap<>();
    mViewToDrawing = new WeakHashMap<>();
  }

  public void placeDrawingInView(
      final Drawing drawing,
      final ImageView imageView) {
    sThreadPool.submit(new Runnable() {
      @Override
      public void run() {
        placeDrawingInViewHeavy(drawing, imageView);
      }
    });
  }

  private void placeDrawingInViewHeavy(
      final Drawing drawing,
      ImageView imageView) {
    imageView.addOnLayoutChangeListener(this);
    setImageViewBitmap(imageView, null);

    final ListenableFuture<Bitmap> bitmapFuture;
    synchronized (mLock) {
      Drawing oldDrawing = mViewToDrawing.get(imageView);
      mViewToDrawing.put(imageView, drawing);

      Bitmap bitmap =  mViewToBitmap.get(imageView);
      if (bitmap != null
          && oldDrawing != null
          && imageView.getDrawable() != null
          && bitmap.getWidth() == imageView.getWidth()
          && oldDrawing.equals(drawing)) {
        return;
      }

      if (bitmap == null || bitmap.getWidth() < imageView.getWidth()) {
        bitmap = DrawingUtil.newDrawingBitmap(imageView.getWidth());
        mViewToBitmap.put(imageView, bitmap);
      }

      ListenableFuture<Bitmap> previousFuture = mViewToFuture.get(imageView);
      if(previousFuture != null) previousFuture.cancel(true);

      bitmapFuture = DrawingUtil.renderDrawing(drawing, bitmap);
      mViewToFuture.put(imageView, bitmapFuture);
    }

    final WeakReference<ImageView> weakView = new WeakReference<>(imageView);
    Futures.addCallback(bitmapFuture, new FutureCallback<Bitmap>() {
      @Override
      public void onSuccess(final Bitmap result) {
        final ImageView imageView = weakView.get();
        if (imageView == null) {
          return;
        }

        // Check if the current bitmap that is intended for this view is
        // the future that just finished. If it is, set the image view to
        // the resulting bitmap and remove it from the map.
        synchronized (mLock) {
          ListenableFuture<Bitmap> future = mViewToFuture.get(imageView);
          if (future != bitmapFuture || bitmapFuture.isCancelled()) {
            return;
          }
          mViewToFuture.remove(imageView);
          setImageViewBitmap(imageView, result);
        }
      }

      @Override
      public void onFailure(Throwable t) {
        // Do nothing.
      }
    }, sThreadPool);
  }

  @Override
  public void onLayoutChange(
      View view,
      int left, int top, int right, int bottom,
      int oldLeft, int oldTop, int oldRight, int oldBottom) {
    synchronized (mLock) {
      Bitmap bitmap = mViewToBitmap.get(view);
      if (bitmap != null && bitmap.getWidth() != 0) {
        return;
      }

      Drawing drawing = mViewToDrawing.get(view);
      if (drawing == null) {
        return;
      }

      ListenableFuture<Bitmap> future = mViewToFuture.get(view);
      if (future != null) {
        future.cancel(true);
      }
      placeDrawingInView(drawing, (ImageView) view);
    }
  }

  private void setImageViewBitmap(
      final ImageView imageView,
      final Bitmap bitmap) {
    UI_HANDLER.post(new Runnable() {
      @Override
      public void run() {
        imageView.clearAnimation();
        imageView.startAnimation(
            AnimationUtils.loadAnimation(
                imageView.getContext(),
                R.anim.drawing_placer_fade_in));
        imageView.setImageBitmap(bitmap);
      }
    });
  }
}
