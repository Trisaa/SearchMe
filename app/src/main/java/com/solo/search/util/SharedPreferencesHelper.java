package com.solo.search.util;

import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * SharedPreferences工具类。对系统的SharedPreferences的所有数据类型的方法都进行了进一步封装方便使用。
 * 
 * 
 */
public final class SharedPreferencesHelper {

	public static final String DEFAULT_NAME = "com.solo.search_preferences";

	private static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE);
	}

	private static Editor edit(Context context) {
		SharedPreferences sp = getSharedPreferences(context);
		return sp.edit();
	}

	public static void setBoolean(Context context, String key, boolean value) {
		SharedPreferences.Editor editPrefs = edit(context);
		editPrefs.putBoolean(key, value);
		editPrefs.commit();
	}

	public static void setFloat(Context context, String key, float value) {
		SharedPreferences.Editor editPrefs = edit(context);
		editPrefs.putFloat(key, value);
		editPrefs.commit();
	}

	public static void setInt(Context context, String key, int value) {
		SharedPreferences.Editor editPrefs = edit(context);
		editPrefs.putInt(key, value);
		editPrefs.commit();
	}

	public static void setLong(Context context, String key, long value) {
		SharedPreferences.Editor editPrefs = edit(context);
		editPrefs.putLong(key, value);
		editPrefs.commit();
	}

	public static void setString(Context context, String key, String value) {
		SharedPreferences.Editor editPrefs = edit(context);
		editPrefs.putString(key, value);
		editPrefs.commit();
	}

	public static void setString(Context context, String key, Set<String> values) {
		SharedPreferences.Editor editPrefs = edit(context);
		editPrefs.putStringSet(key, values);
		editPrefs.commit();
	}

	public static boolean getBoolean(Context context, String key, boolean defValue) {
		SharedPreferences sp = getSharedPreferences(context);
		return sp.getBoolean(key, defValue);
	}

	public static int getInt(Context context, String key, int defValue) {
		SharedPreferences sp = getSharedPreferences(context);
		return sp.getInt(key, defValue);
	}

	public static float getFloat(Context context, String key, float defValue) {
		SharedPreferences sp = getSharedPreferences(context);
		return sp.getFloat(key, defValue);
	}

	public static int getBoolean(Context context, String key, int defValue) {
		SharedPreferences sp = getSharedPreferences(context);
		return sp.getInt(key, defValue);
	}

	public static long getLong(Context context, String key, long defValue) {
		SharedPreferences sp = getSharedPreferences(context);
		return sp.getLong(key, defValue);
	}

	public static String getString(Context context, String key, String defValue) {
		SharedPreferences sp = getSharedPreferences(context);
		return sp.getString(key, defValue);
	}

	public static Set<String> getStringSet(Context context, String key, Set<String> defValues) {
		SharedPreferences sp = getSharedPreferences(context);
		return sp.getStringSet(key, defValues);
	}
}
