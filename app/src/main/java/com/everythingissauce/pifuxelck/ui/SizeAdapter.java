package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.Color;
import com.everythingissauce.pifuxelck.R;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

import com.github.pavlospt.CircleView;

/**
 * A {@link ListAdapter} that will provide views that correspond to choices in a
 * color palette spanning the usable spectrum.
 */
public class SizeAdapter extends ArrayAdapter<Double> implements
    AdapterView.OnItemClickListener {

  private static final Double[] SIZES = {
    0.025, 0.05, 0.1, 0.15, 0.2,  0.25, 0.3, 0.35, 0.4, 0.6
  };

  /**
   * A listener that will be notified when one of the size options has been
   * selected by the user.
   */
  public static interface OnSizeSelectedListener {
    void onSizeSelected(double size);
  }

  private int mColor;
  private int mFullWidth;
  private @Nullable OnSizeSelectedListener mOnSizeSelectedListener;

  public SizeAdapter(Context context) {
    super(context, R.layout.size_swatch, SIZES);
    mFullWidth = 0;
    mColor = android.graphics.Color.BLACK;
  }

  public void setMaxStrokeWidth(int maxWidth) {
    mFullWidth = maxWidth;
  }

  public void setColor(int color) {
    mColor = color;
  }

  @Override
  public View getView(final int i, View containerView, ViewGroup viewGroup) {
    if (containerView == null) {
      containerView = View.inflate(getContext(), R.layout.size_swatch, null);
    }

    CircleView circleView =
        (CircleView) containerView.findViewById(R.id.size_button);

    circleView.setFillColor(mColor);
    circleView.setStrokeColor(mColor);

    // Resize the view based on the current stroke width and the width of the
    // drawing view.
    ViewGroup.LayoutParams layoutParams = circleView.getLayoutParams();
    layoutParams.height = (int) (mFullWidth * SIZES[i]);
    layoutParams.width = (int) (mFullWidth * SIZES[i]);
    circleView.setLayoutParams(layoutParams);


    return containerView;
  }

  @Override
  public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    if (mOnSizeSelectedListener!= null) {
      mOnSizeSelectedListener.onSizeSelected(SIZES[i]);
    }
  }

  public void setOnSizeSelectedListener(
      @Nullable OnSizeSelectedListener listener) {
    mOnSizeSelectedListener = listener;
  }
}

