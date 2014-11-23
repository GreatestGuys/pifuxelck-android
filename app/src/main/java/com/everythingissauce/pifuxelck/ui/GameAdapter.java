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

    Drawing drawing1 = getDrawing(game, 0);
    Drawing drawing2 = getDrawing(game, 1);
    Drawing drawing3 = getDrawing(game, 2);

    View frame1 = view.findViewById(R.id.drawing_1_frame);
    View frame2 = view.findViewById(R.id.drawing_2_frame);
    View frame3 = view.findViewById(R.id.drawing_3_frame);

    DrawingView view1 = (DrawingView) view.findViewById(R.id.drawing_1_view);
    DrawingView view2 = (DrawingView) view.findViewById(R.id.drawing_2_view);
    DrawingView view3 = (DrawingView) view.findViewById(R.id.drawing_3_view);

    TextView labelView = (TextView) view.findViewById(R.id.label);

    bindFrameAndDrawing(drawing1, frame1, view1);
    bindFrameAndDrawing(drawing2, frame2, view2);
    bindFrameAndDrawing(drawing3, frame3, view3);

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