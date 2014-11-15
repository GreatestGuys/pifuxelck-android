package com.everythingissauce.pifuxelck.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.everythingissauce.pifuxelck.Drawing;
import com.everythingissauce.pifuxelck.R;

public class DrawingActivity extends Activity implements View.OnClickListener {

  private static final String BUNDLE_KEY_DRAWING = "drawing";

  @Nullable Drawing.Builder mDrawingBuilder;
  @Nullable DrawingView mDrawingView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_drawing);

    mDrawingBuilder = savedInstanceState == null
        ? new Drawing.Builder()
        : new Drawing.Builder(
            Drawing.fromBundle(savedInstanceState.getBundle(BUNDLE_KEY_DRAWING)));

    mDrawingView = (DrawingView) findViewById(R.id.drawing_view);
    mDrawingView.setDrawing(mDrawingBuilder);
    DrawingOnTouchListener.install(mDrawingView, mDrawingBuilder);

    View undoButton = findViewById(R.id.undo_button);
    undoButton.setOnClickListener(this);
  }

  @Override
  protected void onSaveInstanceState (Bundle outState) {
    outState.putBundle("drawing", mDrawingBuilder.build().toBundle());
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.undo_button:
        mDrawingBuilder.popLine();
        mDrawingView.invalidate();
        break;
    }
  }
}
