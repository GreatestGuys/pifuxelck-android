package com.everythingissauce.pifuxelck.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.everythingissauce.pifuxelck.ThreadUtil;
import com.everythingissauce.pifuxelck.data.Game;
import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.storage.HistoryStore;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

public class GameActivity extends Activity {

  /**
   * The intent extra that contains the game ID to display.
   */
  public static final String EXTRA_GAME = "game";

  private static final String TAG = "GameActivity";

  private ListView mTurnListView;
  private TurnAdapter mTurnAdapter;

  private HistoryStore mHistoryStore;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game);

    mHistoryStore = new HistoryStore(this);

    mTurnListView = (ListView) findViewById(R.id.turn_list_view);

    final long gameId = getIntent().getLongExtra(EXTRA_GAME, -1);
    ListenableFuture<Game> gameFuture = ThreadUtil.THREAD_POOL.submit(
        new Callable<Game>() {
          @Override
          public Game call() throws Exception {
            return mHistoryStore.getGame(gameId);
          }
        });

    ThreadUtil.callbackOnUi(gameFuture, new FutureCallback<Game>() {
      @Override
      public void onSuccess(Game game) {
        if (game != null) {
          mTurnAdapter = TurnAdapter.newTurnAdapter(GameActivity.this, game);
          mTurnListView.setAdapter(mTurnAdapter);
        }
      }

      @Override
      public void onFailure(Throwable t) {
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        super.onBackPressed();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
