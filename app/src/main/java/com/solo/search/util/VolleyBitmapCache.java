package com.solo.search.util;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class VolleyBitmapCache implements ImageCache {

	private LruCache<String, Bitmap> mCache;

	private static VolleyBitmapCache cache;

	public static VolleyBitmapCache getCache() {
		if (cache == null) {
			cache = new VolleyBitmapCache();
		}
		return cache;
	}

	public LruCache<String, Bitmap> getBitmapCache() {
		return mCache;
	}

	private VolleyBitmapCache() {
		// 获取到可用内存的最大值，使用内存超出这个值会引起OutOfMemory异常。
		// LruCache通过构造函数传入缓存值，以KB为单位。
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		// 使用最大可用内存值的1/8作为缓存的大小。
		int cacheSize = maxMemory / 8;
		mCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				// 重写此方法来衡量每张图片的大小，默认返回图片数量。
				return value.getByteCount() / 1024;
			}

		};
	}

	@Override
	public Bitmap getBitmap(String url) {
		return mCache.get(url);
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		mCache.put(url, bitmap);
	}

}
