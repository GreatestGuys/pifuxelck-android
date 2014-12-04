package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.InboxEntry;
import com.everythingissauce.pifuxelck.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.everythingissauce.pifuxelck.Turn;

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

  private InboxAdapter(Context context, InboxEntry[] entries) {
    super(context, 0, entries);
  }

  @Override
  public View getView(int i, View containerView, ViewGroup viewGroup) {
    Turn turn = getItem(i).getPreviousTurn();

    if (turn.isLabelTurn()) {
      return getViewForLabel(turn, containerView, viewGroup);
    } else if (turn.isDrawingTurn()) {
      return getViewForDrawing(turn, containerView, viewGroup);
    }

    return null;
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
    DrawingView drawingView;

    // Create a new container if there isn't one that can be recycled, or if the
    // one that we were given to use is actually a label container.
    if (container == null || (drawingView = findDrawing(container)) == null) {
      container = View.inflate(getContext(), R.layout.inbox_drawing, null);
      drawingView = (DrawingView) container.findViewById(R.id.drawing_view);
    }

    drawingView.setDrawing(turn.getDrawing());
    drawingView.clearDrawingCache();
    drawingView.refreshDrawingCache();
    return container;
  }

  private DrawingView findDrawing(View container) {
    return (DrawingView) container.findViewById(R.id.drawing_view);
  }
}
