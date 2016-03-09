package com.solo.search.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.solo.search.util.ResourceUtil;
import com.yahoo.mobile.client.share.search.interfaces.ISearchController;
import com.yahoo.mobile.client.share.search.interfaces.ISearchViewHolder;

public class YahooSearchHeaderLayout extends RelativeLayout implements ISearchViewHolder {

	public YahooSearchHeaderLayout(Context context) {
		super(context);
	}

	public YahooSearchHeaderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public YahooSearchHeaderLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setSearchController(ISearchController searchController) {
	}

	@Override
	public EditText getSearchEditText() {
		return (EditText) findViewById(ResourceUtil.getId(getContext(), "search_edit"));
	}

	@Override
	public View getVoiceSearchButton() {
		return findViewById(ResourceUtil.getId(getContext(), "search_voice_layout"));
	}

	@Override
	public View getClearTextButton() {
		return findViewById(ResourceUtil.getId(getContext(), "search_clear_layout"));
	}

	@Override
	public int getSearchViewHeightOffset() {
		return 0;
	}

	@Override
	public void onVoiceSearchAvailabilityChanged(boolean isVoiceSearchAvailable) {

	}

	@Override
	public void onFocusChanged(EditText arg0, boolean arg1) {
	}

}
