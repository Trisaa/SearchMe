package com.solo.search.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class HomePageScrollView extends ScrollView {

	private boolean mPauseFlag = false;
	private ScrollViewListener mScrollViewListener;

	public HomePageScrollView(Context paramContext) {
		super(paramContext);
	}

	public HomePageScrollView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
	}

	public HomePageScrollView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mScrollViewListener != null) {
			mScrollViewListener.onInterceptTouchEvent(ev);
		}
		if (mPauseFlag) {
			return false;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mScrollViewListener != null) {
			mScrollViewListener.onScrollTouchEvent(ev);
		}
		return super.onTouchEvent(ev);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (mScrollViewListener != null) {
			mScrollViewListener.onScrollChanged(this, l, t, oldl, oldt);
		}
	}

	public void setPauseTouchEvent(boolean pauseFlag) {
		mPauseFlag = pauseFlag;
	}

	public void setScrollViewListener(ScrollViewListener paramScrollViewListener) {
		mScrollViewListener = paramScrollViewListener;
	}

	public static abstract interface ScrollViewListener {

		public abstract void onInterceptTouchEvent(MotionEvent ev);

		public abstract void onScrollChanged(HomePageScrollView scrollView, int l, int t, int oldl, int oldt);

		public abstract void onScrollTouchEvent(MotionEvent ev);
	}
}