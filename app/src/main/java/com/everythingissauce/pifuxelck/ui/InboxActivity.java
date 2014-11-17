package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.Drawing;
import com.everythingissauce.pifuxelck.InboxEntry;
import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.Turn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class InboxActivity extends Activity implements
    AdapterView.OnItemClickListener,
    SwipeRefreshLayout.OnRefreshListener {

  // Request codes used when launching Activities for results.
  private static final int REQUEST_DRAWING = 0;

  private InboxAdapter mInboxAdapter;
  private SwipeRefreshLayout mEntryRefreshLayout;
  private ListView mEntryListView;

  // TODO(will): Used to store made up turns. This will be removed once a
  // proper data store and networking has been implemented.
  private final List<InboxEntry> mEntries = new ArrayList<InboxEntry>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_inbox);

    addEntry(new Turn(null, "A banana"));
    addEntry(new Turn(null, "The Death Star!"));
    addEntry(new Turn(null, "Luke Skywalker eating froyo"));

    mEntryListView = (ListView) findViewById(R.id.entry_list);
    mEntryListView.setOnItemClickListener(this);

    mEntryRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
    mEntryRefreshLayout.setOnRefreshListener(this);
    mEntryRefreshLayout.setColorSchemeColors(R.color.accentLight);

    refreshInbox();
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int index, long l) {
    InboxEntry entry = mInboxAdapter.getItem(index);
    Turn turn = entry.getPreviousTurn();

    if (turn.isLabelTurn()) {
      Intent drawingIntent = new Intent(DrawingActivity.ACTION_DRAW);
      drawingIntent.putExtra(DrawingActivity.EXTRAS_LABEL, turn.getLabel());
      startActivityForResult(drawingIntent, REQUEST_DRAWING);
    }
  }

  @Override
  public void onRefresh() {
    mEntryRefreshLayout.setRefreshing(false);
  }

  @Override
  protected void onActivityResult(int request, int result, Intent data) {
    switch (request) {
      case REQUEST_DRAWING:
        onDrawingActivityResult(result, data);
        return;
    }
  }

  private void refreshInbox() {
    mInboxAdapter = InboxAdapter.newInboxAdapter(this, mEntries);
    mEntryListView.setAdapter(mInboxAdapter);
  }

  private void onDrawingActivityResult(int result, Intent data) {
    if (result != RESULT_OK) {
      return;
    }

    // For now just add it as an inbox entry.
    Drawing drawing = Drawing.fromBundle(
        data.getBundleExtra(DrawingActivity.EXTRAS_DRAWING));
    addEntry(new Turn(null, drawing));

    refreshInbox();
  }

  // TODO(will): Remove once a real entry data store has been made.
  private void addEntry(Turn turn) {
    mEntries.add(new InboxEntry(mEntries.size(), turn));
  }
}