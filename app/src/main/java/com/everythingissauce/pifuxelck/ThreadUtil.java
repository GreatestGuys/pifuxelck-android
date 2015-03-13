package com.everythingissauce.pifuxelck;

import android.os.Handler;
import android.os.Looper;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executors;

public class ThreadUtil {

  public static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());

  public static final ListeningExecutorService THREAD_POOL = newThreadPool(16);

  public static ListeningExecutorService newThreadPool(int size) {
    return MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(size));
  }

  public static <V> void callbackOnUi(
      ListenableFuture<V> future,
      final FutureCallback<V> callback) {
    Futures.addCallback(future, new FutureCallback<V>() {
      @Override
      public void onSuccess(final V result) {
        UI_HANDLER.post(new Runnable() {
          @Override
          public void run() {
            callback.onSuccess(result);
          }
        });
      }

      @Override
      public void onFailure(final Throwable t) {
        UI_HANDLER.post(new Runnable() {
          @Override
          public void run() {
            callback.onFailure(t);
          }
        });
      }
    }, THREAD_POOL);
  }

  public static <V> ListenableFuture<Void> voidFuture(
      ListenableFuture<V>  future) {
    return Futures.transform(future, voidTransform(), THREAD_POOL);
  }

  public static <V> Function<V, Void> voidTransform() {
    return new Function<V, Void>() {
      @Override
      public Void apply(V input) {
        return null;
      }
    };
  }

  public static void checkUI() {
    Preconditions.checkState(
        Looper.getMainLooper().equals(Looper.myLooper()),
        "Must be called from the UI thread.");
  }

  private ThreadUtil() {}
}
