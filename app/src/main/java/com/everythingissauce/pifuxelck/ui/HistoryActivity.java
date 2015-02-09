package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.api.Api;
import com.everythingissauce.pifuxelck.api.ApiProvider;
import com.everythingissauce.pifuxelck.data.Game;
import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.storage.HistoryStore;
import com.everythingissauce.pifuxelck.storage.IdentityProvider;
import com.everythingissauce.pifuxelck.sync.SyncAdapter;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.List;

public class HistoryActivity extends Activity implements
    AdapterView.OnItemClickListener,
    LoaderManager.LoaderCallbacks<Cursor>,
    SwipeRefreshLayout.OnRefreshListener {

  private static final String TAG = "HistoryActivity";

  private final Api mApi = ApiProvider.getApi();

  private ListView mGameListView;
  private HistoryAdapter mGameAdapter;
  private SwipeRefreshLayout mRefreshLayout;

  private HistoryStore mHistoryStore;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_history);

    mHistoryStore = new HistoryStore(this);

    mRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
    mRefreshLayout.setOnRefreshListener(this);
    mRefreshLayout.setColorSchemeResources(R.color.accent);

    mGameAdapter = new HistoryAdapter(this);
    mGameListView = (ListView) findViewById(R.id.game_list_view);
    mGameListView.setAdapter(mGameAdapter);
    mGameListView.setOnItemClickListener(this);

    // Initialize the query for historic games.
    refreshHistoryFromNetwork();
  }

  @Override
  public void onItemClick(AdapterView<?> listView, View view, int i, long l) {
    Intent intent = new Intent();
    intent.setClass(getApplicationContext(), GameActivity.class);
    intent.putExtra(GameActivity.EXTRA_GAME, mGameAdapter.getGame(i).getId());
    startActivity(intent);
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

  @Override
  public void onRefresh() {
    refreshHistoryFromNetwork();
  }

  private void refreshHistoryFromNetwork() {
    SyncAdapter.syncHistory(
        new IdentityProvider(this),
        mApi,
        mHistoryStore,
        new Api.Callback<Integer> () {
      @Override
      public void onApiSuccess(Integer numNew) {
        refreshHistoryInUI();
      }

      @Override
      public void onApiFailure() {
        Toast.makeText(
            HistoryActivity.this,
            R.string.error_history_refresh,
            Toast.LENGTH_LONG).show();
        refreshHistoryInUI();
      }
    });
  }

  private void refreshHistoryInUI() {
    getLoaderManager().initLoader(0, null, this).forceLoad();
    mRefreshLayout.setRefreshing(false);
  }
}