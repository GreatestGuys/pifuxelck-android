package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.Drawing;
import com.everythingissauce.pifuxelck.InboxEntry;
import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.Turn;
import com.github.pavlospt.CircleView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class InboxActivity extends Activity implements
    AdapterView.OnItemClickListener,
    SwipeRefreshLayout.OnRefreshListener,
    View.OnClickListener, TextView.OnEditorActionListener {

  // Request codes used when launching Activities for results.
  private static final int REQUEST_DRAWING = 0;

  private InboxAdapter mInboxAdapter;
  private SwipeRefreshLayout mEntryRefreshLayout;
  private ListView mEntryListView;

  private View mOverlayView;
  private EditText mLabelEditText;
  private DrawingView mDrawingView;

  private CircleView mNewActionButton;
  private CircleView mDoneActionButton;

  // TODO(will): Used to store made up turns. This will be removed once a
  // proper data store and networking has been implemented.
  private final List<InboxEntry> mEntries = new ArrayList<InboxEntry>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_inbox);
    getActionBar().setTitle(R.string.title_activity_inbox);

    addEntry(new Turn(null, "A banana"));
    addEntry(new Turn(null, "The Death Star!"));
    addEntry(new Turn(null, "Luke Skywalker eating froyo"));

    mEntryListView = (ListView) findViewById(R.id.entry_list);
    mEntryListView.setOnItemClickListener(this);

    mEntryRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
    mEntryRefreshLayout.setOnRefreshListener(this);
    mEntryRefreshLayout.setColorSchemeResources(R.color.accent);

    mOverlayView = findViewById(R.id.overlay_layout);
    mLabelEditText = (EditText) findViewById(R.id.label_edit_text);
    mDrawingView = (DrawingView) findViewById(R.id.drawing_view);
    mLabelEditText.setOnEditorActionListener(this);

    mNewActionButton = (CircleView) findViewById(R.id.new_action_button);
    mNewActionButton.setOnClickListener(this);

    mDoneActionButton = (CircleView) findViewById(R.id.done_action_button);
    mDoneActionButton.setOnClickListener(this);

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
    } else if (turn.isDrawingTurn()) {
      showLabelOverlay(turn.getDrawing());
    }
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.new_action_button:
        return;

      case R.id.done_action_button:
        hideLabelOverlay();
        return;
    }
  }

  @Override
  public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
    if (actionId == EditorInfo.IME_ACTION_DONE) {
      hideLabelOverlay();
      return true;
    }
    return false;
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

  private void showLabelOverlay(Drawing drawing) {
    mOverlayView.setVisibility(View.VISIBLE);
    mDrawingView.setDrawing(drawing);
    mDrawingView.invalidate();
    mLabelEditText.setText("");
  }

  private void hideLabelOverlay() {
    // Do not dismiss the overlay unless something has been typed into the box.
    if (mLabelEditText.getText().length() == 0) {
      return;
    }

    mOverlayView.setVisibility(View.INVISIBLE);

    // Make the keyboard go away if it is not already hidden.
    InputMethodManager imm =
        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(mLabelEditText.getWindowToken(), 0);

    // TODO(will): For now also add the new label to the turns list.
    addEntry(new Turn(null, mLabelEditText.getText().toString()));
    refreshInbox();
  }

  private void refreshInbox() {
    mInboxAdapter = InboxAdapter.newInboxAdapter(this, mEntries);
    mEntryListView.setAdapter(mInboxAdapter);
  }

  private void onDrawingActivityResult(int result, Intent data) {
    if (result != RESULT_OK) {
      return;
    }

    // TODO(will): For now just add it as an inbox entry.
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