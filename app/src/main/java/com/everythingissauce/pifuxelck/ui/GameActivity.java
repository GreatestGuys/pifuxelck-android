package com.everythingissauce.pifuxelck.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.everythingissauce.pifuxelck.data.Game;
import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.storage.HistoryStore;

import org.json.JSONException;
import org.json.JSONObject;

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

    Game game = mHistoryStore.getGame(getIntent().getLongExtra(EXTRA_GAME, -1));
    if (game == null) {
      return;
    }

    mTurnAdapter = TurnAdapter.newTurnAdapter(this, game);
    mTurnListView = (ListView) findViewById(R.id.turn_list_view);
    mTurnListView.setAdapter(mTurnAdapter);
  }
}
