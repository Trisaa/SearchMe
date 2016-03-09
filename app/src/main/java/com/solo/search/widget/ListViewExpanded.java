package com.solo.search.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ListViewExpanded extends ListView {

	public ListViewExpanded(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDividerHeight(0);
	}

	public ListViewExpanded(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ListViewExpanded(Context context) {
		super(context);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
				Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST));
	}
}
