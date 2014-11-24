package com.everythingissauce.pifuxelck.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.everythingissauce.pifuxelck.R;

public class ContactsAdapter extends ArrayAdapter<String> {

  public ContactsAdapter(Context context) {
    super(context, R.layout.new_game_contact, new String[] {
            "Cosmo", "Graham", "Jesse", "Zhenya"
        });
  }

  @Override
  public View getView(int index, View container, ViewGroup parent) {
    if (container == null) {
      container = View.inflate(getContext(), R.layout.new_game_contact, null);
    }

    TextView contactName = (TextView) container.findViewById(R.id.contact_name);
    contactName.setText(getItem(index));

    return container;
  }
}
