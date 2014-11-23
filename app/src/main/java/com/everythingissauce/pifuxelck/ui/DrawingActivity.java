package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.Color;
import com.everythingissauce.pifuxelck.Drawing;
import com.everythingissauce.pifuxelck.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

public class DrawingActivity extends Activity implements
    View.OnClickListener, View.OnTouchListener {

  /**
   * The extras key that will contain the bundled drawing.
   */
  public static final String EXTRAS_DRAWING = "drawing";

  /**
   * The extras key that should contain the label that should be drawn..
   */
  public static final String EXTRAS_LABEL = "label";

  private static final String TAG = "DrawingActivity";

  // Constants used when the user is swiping over the option button that they
  // want to activate.
  private static final int OPTIONS_NONE = -1;
  private static final int OPTIONS_SIZE = 0;
  private static final int OPTIONS_COLOR = 1;
  private static final int OPTIONS_BACKGROUND = 2;

  private Drawing.Builder mDrawingBuilder;
  private DrawingView mDrawingView;
  private DrawingOnTouchListener mDrawingOnTouchListener;

  private View mOptionsButton;
  private View mSizeButton;
  private View mColorButton;
  private View mBackgroundButton;

  private GridView mColorPickerView;
  private ColorAdapter mColorPickerAdapter;

  private GridView mSizePickerView;
  private SizeAdapter mSizePickerAdapter;

  private int mChosenOption = OPTIONS_NONE;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_drawing);

    mDrawingBuilder = savedInstanceState == null
        ? new Drawing.Builder()
        : new Drawing.Builder(Drawing.fromBundle(
              savedInstanceState.getBundle(EXTRAS_DRAWING)));

    mDrawingView = (DrawingView) findViewById(R.id.drawing_view);
    mDrawingView.setDrawing(mDrawingBuilder);
    mDrawingOnTouchListener =
        DrawingOnTouchListener.install(mDrawingView, mDrawingBuilder);

    TextView labelView = (TextView) findViewById(R.id.label);
    String label = getIntent().getStringExtra(EXTRAS_LABEL);
    labelView.setText(label == null ? "" : label);

    View undoButton = findViewById(R.id.undo_button);
    undoButton.setOnClickListener(this);

    View doneButton = findViewById(R.id.done_button);
    doneButton.setOnClickListener(this);

    mOptionsButton = findViewById(R.id.options_button);
    mOptionsButton.setOnTouchListener(this);

    mSizeButton = findViewById(R.id.size_button);
    mColorButton = findViewById(R.id.stroke_color_button);
    mBackgroundButton = findViewById(R.id.background_color_button);

    mColorPickerAdapter = new ColorAdapter(this);
    mColorPickerView = (GridView) findViewById(R.id.color_picker);
    mColorPickerView.setAdapter(mColorPickerAdapter);
    mColorPickerView.setOnItemClickListener(mColorPickerAdapter);

    mSizePickerAdapter = new SizeAdapter(this);
    mSizePickerView = (GridView) findViewById(R.id.size_picker);
    mSizePickerView.setAdapter(mSizePickerAdapter);
    mSizePickerView.setOnItemClickListener(mSizePickerAdapter);
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    if (mDrawingBuilder != null) {
      outState.putBundle("drawing", mDrawingBuilder.build().toBundle());
    }
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.undo_button:
        mDrawingBuilder.popLine();
        mDrawingView.refreshCache();
        break;

      case R.id.done_button:
        Intent intent = new Intent();
        intent.putExtra(EXTRAS_DRAWING, mDrawingBuilder.build().toBundle());
        setResult(RESULT_OK, intent);
        finish();
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

  @Override
  public void onBackPressed() {
    // Interpret the back button to mean, close the current overlay if the
    // color picker, or the size picker is displayed.
    if (mColorPickerView.getVisibility() == View.VISIBLE) {
      mColorPickerView.setVisibility(View.INVISIBLE);
      mColorPickerAdapter.setOnColorSelectedListener(null);
    } else if (mSizePickerView.getVisibility() == View.VISIBLE) {
      mSizePickerView.setVisibility(View.INVISIBLE);
      mSizePickerAdapter.setOnSizeSelectedListener(null);
    } else {
      setResult(RESULT_CANCELED, new Intent());
      super.onBackPressed();
    }
  }

  private boolean onTouchOptionsButton(MotionEvent event) {
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        mChosenOption = OPTIONS_NONE;
        showOptionButtons();
        return true;

      case MotionEvent.ACTION_UP:
        hideOptionButtons();
        if (mChosenOption == OPTIONS_SIZE) {
          showStrokeSizes();
        } else if (mChosenOption == OPTIONS_COLOR) {
          showStrokeColors();
        } else if (mChosenOption == OPTIONS_BACKGROUND) {
          showBackgroundColors();
        }
        return true;

      case MotionEvent.ACTION_MOVE:
        if (viewContainsEvent(mOptionsButton, mSizeButton, event)) {
          mChosenOption = OPTIONS_SIZE;
        } else if (viewContainsEvent(mOptionsButton, mColorButton, event)) {
          mChosenOption = OPTIONS_COLOR;
        } else if (viewContainsEvent(
            mOptionsButton, mBackgroundButton, event)) {
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

  private void showBackgroundColors() {
    mColorPickerView.setVisibility(View.VISIBLE);
    mColorPickerAdapter.setOnColorSelectedListener(
        new ColorAdapter.OnColorSelectedListener() {
          @Override
          public void onColorSelected(Color color) {
            mColorPickerAdapter.setOnColorSelectedListener(null);
            mColorPickerView.setVisibility(View.INVISIBLE);
            mDrawingBuilder.setBackgroundColor(color);
            mDrawingView.refreshCache();
          }
        });
  }

  private void showStrokeColors() {
    mColorPickerView.setVisibility(View.VISIBLE);
    mColorPickerAdapter.setOnColorSelectedListener(
        new ColorAdapter.OnColorSelectedListener() {
          @Override
          public void onColorSelected(Color color) {
            mColorPickerAdapter.setOnColorSelectedListener(null);
            mColorPickerView.setVisibility(View.INVISIBLE);
            mDrawingOnTouchListener.setCurrentColor(color);
            mSizePickerAdapter.setColor(color.toAndroidColor());
            mDrawingView.refreshCache();
          }
        });
  }

  private void showStrokeSizes() {
    // Update the maximum size of a stroke to render, and reset the adapter
    // causing a re-layout in the size view picker.
    mSizePickerAdapter.setMaxStrokeWidth(mDrawingView.getWidth());
    mSizePickerView.setAdapter(mSizePickerAdapter);

    mSizePickerView.setVisibility(View.VISIBLE);
    mSizePickerAdapter.setOnSizeSelectedListener(
        new SizeAdapter.OnSizeSelectedListener() {
          @Override
          public void onSizeSelected(double size) {
            mSizePickerAdapter.setOnSizeSelectedListener(null);
            mSizePickerView.setVisibility(View.INVISIBLE);
            mDrawingOnTouchListener.setCurrentSize(size);
            mDrawingView.refreshCache();
          }
        });
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