package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.Drawing;
import com.everythingissauce.pifuxelck.Game;
import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.Turn;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GameAdapter extends ArrayAdapter<Game> {

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

  public GameAdapter(Context context) {
    super(context, R.layout.history_game);
  }

  public GameAdapter(Context context, Game[] games) {
    super(context, R.layout.history_game, games);
  }

  @Override
  public View getView(int index, View view, ViewGroup parent) {
    if (view == null) {
      view = View.inflate(getContext(), R.layout.history_game, null);
    }

    Game game = getItem(index);

    for (int i = 0; i < DRAWING_FRAME_VIEW_IDS.length; i++) {
      bindFrameAndDrawing(
          getDrawing(game, i),
          view.findViewById(DRAWING_FRAME_VIEW_IDS[i]),
          (DrawingView) view.findViewById(DRAWING_VIEW_IDS[i]));
    }

    TextView labelView = (TextView) view.findViewById(R.id.label);

    labelView.setText(getFirstLabel(game));

    return view;
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
    frame.setVisibility(drawing == null ? View.GONE : View.VISIBLE);
  }
}
