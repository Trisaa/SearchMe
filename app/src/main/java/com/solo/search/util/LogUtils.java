package com.solo.search.util;

import android.util.Log;

public class LogUtils {

	private static final int MAX_LOG_TAG_LENGTH = 23;
	public static final boolean DEBUG = false;

	private LogUtils() {
	}

	public static String makeLogTag(String str) {
		if (str.length() > MAX_LOG_TAG_LENGTH) {
			return str.substring(0, MAX_LOG_TAG_LENGTH);
		}
		return str;
	}

	public static String makeLogTag(Class cls) {
		return makeLogTag(cls.getSimpleName());
	}

	public static void d(final String tag, String message) {
		if (DEBUG) {
			Log.d(tag, message);
		}
	}

	public static void v(final String tag, String message) {
		if (DEBUG) {
			Log.v(tag, message);
		}
	}

	public static void i(final String tag, String message) {
		Log.i(tag, message);
	}

	public static void w(final String tag, String message) {
		Log.w(tag, message);
	}

	public static void e(final String tag, String message) {
		Log.e(tag, message);
	}

}
