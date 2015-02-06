package com.everythingissauce.pifuxelck.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.everythingissauce.pifuxelck.R;
import com.everythingissauce.pifuxelck.data.Contact;
import com.everythingissauce.pifuxelck.storage.ContactsStore;

public class ContactsAdapter extends CursorAdapter {

  private final Context mContext;
  private final int mContactLayout;

  public ContactsAdapter(Context context, int layout) {
    super(context, null, 0);
    mContext = context;
    mContactLayout = layout;
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
    return View.inflate(mContext, mContactLayout, null);
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    Contact contact = ContactsStore.cursorToContact(cursor);
    TextView contactName = (TextView) view.findViewById(R.id.contact_name);
    contactName.setText(contact.getDisplayName());
  }

  public Contact getContact(int index) {
    Cursor cursor = getCursor();
    cursor.moveToPosition(index);
    return ContactsStore.cursorToContact(cursor);
  }
}