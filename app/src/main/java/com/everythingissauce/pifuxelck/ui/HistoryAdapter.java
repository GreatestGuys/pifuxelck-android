package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.data.Drawing;
import com.everythingissauce.pifuxelck.data.Game;
import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.data.Turn;
import com.everythingissauce.pifuxelck.storage.HistoryStore;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.TextView;

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

  public HistoryAdapter(Context context) {
    super(context, null, 0);
  }

  public Game getGame(int index) {
    Cursor cursor = getCursor();
    cursor.moveToPosition(index);
    return HistoryStore.cursorToGame(cursor);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
    return View.inflate(context, R.layout.history_game, null);
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    Game game = HistoryStore.cursorToGame(cursor);

    if (game == null) {
      view.setVisibility(View.GONE);
      return;
    }
    view.setVisibility(View.VISIBLE);

    for (int i = 0; i < DRAWING_FRAME_VIEW_IDS.length; i++) {
      bindFrameAndDrawing(
          getDrawing(game, i),
          view.findViewById(DRAWING_FRAME_VIEW_IDS[i]),
          (DrawingView) view.findViewById(DRAWING_VIEW_IDS[i]));
    }

    TextView labelView = (TextView) view.findViewById(R.id.label);

    labelView.setText(getFirstLabel(game));
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

  private String getFirstLabel(Game game) {
    for (Turn turn : game) {
      if (turn.isLabelTurn()) {
        return turn.getLabel();
      }
    }
    return "";
  }

  private void bindFrameAndDrawing(
      @Nullable Drawing drawing, View frame, DrawingView drawingView) {
    drawingView.setDrawing(drawing);
    drawingView.clearDrawingCache();
    drawingView.refreshDrawingCache();
    drawingView.invalidate();
    frame.setVisibility(drawing == null ? View.GONE : View.VISIBLE);
  }
}
