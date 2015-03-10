package com.everythingissauce.pifuxelck;

import android.os.Handler;
import android.os.Looper;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executors;

public class ThreadUtil {

  public static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());

  public static final ListeningExecutorService THREAD_POOL =
      MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

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

  private ThreadUtil() {}
}
