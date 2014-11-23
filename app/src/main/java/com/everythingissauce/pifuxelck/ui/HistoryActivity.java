package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.data.Color;
import com.everythingissauce.pifuxelck.data.Drawing;
import com.everythingissauce.pifuxelck.data.Game;
import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.data.Turn;
import com.everythingissauce.pifuxelck.storage.HistoryStore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONException;

public class HistoryActivity extends Activity implements AdapterView.OnItemClickListener {

  private static final String TAG = "HistoryActivity";

  private ListView mGameListView;
  private GameAdapter mGameAdapter;

  private HistoryStore mHistoryStore;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_history);

    mHistoryStore = new HistoryStore(this);

    mGameAdapter = new GameAdapter(this, new Game[] {
        new Game.Builder(0)
            .addTurn(new Turn("Cosmo", "A game with 3 drawings"))
            .addTurn(new Turn("Zhenya", new Drawing.Builder()
                .setBackgroundColor(new Color(1, 0, 0))
                .build()))
            .addTurn(new Turn("Zhenya", "Penis Penis Penis"))
            .addTurn(new Turn("Jesse", new Drawing.Builder()
                .setBackgroundColor(new Color(0, 1, 0))
                .build()))
            .addTurn(new Turn("Zhenya", "Something offensive"))
            .addTurn(new Turn("Will", new Drawing.Builder()
                .setBackgroundColor(new Color(0, 0, 1))
                .build()))
            .build(),

        new Game.Builder(0)
            .addTurn(new Turn("Cosmo", "A game with just 2 drawings"))
            .addTurn(new Turn("Zhenya", new Drawing.Builder()
                .setBackgroundColor(new Color(1, 1, 0))
                .build()))
            .addTurn(new Turn("Cosmo", "Whatever"))
            .addTurn(new Turn("Jesse", new Drawing.Builder()
                .setBackgroundColor(new Color(0, 1, 1))
                .build()))
            .build(),

    });
    mGameListView = (ListView) findViewById(R.id.game_list_view);
    mGameListView.setAdapter(mGameAdapter);
    mGameListView.setOnItemClickListener(this);
  }

  @Override
  public void onItemClick(AdapterView<?> listView, View view, int i, long l) {
    Game game = mGameAdapter.getItem(i);

    try {
      Intent intent = new Intent();
      intent.setClass(getApplicationContext(), GameActivity.class);
      intent.putExtra(GameActivity.EXTRA_GAME, game.toJson().toString());
      Log.i(TAG, "Game: " + game.toString());
      Log.i(TAG, "Game JSON: " + game.toJson().toString());
      startActivity(intent);
    } catch (JSONException exception) {
      Log.e(TAG, "Could not marshal game to JSON", exception);
    }
  }
}