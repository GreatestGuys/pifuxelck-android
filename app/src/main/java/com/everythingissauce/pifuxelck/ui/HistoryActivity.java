package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.Color;
import com.everythingissauce.pifuxelck.Drawing;
import com.everythingissauce.pifuxelck.Game;
import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.Turn;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class HistoryActivity extends Activity {

  private ListView mGameListView;
  private HistoryGameAdapter mHistoryGameAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_history);

    mHistoryGameAdapter = new HistoryGameAdapter(this, new Game[] {
        new Game.Builder(0)
            .addTurn(new Turn("Cosmo", "A game with 3 drawings"))
            .addTurn(new Turn("Zhenya", new Drawing.Builder()
                .setBackgroundColor(new Color(1, 0, 0))
                .build()))
            .addTurn(new Turn("Jesse", new Drawing.Builder()
                .setBackgroundColor(new Color(0, 1, 0))
                .build()))
            .addTurn(new Turn("Will", new Drawing.Builder()
                .setBackgroundColor(new Color(0, 0, 1))
                .build()))
            .build(),

        new Game.Builder(0)
            .addTurn(new Turn("Cosmo", "A game with just 2 drawings"))
            .addTurn(new Turn("Zhenya", new Drawing.Builder()
                .setBackgroundColor(new Color(1, 1, 0))
                .build()))
            .addTurn(new Turn("Jesse", new Drawing.Builder()
                .setBackgroundColor(new Color(0, 1, 1))
                .build()))
            .build(),

    });
    mGameListView = (ListView) findViewById(R.id.game_list_view);
    mGameListView.setAdapter(mHistoryGameAdapter);
  }
}