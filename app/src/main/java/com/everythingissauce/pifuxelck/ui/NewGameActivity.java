package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.R;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.everythingissauce.pifuxelck.api.Api;
import com.everythingissauce.pifuxelck.api.ApiProvider;
import com.everythingissauce.pifuxelck.data.Contact;
import com.everythingissauce.pifuxelck.data.InboxEntry;
import com.everythingissauce.pifuxelck.data.Turn;
import com.everythingissauce.pifuxelck.storage.ContactsStore;
import com.everythingissauce.pifuxelck.storage.InboxStore;
import com.github.pavlospt.CircleView;

import java.util.ArrayList;
import java.util.List;

public class NewGameActivity extends Activity implements
    View.OnClickListener,
    AdapterView.OnItemClickListener,
    LoaderManager.LoaderCallbacks<Cursor> {

  private static final String TAG = "NewGameActivity";

  private final Api mApi = ApiProvider.getApi();
  private final List<Long> mPlayers = new ArrayList<Long>();

  private CircleView mActionButton;
  private ListView mContactsListView;
  private EditText mLabelEditText;

  private ContactsAdapter mContactsAdapter;

  private ContactsStore mContactsStore;
  private InboxStore mInboxStore;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_game);

    mLabelEditText = (EditText) findViewById(R.id.label_edit_text);

    mActionButton = (CircleView) findViewById(R.id.done_action_button);
    mActionButton.setOnClickListener(this);

    mContactsAdapter = new ContactsAdapter(this, R.layout.new_game_contact);
    mContactsListView = (ListView) findViewById(R.id.contacts_list_view);
    mContactsListView.setAdapter(mContactsAdapter);
    mContactsListView.setOnItemClickListener(this);

    mContactsStore = new ContactsStore(this);
    mInboxStore = new InboxStore(this);

    // Kick off loading of contacts list.
    getLoaderManager().initLoader(0, null, this).forceLoad();
  }

  @Override
  public void onClick(View view) {
    String label = mLabelEditText.getText().toString();
    if (TextUtils.isEmpty(label)) {
      return;
    }

    mApi.newGame(label, mPlayers, new Api.Callback<Void>() {
      @Override
      public void onApiSuccess(Void result) {
        finish();
      }

      @Override
      public void onApiFailure() {
        Toast.makeText(
            NewGameActivity.this,
            R.string.error_new_game,
            Toast.LENGTH_LONG).show();
      }
    });
  }

  @Override
  public void onItemClick(AdapterView<?> adapter, View view, int i, long l) {
    View checkBoxView = view.findViewById(R.id.checkbox);
    boolean isSelected = !checkBoxView.isSelected();
    checkBoxView.setSelected(isSelected);

    Contact contact = mContactsAdapter.getContact(i);
    if (isSelected) {
      mPlayers.add(contact.getUserId());
    } else {
      mPlayers.remove(contact.getUserId());
    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    return mContactsStore.getContactsLoader(null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    mContactsAdapter.swapCursor(cursor);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mContactsAdapter.swapCursor(null);
  }
}
