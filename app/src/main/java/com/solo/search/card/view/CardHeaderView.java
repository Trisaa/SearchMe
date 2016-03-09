package com.solo.search.card.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.solo.search.util.ResourceUtil;

public class CardHeaderView extends RelativeLayout implements OnClickListener {

	private TextView mTitleTV;
	private RelativeLayout mRefreshBtn;
	private ImageView mRefreshImg;

	private OnClickListener mOnTitleClickListener;
	private OnClickListener mOnRefreshClickListener;

	public CardHeaderView(Context context) {
		super(context);
		init();
	}

	public CardHeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CardHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		inflate(getContext(), ResourceUtil.getLayoutId(getContext(), "ssearch_card_header"), this);
		mTitleTV = (TextView) findViewById(ResourceUtil.getId(getContext(), "card_header_title"));
		mRefreshBtn = (RelativeLayout) findViewById(ResourceUtil.getId(getContext(), "card_header_refresh"));
		mRefreshImg = (ImageView) findViewById(ResourceUtil.getId(getContext(), "card_refresh_img"));
		setListener();
	}

	private void setListener() {
		mTitleTV.setOnClickListener(this);
		mRefreshBtn.setOnClickListener(this);
	}

	public void setOnTitleClickListener(OnClickListener listener) {
		mOnTitleClickListener = listener;
	}

	public void setOnRefreshClickListener(OnClickListener listener) {
		mOnRefreshClickListener = listener;
	}

	public void setTitleText(String text) {
		mTitleTV.setText(text);
	}

	public void setRefreshButtonVisibility(int visibility) {
		mRefreshBtn.setVisibility(visibility);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == ResourceUtil.getId(getContext(), "card_header_title")) {
			if (mOnTitleClickListener != null) {
				mOnTitleClickListener.onClick(v);
			}
		} else if (id == ResourceUtil.getId(getContext(), "card_header_refresh")) {
			RotateAnimation anim = new RotateAnimation(0.0F, 360.0F, 1, 0.5F, 1, 0.5F);
			anim.setDuration(800L);
			mRefreshImg.startAnimation(anim);
			if (mOnRefreshClickListener != null) {
				mOnRefreshClickListener.onClick(v);
			}
		}
	}
}
