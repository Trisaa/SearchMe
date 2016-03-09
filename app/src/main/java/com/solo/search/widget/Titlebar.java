package com.solo.search.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.solo.search.util.ResourceUtil;

public class Titlebar extends RelativeLayout implements OnClickListener {

	private LinearLayout mBackLayout;
	private TextView mTitleTV;
	private LinearLayout mRightLayout;
	private String mTitle;
	private Drawable mRightIcon;

	private OnTitlebarClickListener mListener;

	public Titlebar(Context context) {
		super(context);
		init(null, 0);
	}

	public Titlebar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public Titlebar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private int[] getAttrs() {
		int[] attrs = { ResourceUtil.getId(getContext(), "ssearch_titlebar_title"),
				ResourceUtil.getId(getContext(), "ssearch_titlebar_right_icon") };
		return attrs;
	}

	private void init(AttributeSet attrs, int defStyle) {
		TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, getAttrs(), defStyle, defStyle);

		int titleResId = a.getResourceId(ResourceUtil.getStyleableId(getContext(), "ssearch_titlebar_title"), ResourceUtil
				.getStringId(getContext(), "ssearch_hint"));
		mTitle = getResources().getString(titleResId);
		mRightIcon = a.getDrawable(ResourceUtil.getStyleableId(getContext(), "ssearch_titlebar_right_icon"));

		a.recycle();

		initView();
		setupView();
	}

	private void initView() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(ResourceUtil.getLayoutId(getContext(), "ssearch_titlebar_layout"), this, true);
		mBackLayout = (LinearLayout) view.findViewById(ResourceUtil.getId(getContext(), "back_layout"));
		mTitleTV = (TextView) view.findViewById(ResourceUtil.getId(getContext(), "title"));
		mRightLayout = (LinearLayout) view.findViewById(ResourceUtil.getId(getContext(), "right_btn_layout"));
		if (mRightIcon != null) {
			mRightLayout.setVisibility(View.VISIBLE);
			mRightLayout.setOnClickListener(this);
			((ImageView) view.findViewById(ResourceUtil.getId(getContext(), "right_icon"))).setImageDrawable(mRightIcon);
		} else {
			mRightLayout.setVisibility(View.GONE);
		}
	}

	private void setupView() {
		setTitle(mTitle);
		mBackLayout.setOnClickListener(this);
		mRightLayout.setOnClickListener(this);
	}

	public void setTitle(String title) {
		if (mTitleTV != null && !TextUtils.isEmpty(title)) {
			mTitleTV.setText(title);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == ResourceUtil.getId(getContext(), "back_layout")) {
			if (mListener != null) {
				mListener.onBackButtonClick();
			}
		} else if (id == ResourceUtil.getId(getContext(), "right_btn_layout")) {
			if (mListener != null) {
				mListener.onRightButtonClick();
			}
		}
	}

	public void setOnTitlebarClickListener(OnTitlebarClickListener listener) {
		mListener = listener;
	}

	public interface OnTitlebarClickListener {
		public void onBackButtonClick();

		public void onRightButtonClick();
	}

}
