package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.ThreadUtil;
import com.everythingissauce.pifuxelck.api.Api;
import com.everythingissauce.pifuxelck.api.ApiProvider;
import com.everythingissauce.pifuxelck.data.Drawing;
import com.everythingissauce.pifuxelck.data.InboxEntry;
import com.everythingissauce.pifuxelck.data.Turn;
import com.everythingissauce.pifuxelck.drawing.DrawingPlacer;
import com.everythingissauce.pifuxelck.storage.IdentityProvider;
import com.everythingissauce.pifuxelck.storage.InboxStore;

import com.everythingissauce.pifuxelck.sync.SyncAdapter;
import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.Callable;

public class InboxActivity extends Activity implements
    AdapterView.OnItemClickListener,
    SwipeRefreshLayout.OnRefreshListener,
    View.OnClickListener, TextView.OnEditorActionListener {

  // Request codes used when launching Activities for results.
  private static final int REQUEST_DRAWING = 0;

  private final Api mApi = ApiProvider.getApi();
  private final DrawingPlacer mDrawingPlacer = new DrawingPlacer();

  private InboxAdapter mInboxAdapter;
  private SwipeRefreshLayout mEntryRefreshLayout;
  private ListView mEntryListView;

  private View mOverlayView;
  private EditText mLabelEditText;
  private ImageView mDrawingView;

  private ImageButton mNewActionButton;
  private ImageButton mDoneActionButton;

  // The ID of the game that corresponds to the current drawing that is being
  // labeled to the user.
  private long mGameId;

  private InboxStore mInboxStore;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_inbox);
    getActionBar().setTitle(R.string.title_activity_inbox);

    mEntryListView = (ListView) findViewById(R.id.entry_list);
    mEntryListView.setOnItemClickListener(this);

    mEntryRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
    mEntryRefreshLayout.setOnRefreshListener(this);
    mEntryRefreshLayout.setColorSchemeResources(R.color.accent);

    mOverlayView = findViewById(R.id.overlay_layout);
    mLabelEditText = (EditText) findViewById(R.id.label_edit_text);
    mDrawingView = (ImageView) findViewById(R.id.drawing_view);
    mLabelEditText.setOnEditorActionListener(this);

    mNewActionButton = (ImageButton) findViewById(R.id.new_action_button);
    mNewActionButton.setOnClickListener(this);

    mDoneActionButton = (ImageButton) findViewById(R.id.done_action_button);
    mDoneActionButton.setOnClickListener(this);

    mInboxStore = new InboxStore(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    refreshInboxAdapter();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu items for use in the action bar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_inbox, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent intent = new Intent();
    switch (item.getItemId()) {
      case R.id.action_history:
        intent.setClass(getApplicationContext(), HistoryActivity.class);
        startActivity(intent);
        return true;
      case R.id.action_contacts:
        intent.setClass(getApplicationContext(), ContactsActivity.class);
        startActivity(intent);
        return true;
      case R.id.action_settings:
        intent.setClass(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int index, long l) {
    InboxEntry entry = mInboxAdapter.getItem(index);
    Turn turn = entry.getPreviousTurn();
    mGameId = entry.getGameId();

    Turn reply = entry.getCurrentTurn();
    if (reply != null) {
      submitTurn(mGameId, reply);
      return;
    }

    if (turn.isLabelTurn()) {
      Intent drawingIntent = new Intent();
      drawingIntent.putExtra(DrawingActivity.EXTRAS_GAME_ID, mGameId);
      drawingIntent.putExtra(DrawingActivity.EXTRAS_LABEL, turn.getLabel());
      drawingIntent.setClass(getApplicationContext(), DrawingActivity.class);
      startActivityForResult(drawingIntent, REQUEST_DRAWING);
    } else if (turn.isDrawingTurn()) {
      showLabelOverlay(turn.getDrawing(), view);
    }
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.new_action_button:
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), NewGameActivity.class);
        startActivity(intent);
        return;

      case R.id.done_action_button:
        if (submitLabelTurn()) {
          hideLabelOverlay();
        }
        return;
    }
  }

  @Override
  public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
    if (actionId == EditorInfo.IME_ACTION_DONE) {
      if (submitLabelTurn()) {
        hideLabelOverlay();
      }
      return true;
    }
    return false;
  }

  @Override
  public void onRefresh() {
    refreshInbox();
  }

  @Override
  protected void onActivityResult(int request, int result, Intent data) {
    switch (request) {
      case REQUEST_DRAWING:
        onDrawingActivityResult(result, data);
        return;
    }
    refreshInbox();
  }

  @Override
  public void onBackPressed() {
    // Interpret the back button to mean close the current overlay.
    if (mOverlayView.getVisibility() == View.VISIBLE) {
      hideLabelOverlay();
    } else {
      // Prevent going back to the welcome page.
      // setResult(RESULT_CANCELED, new Intent());
      // super.onBackPressed();
    }
  }

  private void showLabelOverlay(Drawing drawing, View view) {
    mOverlayView.setVisibility(View.VISIBLE);
    mLabelEditText.setText("");

    // Use the bitmap from the label, so we don't have to wait for a new
    // bitmap to render.
    ImageView drawingView = (ImageView) view.findViewById(R.id.drawing_view);
    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawingView.getDrawable();
    mDrawingView.setImageBitmap(bitmapDrawable.getBitmap());
  }

  /**
   * Submit a turn by labelling the current drawing.
   * @return True if the drawing was successfully labeled, false other wise.
   */
  private boolean submitLabelTurn() {
    // Do not dismiss the overlay unless something has been typed into the box.
    if (mLabelEditText.getText().length() == 0) {
      return false;
    }

    submitTurn(mGameId, new Turn(null, mLabelEditText.getText().toString()));
    return true;
  }

  private void hideLabelOverlay() {
    mOverlayView.setVisibility(View.INVISIBLE);
    mDrawingPlacer.clearView(mDrawingView);

    // Make the keyboard go away if it is not already hidden.
    InputMethodManager imm =
        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(mLabelEditText.getWindowToken(), 0);
  }

  private void refreshInbox() {
    ThreadUtil.THREAD_POOL.submit(new Runnable() {
      @Override
      public void run() {
        ThreadUtil.callbackOnUi(
            SyncAdapter.syncInbox(
                new IdentityProvider(InboxActivity.this), mApi, mInboxStore),
            new FutureCallback<Integer>() {
              @Override
              public void onSuccess(Integer numNew) {
                refreshInboxAdapter();
              }

              @Override
              public void onFailure(Throwable t) {
                Toast.makeText(
                    InboxActivity.this,
                    R.string.error_inbox_refresh,
                    Toast.LENGTH_LONG).show();
                refreshInboxAdapter();
              }
            });
      }
    });
  }

  private void refreshInboxAdapter() {
    ThreadUtil.callbackOnUi(
        ThreadUtil.THREAD_POOL.submit(new Callable<List<InboxEntry>>() {
          @Override
          public List<InboxEntry> call() throws Exception {
            return mInboxStore.getEntries();
          }
        }),
        new FutureCallback<List<InboxEntry>>() {
          @Override
          public void onSuccess(List<InboxEntry> entries) {
            mInboxAdapter =
                InboxAdapter.newInboxAdapter(InboxActivity.this, entries);
            mEntryListView.setAdapter(mInboxAdapter);
            mEntryRefreshLayout.setRefreshing(false);
          }

          @Override
          public void onFailure(Throwable t) {
            mEntryRefreshLayout.setRefreshing(false);
          }
        });
  }

  private void onDrawingActivityResult(int result, Intent data) {
    if (result != RESULT_OK) {
      return;
    }

    final long gameId = data.getLongExtra(DrawingActivity.EXTRAS_GAME_ID, -1);
    if (gameId == -1) {
      return;
    }

    final Drawing drawing = Drawing.fromBundle(
        data.getBundleExtra(DrawingActivity.EXTRAS_DRAWING));
    submitTurn(gameId, new Turn(null, drawing));
  }

  private void submitTurn(final long gameId, final Turn turn) {
    // Always save the turn in the inbox regardless if it is a success or
    // failure.
    mInboxStore.updateEntryWithReply(gameId, turn);

    ListenableFuture<Void> moveFuture = mApi.move(gameId, turn);

    Futures.addCallback(moveFuture, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        ThreadUtil.UI_HANDLER.post(new Runnable() {
          @Override
          public void run() {
            refreshInbox();
          }
        });
      }

      @Override
      public void onFailure(Throwable t) {
        ThreadUtil.UI_HANDLER.post(new Runnable() {
          @Override
          public void run() {
            // Refresh so that "tap to retry" is displayed.
            refreshInboxAdapter();
            Toast.makeText(
                InboxActivity.this, R.string.error_submit_turn, Toast.LENGTH_LONG)
                .show();
          }
        });
      }
    });
  }
}
