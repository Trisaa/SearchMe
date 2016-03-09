package com.solo.search.widget.webview;

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;

public class SafeWebView extends ObservableWebView {

	public SafeWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		fixWebview();
	}

	public SafeWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		fixWebview();
	}

	public SafeWebView(Context context) {
		super(context);
		fixWebview();
	}

	private void fixWebview(String name, String params) {
		try {
			Method localMethod = WebView.class.getDeclaredMethod(name, new Class[] { String.class });
			if (localMethod != null) {
				localMethod.setAccessible(true);
				localMethod.invoke(this, new Object[] { params });
			}
			return;
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@SuppressLint({ "NewApi" })
	private boolean fixWebview() {
		try {
			if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 17) {
				fixWebview("removeJavascriptInterface", "searchBoxJavaBridge_");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
