package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.data.Game;
import com.everythingissauce.pifuxelck.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.everythingissauce.pifuxelck.data.Turn;
import com.everythingissauce.pifuxelck.drawing.DrawingPlacer;

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

  private final DrawingPlacer mDrawingPlacer;

  private int mLastPosition = -1;

  private TurnAdapter(Context context, Turn[] turns) {
    super(context, 0, turns);
    mDrawingPlacer = new DrawingPlacer();
  }

  private void animateView(View view, int position) {
    view.clearAnimation();
    view.startAnimation(
        AnimationUtils.loadAnimation(
            view.getContext(),
            (position > mLastPosition)
                ? R.anim.up_from_bottom
                : R.anim.down_from_top));
    mLastPosition = position;
  }

  @Override
  public int getViewTypeCount() {
    return 2;
  }

  @Override
  public int getItemViewType(int position) {
    return getItem(position).isDrawingTurn() ? 0 : 1;
  }

  @Override
  public View getView(int i, View containerView, ViewGroup viewGroup) {
    Turn turn = getItem(i);

    View view = null;
    if (turn.isLabelTurn()) {
      view = getViewForLabel(turn, containerView, viewGroup);
    } else if (turn.isDrawingTurn()) {
      view = getViewForDrawing(turn, containerView, viewGroup);
    }

    mLastPosition = AnimationUtil.animateListView(view, mLastPosition, i);
    return view;
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
    ImageView drawingView;
    TextView playerView;

    // Create a new container if there isn't one that can be recycled, or if the
    // one that we were given to use is actually a label container.
    if (container == null || (drawingView = findDrawing(container)) == null) {
      container = View.inflate(getContext(), R.layout.game_drawing, null);
      drawingView = (ImageView) container.findViewById(R.id.drawing_view);
    }
    playerView = (TextView) container.findViewById(R.id.player);

    mDrawingPlacer.placeDrawingInView(turn.getDrawing(), drawingView);
    playerView.setText(turn.getPlayerId());
    return container;
  }

  private ImageView findDrawing(View container) {
    return (ImageView) container.findViewById(R.id.drawing_view);
  }
}