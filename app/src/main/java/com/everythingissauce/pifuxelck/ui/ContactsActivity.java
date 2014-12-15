package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.api.Api;
import com.everythingissauce.pifuxelck.api.ApiProvider;
import com.everythingissauce.pifuxelck.auth.Identity;
import com.everythingissauce.pifuxelck.data.Contact;
import com.everythingissauce.pifuxelck.storage.ContactsStore;
import com.everythingissauce.pifuxelck.storage.IdentityProvider;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

public class ContactsActivity extends Activity implements
    SearchView.OnQueryTextListener,
    LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

  private final Api mApi = ApiProvider.getApi();

  private Identity mIdentity;

  private ContactsStore mContactsStore;

  private ListView mContactsListView;
  private ContactsAdapter mContactsAdapter;
  private SearchView mSearchView;

  private ViewGroup mNewContactBox;
  private TextView mNewContactName;
  private Button mAddContactButton;

  // These mutable fields MUST only be access from the UI thread!
  private String mQuery;
  @Nullable private Long mResolvedUserId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contacts);

    mContactsStore = new ContactsStore(this);

    // Subclass the contacts adapter to add an on click listener for each
    // button that will remove the corresponding contact from the data store.
    mContactsAdapter = new ContactsAdapter(this, R.layout.contacts_contact) {
      @Override
      public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        final Contact contact = ContactsStore.cursorToContact(cursor);
        view.findViewById(R.id.remove_contact_button)
            .setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                removeContact(contact);
              }
            });
      }
    };

    mContactsListView = (ListView) findViewById(R.id.contacts_list_view);
    mContactsListView.setAdapter(mContactsAdapter);

    mNewContactBox = (ViewGroup) findViewById(R.id.new_contact_box);
    mNewContactName = (TextView) findViewById(R.id.new_contact_name);
    mAddContactButton = (Button) findViewById(R.id.add_contact_button);
    mAddContactButton.setOnClickListener(this);

    mIdentity = new IdentityProvider(this).getIdentity();

    // Initialize the query for all contacts.
    refreshContactsList();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_contacts, menu);

    mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
    mSearchView.setOnQueryTextListener(this);

    return true;
  }

  @Override
  public boolean onQueryTextSubmit(String s) {
    mQuery = s;
    mSearchView.clearFocus();
    refreshContactsList();
    return true;
  }

  @Override
  public boolean onQueryTextChange(String query) {
    mQuery = query;
    refreshContactsList();
    return true;
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.add_contact_button:
        addContact();
    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    return mContactsStore.getContactsLoader(mQuery);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    mContactsAdapter.swapCursor(cursor);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> cursorLoader) {
    mContactsAdapter.swapCursor(null);
  }

  private void refreshContactsList() {
    getLoaderManager().initLoader(0, null, this).forceLoad();
    updateNewContactBox();
  }

  private void updateNewContactBox() {
    mResolvedUserId = null;
    if (TextUtils.isEmpty(mQuery)) {
      mNewContactBox.setVisibility(View.GONE);
      return;
    }

    mNewContactName.setText(mQuery);
    mNewContactBox.setVisibility(View.VISIBLE);
    mAddContactButton.setVisibility(View.INVISIBLE);

    // Don't bother to query for the user's own display name.
    if (mQuery.equals(mIdentity.getDisplayName())) {
      return;
    }

    mApi.lookupUserId(mQuery, new Api.Callback<Long>() {
      @Override
      public void onApiSuccess(Long result) {
        mResolvedUserId = result;
        mAddContactButton.setVisibility(View.VISIBLE);
      }

      @Override
      public void onApiFailure() {
        mResolvedUserId = null;
      }
    });
  }

  private void addContact() {
    if (mResolvedUserId == null) {
      return;
    }

    mContactsStore.addContact(new Contact(mResolvedUserId, mQuery));
    mSearchView.setQuery("", false);
    mSearchView.clearFocus();
    refreshContactsList();
  }

  private void removeContact(Contact contact) {
    mContactsStore.removeContact(contact);
    refreshContactsList();
  }
}