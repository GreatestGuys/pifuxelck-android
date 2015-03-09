package com.everythingissauce.pifuxelck.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;

import com.everythingissauce.pifuxelck.data.AbstractDrawing;
import com.everythingissauce.pifuxelck.data.AbstractLine;
import com.everythingissauce.pifuxelck.data.Line;
import com.everythingissauce.pifuxelck.data.LineSegment;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class DrawingUtil {

  private static final ListeningExecutorService sThreadPool =
      MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

  public static ListenableFuture<Bitmap> renderDrawing(
      final AbstractDrawing drawing,
      final Bitmap bitmap) {
    return sThreadPool.submit(new Callable<Bitmap>() {
      @Override
      public Bitmap call() throws Exception {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = newPaint();

        canvas.drawColor(drawing.getBackgroundColor().toAndroidColor());

        for (Line drawingLine : drawing) {
          drawLine(canvas, paint, drawingLine, bitmap.getWidth());
        }

        return bitmap;
      }
    });
  }

  public static ListenableFuture<Bitmap> renderDrawing(
      final AbstractDrawing drawing,
      final int size) {
    return Futures.transform(
        futureDrawingBitmap(size),
        new AsyncFunction<Bitmap, Bitmap>() {
          @Override
          public ListenableFuture<Bitmap> apply(Bitmap input) {
            return renderDrawing(drawing, input);
          }
        });
  }

  public static ListenableFuture<Bitmap> renderLine(
      final AbstractLine line,
      final int size) {
    return Futures.transform(
        futureLineBitmap(size),
        new AsyncFunction<Bitmap, Bitmap>() {
          @Override
          public ListenableFuture<Bitmap> apply(Bitmap input) {
            return renderLine(line, input);
          }
        });
  }

  public static ListenableFuture<Bitmap> renderLine(
      final AbstractLine line,
      final Bitmap bitmap) {
    return sThreadPool.submit(new Callable<Bitmap>() {
      @Override
      public Bitmap call() throws Exception {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = newPaint();

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawLine(canvas, paint, line, bitmap.getWidth());

        return bitmap;
      }
    });
  }

  public static Bitmap newDrawingBitmap(int size) {
    return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
  }

  public static Bitmap newLineBitmap(int size) {
    return Bitmap.createBitmap(size, size,  Bitmap.Config.ARGB_4444);
  }

  private static ListenableFuture<Bitmap> futureDrawingBitmap(final int size) {
    return sThreadPool.submit(new Callable<Bitmap>() {
      @Override
      public Bitmap call() throws Exception {
        return newDrawingBitmap(size);
      }
    });
  }

  private static ListenableFuture<Bitmap> futureLineBitmap(final int size) {
    return sThreadPool.submit(new Callable<Bitmap>() {
      @Override
      public Bitmap call() throws Exception {
        return newLineBitmap(size);
      }
    });
  }

  private static Paint newPaint() {
    return new Paint(Paint.ANTI_ALIAS_FLAG);
  }

  private static void drawLine(
      Canvas canvas,
      Paint paint,
      AbstractLine line,
      int size) {
    paint.setColor(line.getColor().toAndroidColor());
    paint.setStrokeWidth((float) line.getSize() * size);

    // Only draw a circle over the first point of the line segment for the
    // very first point in the sequence.
    canvas.drawCircle(
        (float) line.getPoint(0).getX() * size,
        (float) line.getPoint(0).getY() * size,
        (float) line.getSize() / 2 * size,
        paint);

    for (LineSegment segment : line) {
      canvas.drawLine(
          (int) (segment.getFirst().getX() * size),
          (int) (segment.getFirst().getY() * size),
          (int) (segment.getSecond().getX() * size),
          (int) (segment.getSecond().getY() * size),
          paint);

      canvas.drawCircle(
          (float) segment.getSecond().getX() * size,
          (float) segment.getSecond().getY() * size,
          (float) line.getSize() / 2 * size,
          paint);
    }
  }
}
