package com.everythingissauce.pifuxelck.ui;

import com.everythingissauce.pifuxelck.Color;
import com.everythingissauce.pifuxelck.R;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.github.pavlospt.CircleView;

/**
 * A {@link ListAdapter} that will provide views that correspond to all of
 * possible color options.
 */
public class ColorAdapter extends ArrayAdapter<Color> implements
    AdapterView.OnItemClickListener {

  /**
   * A list of predefined colors that form a decent color palette. These values
   * obtained from:
   * http://www.oracle.com/webfolder/ux/middleware/richclient/index.html?/webfolder/ux/middleware/richclient/guidelines5/inputColor.html
   */
  private static final Color[] PALETTE = {
    // Black, greys, and white:
    newColor(0, 0, 0), newColor(190, 190, 190), newColor(167, 167, 167),
    newColor(220, 220, 220), newColor(248, 248, 255), newColor(255, 255, 255),

    // Blues:
    newColor(25, 25, 112), newColor(0, 0, 255), newColor(64, 105, 255),
    newColor(0, 191, 255), newColor(135, 206, 235), newColor(173, 216, 230),

    // Greens:
    newColor(0, 100, 0), newColor(107, 142, 35), newColor(0, 128, 0),
    newColor(46, 139, 87), newColor(60, 179, 113), newColor(0, 255, 127),

    // Yellows:
    newColor(184, 134, 11), newColor(255, 140, 0), newColor(255, 165, 0),
    newColor(255, 255, 0), newColor(238, 232, 170), newColor(250, 250, 210),

    // Reds:
    newColor(178, 34, 34), newColor(165, 42, 42), newColor(205, 92, 92),
    newColor(250, 128, 114), newColor(255, 160, 122), newColor(255, 228, 181),

    // Oranges:
    newColor(128, 0, 0), newColor(210, 105, 30), newColor(255, 0, 0),
    newColor(255, 99, 71), newColor(244, 164, 96), newColor(245, 222, 179),

    // Purples:
    newColor(139, 0, 139), newColor(208, 32, 144), newColor(255, 20, 147),
    newColor(255, 0, 255), newColor(219, 112, 147), newColor(255, 182, 193)
  };

  // Constructs a color object with color values given in the range 0 to 255.
  private static Color newColor(int red, int green, int blue) {
    return new Color(red / 255.0, green / 255.0, blue / 255.0);
  }

  /**
   * A listener that will be notified when one of the color swatches has been
   * selected by the user.
   */
  public static interface OnColorSelectedListener {
    void onColorSelected(Color color);
  }

  private @Nullable OnColorSelectedListener mOnColorSelectedListener;

  public ColorAdapter(Context context) {
    super(context, R.layout.color_swatch, PALETTE);
  }

  @Override
  public View getView(final int i, View containerView, ViewGroup viewGroup) {
    if (containerView == null) {
      containerView = View.inflate(getContext(), R.layout.color_swatch, null);
    }

    CircleView circleView =
        (CircleView) containerView.findViewById(R.id.color_button);
    circleView.setFillColor(PALETTE[i % PALETTE.length].toAndroidColor());

    return containerView;
  }

  @Override
  public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    if (mOnColorSelectedListener != null) {
      mOnColorSelectedListener.onColorSelected(PALETTE[i % PALETTE.length]);
    }
  }

  public void setOnColorSelectedListener(
      @Nullable OnColorSelectedListener listener) {
    mOnColorSelectedListener = listener;
  }

  @Override
  public int getCount() {
    return Integer.MAX_VALUE;
  }

  @Override
  public Color getItem(int position) {
    return super.getItem(position % PALETTE.length);
  }
}
