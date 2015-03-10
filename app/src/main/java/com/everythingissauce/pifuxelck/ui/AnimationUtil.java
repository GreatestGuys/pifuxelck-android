package com.everythingissauce.pifuxelck.ui;

import android.view.View;
import android.view.animation.AnimationUtils;

import com.everythingissauce.pifuxelck.R;

class AnimationUtil {

  public static int animateListView(
      final View view,
      int lastPosition,
      int position) {
    final int animation = position > lastPosition
        ? R.anim.up_from_bottom
        : R.anim.down_from_top;

    // It is necessary to make the view invisible since it is possible that
    // it will be displayed on screen briefly before the animation is set.
    view.setVisibility(View.INVISIBLE);
    view.post(new Runnable() {
      @Override
      public void run() {
        animateView(view, animation);
      }
    });

    return position;
  }

  private static void animateView(View view, int animation) {
    view.setVisibility(View.VISIBLE);
    view.clearAnimation();
    view.startAnimation(
        AnimationUtils.loadAnimation(view.getContext(), animation));
  }

  private AnimationUtil() {}
}
