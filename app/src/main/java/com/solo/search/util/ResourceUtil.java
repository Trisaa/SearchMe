package com.solo.search.util;

import android.content.Context;

public class ResourceUtil {

	public static int getId(Context context, String name) {
		return context.getResources().getIdentifier(name, "id", context.getPackageName());
	}

	public static int getLayoutId(Context context, String name) {
		return context.getResources().getIdentifier(name, "layout", context.getPackageName());
	}

	public static int getStringId(Context context, String name) {
		return context.getResources().getIdentifier(name, "string", context.getPackageName());
	}

	public static int getDrawableId(Context context, String name) {
		return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
	}

	public static int getDimenId(Context context, String name) {
		return context.getResources().getIdentifier(name, "dimen", context.getPackageName());
	}

	public static int getStyleId(Context context, String name) {
		return context.getResources().getIdentifier(name, "style", context.getPackageName());
	}

	public static int getColorId(Context context, String name) {
		return context.getResources().getIdentifier(name, "color", context.getPackageName());
	}

	public static int getAnimId(Context context, String name) {
		return context.getResources().getIdentifier(name, "anim", context.getPackageName());
	}

	public static int getBoolId(Context context, String name) {
		return context.getResources().getIdentifier(name, "bool", context.getPackageName());
	}

	public static int getIntegerId(Context context, String name) {
		return context.getResources().getIdentifier(name, "integer", context.getPackageName());
	}

	public static int getStyleableId(Context context, String name) {
		return context.getResources().getIdentifier(name, "styleable", context.getPackageName());
	}

}
