package com.solo.search.widget.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class SearchWebView extends SafeWebView {

	public SearchWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		WebSettings settings = getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDomStorageEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_DEFAULT);
		setHorizontalScrollbarOverlay(true);
		setWebChromeClient(mWebChromeClient);
		setHorizontalScrollbarOverlay(true);
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	@Override
	public void addJavascriptInterface(Object object, String name) {
		super.addJavascriptInterface(object, name);
	}

	@Override
	public void setWebChromeClient(WebChromeClient client) {
		super.setWebChromeClient(client);
	}

	private final WebChromeClient mWebChromeClient = new WebChromeClient() {

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
		}

		@Override
		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
			return super.onJsAlert(view, url, message, result);
		}

	};

}
