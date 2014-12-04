package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.Game;
import com.everythingissauce.pifuxelck.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.everythingissauce.pifuxelck.Turn;

/**
 * A {@link ListAdapter} that will provide views that correspond to the turns
 * of a given game.
 */
public class TurnAdapter extends ArrayAdapter<Turn> {

  public static TurnAdapter newTurnAdapter(Context context, Game game) {
    Turn[] turns = new Turn[game.getNumberOfTurns()];
    int index = 0;
    for (Turn turn : game) {
      turns[index++] = turn;
    }
    return new TurnAdapter(context, turns);
  }

  private TurnAdapter(Context context, Turn[] turns) {
    super(context, 0, turns);
  }

  @Override
  public View getView(int i, View containerView, ViewGroup viewGroup) {
    Turn turn= getItem(i);

    if (turn.isLabelTurn()) {
      return getViewForLabel(turn, containerView, viewGroup);
    } else if (turn.isDrawingTurn()) {
      return getViewForDrawing(turn, containerView, viewGroup);
    }

    return null;
  }

  private View getViewForLabel(Turn turn, View container, ViewGroup group) {
    TextView labelView;
    TextView playerView;

    // Create a new container if there isn't one that can be recycled, or if the
    // one that we were given to use is actually a drawing container.
    if (container == null || (labelView = findLabel(container)) == null) {
      container = View.inflate(getContext(), R.layout.game_label, null);
      labelView = (TextView) container.findViewById(R.id.label);
    }
    playerView = (TextView) container.findViewById(R.id.player);

    labelView.setText(turn.getLabel());
    playerView.setText(turn.getPlayerId());
    return container;
  }

  private TextView findLabel(View container) {
    return (TextView) container.findViewById(R.id.label);
  }

  private View getViewForDrawing(Turn turn, View container, ViewGroup group) {
    DrawingView drawingView;
    TextView playerView;

    // Create a new container if there isn't one that can be recycled, or if the
    // one that we were given to use is actually a label container.
    if (container == null || (drawingView = findDrawing(container)) == null) {
      container = View.inflate(getContext(), R.layout.game_drawing, null);
      drawingView = (DrawingView) container.findViewById(R.id.drawing_view);
    }
    playerView = (TextView) container.findViewById(R.id.player);

    drawingView.setDrawing(turn.getDrawing());
    drawingView.clearDrawingCache();
    drawingView.refreshDrawingCache();
    playerView.setText(turn.getPlayerId());
    return container;
  }

  private DrawingView findDrawing(View container) {
    return (DrawingView) container.findViewById(R.id.drawing_view);
  }
}