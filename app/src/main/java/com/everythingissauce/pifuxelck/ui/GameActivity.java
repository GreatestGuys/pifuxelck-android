package com.everythingissauce.pifuxelck.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.everythingissauce.pifuxelck.Game;
import com.everythingissauce.pifuxelck.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GameActivity extends Activity {

  private static final String TAG = "GameActivity";

  /**
   * The intent extra that contains the game to display.
   * <p>
   * TODO(will): Right now this is a JSON serialized copy of the game. This
   * should be changed to be the game ID once local storage has been
   * implemented.
   */
  public static final String EXTRA_GAME = "game";

  private ListView mTurnListView;
  private TurnAdapter mTurnAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game);

    Game game = null;
    try {
      game = Game.fromJson(
          new JSONObject(getIntent().getStringExtra(EXTRA_GAME)));
    } catch (JSONException exception) {
      Log.e(TAG, "Could not deserialize.", exception);
    }

    if (game == null) {
      return;
    }

    mTurnAdapter = TurnAdapter.newTurnAdapter(this, game);
    mTurnListView = (ListView) findViewById(R.id.turn_list_view);
    mTurnListView.setAdapter(mTurnAdapter);
  }
}
