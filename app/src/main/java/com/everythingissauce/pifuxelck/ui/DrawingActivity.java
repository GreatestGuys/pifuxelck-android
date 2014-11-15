package com.everythingissauce.pifuxelck.ui;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.everythingissauce.pifuxelck.Drawing;
import com.everythingissauce.pifuxelck.R;

public class DrawingActivity extends Activity implements
    View.OnClickListener, View.OnTouchListener {

  private static final String TAG = "DrawingActivity";

  private static final String BUNDLE_KEY_DRAWING = "drawing";

  // Constants used when the user is swiping over the option button that they
  // want to activate.
  private static final int OPTIONS_NONE = -1;
  private static final int OPTIONS_SIZE = 0;
  private static final int OPTIONS_COLOR = 1;
  private static final int OPTIONS_BACKGROUND = 2;

  @Nullable private Drawing.Builder mDrawingBuilder;
  @Nullable private DrawingView mDrawingView;

  @Nullable private View mOptionsButton;
  @Nullable private View mSizeButton;
  @Nullable private View mColorButton;
  @Nullable private View mBackgroundButton;

  private int mChosenOption = OPTIONS_NONE;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_drawing);

    mDrawingBuilder = savedInstanceState == null
        ? new Drawing.Builder()
        : new Drawing.Builder(Drawing.fromBundle(
              savedInstanceState.getBundle(BUNDLE_KEY_DRAWING)));

    mDrawingView = (DrawingView) findViewById(R.id.drawing_view);
    mDrawingView.setDrawing(mDrawingBuilder);
    DrawingOnTouchListener.install(mDrawingView, mDrawingBuilder);

    View undoButton = findViewById(R.id.undo_button);
    undoButton.setOnClickListener(this);

    mOptionsButton = findViewById(R.id.options_button);
    mOptionsButton.setOnTouchListener(this);

    mSizeButton = findViewById(R.id.size_button);
    mColorButton = findViewById(R.id.stroke_color_button);
    mBackgroundButton = findViewById(R.id.background_color_button);
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

  @Override
  public boolean onTouch(View view, MotionEvent event) {
    switch (view.getId()) {
      case R.id.options_button:
        return onTouchOptionsButton(event);
    }
    return false;
  }

  private boolean onTouchOptionsButton(MotionEvent event) {
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        mChosenOption = OPTIONS_NONE;
        showOptionButtons();
        return true;

      case MotionEvent.ACTION_UP:
        hideOptionButtons();
        if (mChosenOption == OPTIONS_NONE) {
          Log.i(TAG, "Chose no option");
        } else if (mChosenOption == OPTIONS_SIZE) {
          Log.i(TAG, "Chose size");
        } else if (mChosenOption == OPTIONS_COLOR) {
          Log.i(TAG, "Chose color");
        } else if (mChosenOption == OPTIONS_BACKGROUND) {
          Log.i(TAG, "Chose background");
        }
        return true;

      case MotionEvent.ACTION_MOVE:
        if (viewContainsEvent(mOptionsButton, mSizeButton, event)) {
          mChosenOption = OPTIONS_SIZE;
        } else if (viewContainsEvent(mOptionsButton, mColorButton, event)) {
          mChosenOption = OPTIONS_COLOR;
        } else if (viewContainsEvent(mOptionsButton, mBackgroundButton, event)) {
          mChosenOption = OPTIONS_BACKGROUND;
        } else {
          mChosenOption = OPTIONS_NONE;
        }
        return true;
    }
    return false;
  }

  private void showOptionButtons() {
    mSizeButton.setVisibility(View.VISIBLE);
    mColorButton.setVisibility(View.VISIBLE);
    mBackgroundButton.setVisibility(View.VISIBLE);
  }

  private void hideOptionButtons() {
    mSizeButton.setVisibility(View.INVISIBLE);
    mColorButton.setVisibility(View.INVISIBLE);
    mBackgroundButton.setVisibility(View.INVISIBLE);
  }

  private boolean viewContainsEvent(
      View touchedView, View targetView, MotionEvent event) {
    int[] touchedViewLocation = new int[2];
    touchedView.getLocationOnScreen(touchedViewLocation);

    int[] targetViewLocation = new int[2];
    targetView.getLocationOnScreen(targetViewLocation);

    int eventX = (int) event.getAxisValue(MotionEvent.AXIS_X)
        + touchedViewLocation[0];
    int eventY = (int) event.getAxisValue(MotionEvent.AXIS_Y)
        + touchedViewLocation[1];

    return targetViewLocation[0] <= eventX
        && eventX <= targetViewLocation[0] + targetView.getWidth()
        && targetViewLocation[1] <= eventY
        && eventY <= targetViewLocation[1] + targetView.getHeight();
  }
}
