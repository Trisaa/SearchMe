package com.solo.search.browser;


import java.net.URISyntaxException;

import com.solo.search.browser.util.BrowserController;
import com.solo.search.browser.util.BrowserUtils;
import com.solo.search.util.AppLauncher;
import com.solo.search.util.LogUtils;
import com.solo.search.widget.webview.SafeWebView;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SearchBrowserView extends SafeWebView implements DownloadListener {

	private static final String TAG = LogUtils.makeLogTag(SearchBrowserView.class);
	private static final int API = android.os.Build.VERSION.SDK_INT;

	private BrowserController mBrowserController;

	public SearchBrowserView(Context context) {
		super(context);
		initialize();
	}

	public SearchBrowserView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public void setBrowserController(BrowserController controller) {
		mBrowserController = controller;
	}

	@SuppressWarnings("deprecation")
	private void initialize() {

		setDownloadListener(this);

		setDrawingCacheBackgroundColor(0x00000000);
		setFocusableInTouchMode(true);
		setFocusable(true);
		setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		setVerticalScrollBarEnabled(true);
		setVerticalScrollbarOverlay(true);
		setHorizontalScrollBarEnabled(false);
		setHorizontalScrollbarOverlay(false);
		setAnimationCacheEnabled(false);
		setDrawingCacheEnabled(false);
		setWillNotCacheDrawing(true);
		setAlwaysDrawnWithCacheEnabled(false);
		setScrollbarFadingEnabled(true);
		setSaveEnabled(true);
		setWebChromeClient(mWebChromeClient);
		setWebViewClient(mWebViewClient);

		WebSettings settings = getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setDomStorageEnabled(true);
		settings.setSaveFormData(true);
		settings.setDatabaseEnabled(true);
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		settings.setLoadsImagesAutomatically(true);
		settings.setBuiltInZoomControls(true);

		settings.setAppCacheEnabled(true);
		settings.setAppCachePath(getContext().getCacheDir().toString());
		settings.setGeolocationDatabasePath(getContext().getFilesDir().toString());
		settings.setAllowFileAccess(true);
		settings.setSupportZoom(false);
		settings.setAllowContentAccess(true);
		settings.setDefaultTextEncodingName("utf-8");

		if (API < 18) {
			settings.setSavePassword(true);
			settings.setAppCacheMaxSize(Long.MAX_VALUE);
		}
		if (API < 17) {
			settings.setEnableSmoothTransition(true);
		}
		if (API > 16) {
			settings.setMediaPlaybackRequiresUserGesture(true);
		}
		if (API < 19) {
			settings.setDatabasePath(getContext().getCacheDir() + "/databases");
		}
		if (Build.VERSION.SDK_INT > 16) {
			settings.setMediaPlaybackRequiresUserGesture(true);
		}

		if (Build.VERSION.SDK_INT > 16) {
			settings.setAllowFileAccessFromFileURLs(false);
			settings.setAllowUniversalAccessFromFileURLs(false);
		}

		settings.setSaveFormData(true);
		settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

	}

	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			getContext().startActivity(intent);
		} catch (Exception ex) {
		}

	}

	public void clearData() {
		ViewGroup parent = (ViewGroup) getParent();
		if (parent != null) {
			parent.removeView(this);
		}
		stopLoading();
		destroy();
	}

	private final WebChromeClient mWebChromeClient = new WebChromeClient() {

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
			mBrowserController.updateProgress(newProgress);
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
		}

		@Override
		public Bitmap getDefaultVideoPoster() {
			return mBrowserController.getDefaultVideoPoster();
		}

		@Override
		public View getVideoLoadingProgressView() {
			return mBrowserController.getVideoLoadingProgressView();
		}

		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			Activity activity = mBrowserController.getActivity();
			mBrowserController.onShowCustomView(view, activity.getRequestedOrientation(), callback);
			super.onShowCustomView(view, callback);
		}

		@Override
		@Deprecated
		public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
			mBrowserController.onShowCustomView(view, requestedOrientation, callback);
			super.onShowCustomView(view, requestedOrientation, callback);
		}

		@Override
		public void onHideCustomView() {
			mBrowserController.onHideCustomView();
			super.onHideCustomView();
		}

		@Override
		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
			return super.onJsAlert(view, url, message, result);
		}

	};

	private WebViewClient mWebViewClient = new WebViewClient() {

		@Override
		public void onFormResubmission(WebView view, Message dontResend, Message resend) {
			super.onFormResubmission(view, dontResend, resend);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			LogUtils.d(TAG, TAG + " onPageStarted");
			if (SearchBrowserView.this.isShown()) {
				mBrowserController.updateUrl(url, false);
			}
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			LogUtils.d(TAG, TAG + " onPageFinished");
			if (SearchBrowserView.this.isShown()) {
				mBrowserController.updateUrl(url, true);
			}
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			LogUtils.d(TAG, TAG + " shouldOverrideUrlLoading url:" + url);
			if (url.contains("mailto:")) {
				MailTo mailTo = MailTo.parse(url);
				Intent i = BrowserUtils.newEmailIntent(getContext(), mailTo.getTo(), mailTo.getSubject(), mailTo.getBody(),
						mailTo.getCc());
				getContext().startActivity(i);
				view.reload();
				return true;
			} else if (url.startsWith("intent://")) {
				Intent intent = null;
				try {
					intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
				} catch (URISyntaxException ex) {
					return false;
				}
				if (intent != null) {
					try {
						getContext().startActivity(intent);
					} catch (ActivityNotFoundException e) {
					}
					return true;
				}
			} else if (url.startsWith("tel:")) {
				Intent i = BrowserUtils.newTelIntent(url);
				getContext().startActivity(i);
				return true;
			} else if (url.startsWith("geo:")) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				getContext().startActivity(i);
				return true;
			} else if (url.startsWith("market://details?id=")) {
				AppLauncher.openAppStore(getContext(), url);
				return true;
			}

			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.FROYO) {
				return;
			}
			super.onReceivedSslError(view, handler, error);
		}

	};

	// private class TouchListener implements OnTouchListener {
	//
	// float mLocation;
	// float mY;
	// int mAction;
	//
	// @SuppressLint("ClickableViewAccessibility")
	// @Override
	// public boolean onTouch(View view, MotionEvent arg1) {
	// if (view != null && !view.hasFocus()) {
	// view.requestFocus();
	// }
	// mAction = arg1.getAction();
	// mY = arg1.getY();
	// if (mAction == MotionEvent.ACTION_DOWN) {
	// mLocation = mY;
	// } else if (mAction == MotionEvent.ACTION_UP) {
	// if ((mY - mLocation) > mScrollDownThreshold) {
	// if (getScrollY() != 0) {
	// if (mBrowserController != null) {
	// mBrowserController.showActionBar();
	// }
	// } else {
	// mBrowserController.toggleActionBar();
	// }
	// } else if ((mY - mLocation) < -mScrollUpThreshold) {
	// if (mBrowserController != null) {
	// mBrowserController.hideActionBar();
	// }
	// }
	// mLocation = 0;
	// }
	// return mGestureDetector.onTouchEvent(arg1);
	// }
	// }

	// private class CustomGestureListener extends SimpleOnGestureListener {
	//
	// private boolean mCanTriggerLongPress = true;
	//
	// @Override
	// public void onLongPress(MotionEvent e) {
	// if (mCanTriggerLongPress)
	// mBrowserController.onLongPress();
	// }
	//
	// @Override
	// public boolean onDoubleTapEvent(MotionEvent e) {
	// mCanTriggerLongPress = false;
	// return false;
	// }
	//
	// @Override
	// public void onShowPress(MotionEvent e) {
	// mCanTriggerLongPress = true;
	// }
	//
	// @Override
	// public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
	// float distanceY) {
	// return super.onScroll(e1, e2, distanceX, distanceY);
	// }
	// }

}
