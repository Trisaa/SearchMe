package com.solo.search.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.solo.search.util.ResourceUtil;

public class FasterProgressBar extends FrameLayout {

	private ProgressBar mProgressBar;
	private View mFlyStar;
	private ObjectAnimator mAnimator;

	public FasterProgressBar(Context context) {
		super(context);
		initialize();
	}

	public FasterProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	private void initialize() {
		LayoutInflater.from(getContext()).inflate(ResourceUtil.getLayoutId(getContext(), "ssearch_faster_progress_bar"), this);
		mProgressBar = (ProgressBar) findViewById(ResourceUtil.getId(getContext(), "progress_bar"));
		mFlyStar = findViewById(ResourceUtil.getId(getContext(), "fly_star"));
	}

	public void stopAnimation() {
		if (mAnimator != null) {
			mAnimator.cancel();
		}
	}

	public void setVisivility(int visibility) {
		super.setVisibility(visibility);
		if (visibility != View.VISIBLE) {
			stopAnimation();
		}

	}

	public ProgressBar getProgressBar() {
		return mProgressBar;
	}

	public final void startAnimation() {
		if (mAnimator == null) {
			float[] screenSize = new float[2];
			screenSize[0] = getResources().getDisplayMetrics().widthPixels / 5;
			screenSize[1] = getResources().getDisplayMetrics().widthPixels;

			mAnimator = ObjectAnimator.ofFloat(mFlyStar, "translationX", screenSize);
			mAnimator.setDuration(900L);
			mAnimator.setInterpolator(new AccelerateInterpolator());
			mAnimator.setRepeatMode(ValueAnimator.RESTART);
			mAnimator.setRepeatCount(ValueAnimator.INFINITE);
		}

		mAnimator.setStartDelay(100L);
		mAnimator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				mFlyStar.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}
		});
		mAnimator.start();
	}

}
