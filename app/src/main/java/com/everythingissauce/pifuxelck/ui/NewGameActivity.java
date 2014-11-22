package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.github.pavlospt.CircleView;

public class NewGameActivity extends Activity implements
    View.OnClickListener,
    AdapterView.OnItemClickListener {

  private static final String TAG = "NewGameActivity";

  private CircleView mActionButton;
  private ListView mContactsListView;
  private EditText mLabelEditText;

  private ArrayAdapter<String> mContactsAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_game);

    mLabelEditText = (EditText) findViewById(R.id.label_edit_text);

    mActionButton = (CircleView) findViewById(R.id.done_action_button);
    mActionButton.setOnClickListener(this);

    mContactsAdapter = new ContactsAdapter(this);
    mContactsListView= (ListView) findViewById(R.id.contacts_list_view);
    mContactsListView.setAdapter(mContactsAdapter);
    mContactsListView.setOnItemClickListener(this);
  }

  @Override
  public void onClick(View view) {
  }

  @Override
  public void onItemClick(AdapterView<?> adapter, View view, int i, long l) {
    View checkBoxView = view.findViewById(R.id.checkbox);
    checkBoxView.setSelected(!checkBoxView.isSelected());
  }
}