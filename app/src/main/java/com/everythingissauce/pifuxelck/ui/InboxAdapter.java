package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.data.InboxEntry;
import com.everythingissauce.pifuxelck.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.everythingissauce.pifuxelck.data.Turn;
import com.everythingissauce.pifuxelck.drawing.DrawingPlacer;

import java.util.Comparator;
import java.util.List;
import java.util.Arrays;

/**
 * A {@link ListAdapter} that will provide views that correspond to the entries
 * in the player's inbox.
 */
public class InboxAdapter extends ArrayAdapter<InboxEntry> {

  public static InboxAdapter newInboxAdapter(
      Context context, List<InboxEntry> entriesList) {
    InboxEntry[] entries = new InboxEntry[entriesList.size()];
    entriesList.toArray(entries);

    // Sort the entries in descending order of their game id.
    Arrays.sort(entries, new Comparator<InboxEntry>() {
      @Override
      public int compare(InboxEntry first, InboxEntry second) {
        long firstId = first.getGameId();
        long secondId = second.getGameId();
        return firstId == secondId ? 0 : firstId < secondId ? 1 : -1;
      }
    });

    return new InboxAdapter(context, entries);
  }

  private final DrawingPlacer mDrawingPlacer;

  private int mLastPosition = -1;

  private InboxAdapter(Context context, InboxEntry[] entries) {
    super(context, 0, entries);
    mDrawingPlacer = new DrawingPlacer();
  }

  @Override
  public int getViewTypeCount() {
    return 2;
  }

  @Override
  public int getItemViewType(int position) {
    return getItem(position).getPreviousTurn().isDrawingTurn() ? 0 : 1;
  }

  @Override
  public View getView(int i, View containerView, ViewGroup viewGroup) {
    InboxEntry entry = getItem(i);
    Turn turn = entry.getPreviousTurn();

    View view = null;
    if (turn.isLabelTurn()) {
      view = getViewForLabel(turn, containerView, viewGroup);
    } else if (turn.isDrawingTurn()) {
      view = getViewForDrawing(turn, containerView, viewGroup);
    }

    if (view != null) {
      View submitTurn = view.findViewById(R.id.submit_saved_turn);
      submitTurn.setVisibility(
          entry.getCurrentTurn() == null ? View.GONE : View.VISIBLE);
    }

    mLastPosition = AnimationUtil.animateListView(view, mLastPosition, i);

    return view;
  }

  private View getViewForLabel(Turn turn, View container, ViewGroup group) {
    TextView labelView;

    // Create a new container if there isn't one that can be recycled, or if the
    // one that we were given to use is actually a drawing container.
    if (container == null || (labelView = findLabel(container)) == null) {
      container = View.inflate(getContext(), R.layout.inbox_label, null);
      labelView = (TextView) container.findViewById(R.id.label);
    }

    labelView.setText(turn.getLabel());
    return container;
  }

  private TextView findLabel(View container) {
    return (TextView) container.findViewById(R.id.label);
  }

  private View getViewForDrawing(Turn turn, View container, ViewGroup group) {
    ImageView drawingView;

    // Create a new container if there isn't one that can be recycled, or if the
    // one that we were given to use is actually a label container.
    if (container == null || (drawingView = findDrawing(container)) == null) {
      container = View.inflate(getContext(), R.layout.inbox_drawing, null);
      drawingView = (ImageView) container.findViewById(R.id.drawing_view);
    }

    mDrawingPlacer.placeDrawingInView(turn.getDrawing(), drawingView);
    return container;
  }

  private ImageView findDrawing(View container) {
    return (ImageView) container.findViewById(R.id.drawing_view);
  }
}
