package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.data.Drawing;
import com.everythingissauce.pifuxelck.data.Game;
import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.data.Turn;
import com.everythingissauce.pifuxelck.drawing.DrawingPlacer;
import com.everythingissauce.pifuxelck.storage.HistoryStore;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class HistoryAdapter extends CursorAdapter {

  private static final int[] DRAWING_FRAME_VIEW_IDS = new int[] {
    R.id.drawing_1_frame,
    R.id.drawing_2_frame,
    R.id.drawing_3_frame
  };

  private static final int[] DRAWING_VIEW_IDS = new int[] {
    R.id.drawing_1_view,
    R.id.drawing_2_view,
    R.id.drawing_3_view
  };

  private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());

  private final DrawingPlacer mDrawingPlacer;
  private final HistoryStore mHistoryStore;
  private final ListeningExecutorService mThreadPool;
  private final WeakHashMap<View, ListenableFuture<Game>> mViewToGame;

  public HistoryAdapter(Context context, HistoryStore historyStore) {
    super(context, null, 0);
    mDrawingPlacer = new DrawingPlacer();
    mHistoryStore = historyStore;
    mViewToGame = new WeakHashMap<>();
    mThreadPool =  MoreExecutors
        .listeningDecorator(Executors
            .newFixedThreadPool(10));
  }

  public HistoryStore.Preview getPreview(int index) {
    Cursor cursor = getCursor();
    cursor.moveToPosition(index);
    return HistoryStore.cursorToPreview(cursor);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
    return View.inflate(context, R.layout.history_game, null);
  }

  @Override
  public void bindView(final View view, Context context, Cursor cursor) {
    HistoryStore.Preview preview = HistoryStore.cursorToPreview(cursor);

    if (preview == null) {
      view.setVisibility(View.GONE);
      return;
    }
    view.setVisibility(View.VISIBLE);

    TextView labelView = (TextView) view.findViewById(R.id.label);
    labelView.setText(preview.getPreviewText());

    hideGameViews(view);

    ListenableFuture<Game> previousGame = mViewToGame.get(view);
    if (previousGame != null) {
      previousGame.cancel(true);
    }
    ListenableFuture<Game> gameFuture = getGame(preview.getGameId());
    mViewToGame.put(view, gameFuture);

    Futures.addCallback(gameFuture, new FutureCallback<Game>() {
      @Override
      public void onSuccess(final Game game) {
        UI_HANDLER.post(new Runnable() {
          @Override
          public void run() {
            bindGameToView(game, view);
          }
        });
      }

      @Override
      public void onFailure(Throwable t) {
      }
    });
  }

  private ListenableFuture<Game> getGame(final long id) {
    return mThreadPool.submit(new Callable<Game>() {
      @Override
      public Game call() throws Exception {
        return mHistoryStore.getGame(id);
      }
    });
  }

  private void hideGameViews(View view) {
    for (int i = 0; i < DRAWING_FRAME_VIEW_IDS.length; i++) {
      view.findViewById(DRAWING_FRAME_VIEW_IDS[i]).setVisibility(View.GONE);
    }
  }

  private void bindGameToView(Game game, View view) {
    mViewToGame.remove(view);
    for (int i = 0; i < DRAWING_FRAME_VIEW_IDS.length; i++) {
      bindFrameAndDrawing(
          getDrawing(game, i),
          view.findViewById(DRAWING_FRAME_VIEW_IDS[i]),
          (ImageView) view.findViewById(DRAWING_VIEW_IDS[i]));
    }
  }

  @Nullable
  private Drawing getDrawing(Game game, int index) {
    int current = 0;
    for (Turn turn : game) {
      if (!turn.isDrawingTurn()) {
        continue;
      }

      if (index <= current) {
        return turn.getDrawing();
      }
      current++;
    }

    return null;
  }

  private void bindFrameAndDrawing(
      @Nullable Drawing drawing, View frame, ImageView drawingView) {
    if (drawing != null) {
      mDrawingPlacer.placeDrawingInView(drawing, drawingView);
      frame.setVisibility(View.VISIBLE);
    } else {
      frame.setVisibility(View.GONE);
    }
  }
}
