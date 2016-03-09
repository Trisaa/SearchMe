package com.solo.search.browser.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;

public interface BrowserController {

	public Activity getActivity();

	public void updateUrl(String url, boolean shortUrl);

	public void updateProgress(int progress);

	public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback);

	public void onHideCustomView();

	public Bitmap getDefaultVideoPoster();

	public View getVideoLoadingProgressView();

	public void onLongPress();
}
