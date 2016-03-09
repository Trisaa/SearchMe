package com.solo.search.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public final class ImageUtils {

	private static int sIconWidth = -1;
	private static int sIconHeight = -1;

	private static void initStatics(Context context) {
		final Resources resources = context.getResources();
		sIconWidth = sIconHeight = (int) resources.getDimension(ResourceUtil.getDimenId(context, "ssearch_app_icon_size"));
	}

	public static Drawable createThumbnail(Bitmap bitmap, Context context) {
		if (sIconWidth == -1) {
			initStatics(context);
		}

		return new BitmapDrawable(Bitmap.createScaledBitmap(bitmap, sIconWidth, sIconHeight, false));
	}

	public static int getIconWidth(Context context) {
		if (sIconWidth == -1) {
			initStatics(context);
		}

		return sIconWidth;
	}

	public static int getIconHeight(Context context) {
		if (sIconHeight == -1) {
			initStatics(context);
		}

		return sIconHeight;
	}

}
