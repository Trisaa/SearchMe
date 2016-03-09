package com.solo.search.browser.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.solo.search.util.ResourceUtil;

public class AssistInputBar extends FrameLayout implements OnClickListener {

	private TextView mThreeWDotTv;
	private TextView mSlashTv;
	private TextView mDotTv;
	private TextView mDotComTv;
	private TextView mDotCountryTv;
	private TextView mMobileDotTv;

	private ImageView mBackBtn;
	private ImageView mNextBtn;

	private EditText mEditText;
	private WebView mWebView;

	public AssistInputBar(Context context) {
		super(context);
	}

	public AssistInputBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setEditTextView(EditText ed) {
		mEditText = ed;
	}

	public void setWebView(WebView webView) {
		mWebView = webView;
	}

	@Override
	protected void onFinishInflate() {
		mThreeWDotTv = (TextView) findViewById(ResourceUtil.getId(getContext(), "three_w_dot"));
		mSlashTv = (TextView) findViewById(ResourceUtil.getId(getContext(), "slash"));
		mDotTv = (TextView) findViewById(ResourceUtil.getId(getContext(), "dot"));
		mDotComTv = (TextView) findViewById(ResourceUtil.getId(getContext(), "dot_com"));
		mDotCountryTv = (TextView) findViewById(ResourceUtil.getId(getContext(), "dot_country"));
		mMobileDotTv = (TextView) findViewById(ResourceUtil.getId(getContext(), "mobile_dot"));
		mBackBtn = (ImageView) findViewById(ResourceUtil.getId(getContext(), "back_btn"));
		mNextBtn = (ImageView) findViewById(ResourceUtil.getId(getContext(), "forward_btn"));

		mThreeWDotTv.setOnClickListener(this);
		mSlashTv.setOnClickListener(this);
		mDotTv.setOnClickListener(this);
		mDotComTv.setOnClickListener(this);
		mDotCountryTv.setOnClickListener(this);
		mMobileDotTv.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == ResourceUtil.getId(getContext(), "three_w_dot") || id == ResourceUtil.getId(getContext(), "slash")
				|| id == ResourceUtil.getId(getContext(), "dot") || id == ResourceUtil.getId(getContext(), "dot_com")
				|| id == ResourceUtil.getId(getContext(), "dot_country")
				|| id == ResourceUtil.getId(getContext(), "mobile_dot")) {
			TextView tv = (TextView) v;
			if (mEditText != null && !TextUtils.isEmpty(tv.getText())) {
				String finalStr = mEditText.getText().toString() + tv.getText();
				mEditText.setText(finalStr);
				mEditText.setSelection(mEditText.getText().length());
			}
		} else if (id == ResourceUtil.getId(getContext(), "back_btn")) {
			if (mWebView != null && mWebView.canGoBack()) {
				mWebView.goBack();
			} else if (id == ResourceUtil.getId(getContext(), "forward_btn")) {
				if (mWebView != null && mWebView.canGoForward()) {
					mWebView.goForward();
				}
			}
		}
	}
}
