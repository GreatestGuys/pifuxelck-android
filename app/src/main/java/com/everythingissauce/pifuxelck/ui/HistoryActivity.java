package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.data.Color;
import com.everythingissauce.pifuxelck.data.Drawing;
import com.everythingissauce.pifuxelck.data.Game;
import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.data.Turn;
import com.everythingissauce.pifuxelck.storage.HistoryStore;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONException;

public class HistoryActivity extends Activity implements
    AdapterView.OnItemClickListener,
    LoaderManager.LoaderCallbacks<Cursor> {

  private static final String TAG = "HistoryActivity";

  private ListView mGameListView;
  private HistoryAdapter mGameAdapter;

  private HistoryStore mHistoryStore;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_history);

    mHistoryStore = new HistoryStore(this);

    mGameAdapter = new HistoryAdapter(this);
    mGameListView = (ListView) findViewById(R.id.game_list_view);
    mGameListView.setAdapter(mGameAdapter);
    mGameListView.setOnItemClickListener(this);

    // Initialize the query for historic games.
    getLoaderManager().initLoader(0, null, this).forceLoad();
  }

  @Override
  public void onItemClick(AdapterView<?> listView, View view, int i, long l) {
    Game game = mGameAdapter.getGame(i);

    try {
      Intent intent = new Intent();
      intent.setClass(getApplicationContext(), GameActivity.class);
      intent.putExtra(GameActivity.EXTRA_GAME, game.toJson().toString());
      startActivity(intent);
    } catch (JSONException exception) {
      Log.e(TAG, "Could not marshal game to JSON", exception);
    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    return mHistoryStore.getHistoryLoader();
  }

  @Override
  public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    mGameAdapter.swapCursor(cursor);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> cursorLoader) {
    mGameAdapter.swapCursor(null);
  }
}