package com.everythingissauce.pifuxelck.ui;

import android.view.View;
import android.view.animation.Animation;
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

  public static void animateView(View view, int animation) {
    view.setVisibility(View.VISIBLE);
    view.clearAnimation();
    view.startAnimation(
        AnimationUtils.loadAnimation(view.getContext(), animation));
  }

  public static void hideOnAnimationFinish(final View view) {
    Animation animation = view.getAnimation();
    if (animation == null) {
      return;
    }

    hideOnAnimationFinish(view, animation);
  }

  public static void hideOnAnimationFinish(
      final View view,  Animation animation) {
    animation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        view.setVisibility(View.INVISIBLE);
        animation.setAnimationListener(null);
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
      }
    });
  }

  private AnimationUtil() {}
}
