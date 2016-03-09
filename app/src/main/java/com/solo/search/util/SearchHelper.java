package com.solo.search.util;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.solo.search.card.CardConfig;
import com.solo.search.card.CardManager;
import com.solo.search.source.AolSource;
import com.solo.search.source.BaiduSource;
import com.solo.search.source.BingSource;
import com.solo.search.source.DuckDuckGoSource;
import com.solo.search.source.GoogleSource;
import com.solo.search.source.SearchSource;
import com.solo.search.source.SoloSource;
import com.solo.search.source.YahooSource;

public class SearchHelper {

	private static final int TIME_GET_DEFAULT_SEARCH_ENGINE = 2 * 60 * 60 * 1000;// 获取Solo
	private static final int TIME_GET_IME_HOTWORDS = 2 * 60 * 60 * 1000;// 获取键盘热词的时间为1小时。

	public static boolean shoulGetOnlineImeHotwords(Context context) {
		long lastSetTime = SharedPreferencesHelper.getLong(context, SearchConfig.KEY_GET_IME_HOTWORD_SUCC_TIME, 0);
		long curTime = System.currentTimeMillis();
		if (curTime - lastSetTime > TIME_GET_IME_HOTWORDS) {
			return true;
		} else {
			return false;
		}
	}

	public static void initDefaultSearchEngine(Context context) {
		long lastSetTime = SharedPreferencesHelper.getLong(context, SearchConfig.KEY_GET_SEARCG_ENGINE_TIME, 0);
		long curTime = System.currentTimeMillis();
		if (curTime - lastSetTime > TIME_GET_DEFAULT_SEARCH_ENGINE) {
			getDefaultSearchEngineUrl(context);
		}
	}

	/**
	 * 获取默认搜素引擎的Url
	 * 
	 * @param context
	 */
	private static void getDefaultSearchEngineUrl(final Context context) {
		String versionCode = String.valueOf(DeviceUtils.getVersionCode(context, context.getPackageName()));
		final String url = CardConfig.URL_DEFAULT_SEARCH_ENGINE.replace("{0}", DeviceUtils.getCountryISOCode(context)).replace(
				"{1}", DeviceUtils.getLocaleLanguage(context)).replace("{2}", versionCode).replace("{3}",
				DeviceUtils.getDeviceUUID(context));
		StringRequest mRequest = new StringRequest(Method.GET, url, new Listener<String>() {
			@Override
			public void onResponse(String result) {
				try {
					JSONObject object = new JSONObject(result);
					String engineUrl = object.getString("url");

					if (TextUtils.isEmpty(engineUrl)) {
						engineUrl = SearchConfig.URL_YAHOO_SEARCH;
					}

					SharedPreferencesHelper.getString(context, SearchConfig.KEY_SOLO_SEARCH_ENGINE_URL, engineUrl);
					SharedPreferencesHelper.setLong(context, SearchConfig.KEY_GET_SEARCG_ENGINE_TIME, System
							.currentTimeMillis());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				error.printStackTrace();
			}
		});

		CardManager.getInstance(context).addToRequestQueue(mRequest);
	}

	public static int getSearchEngine(Context context) {
		int searchEngine = Integer.valueOf(SharedPreferencesHelper
				.getString(context, SearchConfig.KEY_SEARCH_ENGINE, String.valueOf(context.getResources().getInteger(
						ResourceUtil.getIntegerId(context, "ssearch_config_search_engine")))));
		return searchEngine;
	}

	public static boolean isSearchAppActive(Context context) {
		return SharedPreferencesHelper.getBoolean(context, SearchConfig.KEY_APP, context.getResources().getBoolean(
				ResourceUtil.getBoolId(context, "ssearch_config_search_app")));
	}

	public static boolean isSearchContactActive(Context context) {
		return SharedPreferencesHelper.getBoolean(context, SearchConfig.KEY_CONTACT, context.getResources().getBoolean(
				ResourceUtil.getBoolId(context, "ssearch_config_search_contact")));
	}

	public static boolean isSearchMessageActive(Context context) {
		return SharedPreferencesHelper.getBoolean(context, SearchConfig.KEY_MESSAGE, context.getResources().getBoolean(
				ResourceUtil.getBoolId(context, "ssearch_config_search_message")));
	}

	public static boolean isSearchMusicActive(Context context) {
		return SharedPreferencesHelper.getBoolean(context, SearchConfig.KEY_MUSIC, context.getResources().getBoolean(
				ResourceUtil.getBoolId(context, "ssearch_config_search_music")));
	}

	public static boolean isSearchBookmarkActive(Context context) {
		return SharedPreferencesHelper.getBoolean(context, SearchConfig.KEY_BOOKMARK, context.getResources().getBoolean(
				ResourceUtil.getBoolId(context, "ssearch_config_search_bookmark")));
	}

	public static boolean isSearchWebActivie(Context context) {
		return SharedPreferencesHelper.getBoolean(context, SearchConfig.KEY_WEB, context.getResources().getBoolean(
				ResourceUtil.getBoolId(context, "ssearch_config_search_web")));
	}

	public static int getMaxSuggestionsSize() {
		return SearchConfig.MAX_SUGGESTIONS_SIZE;
	}

	public static int getSearchPatternLevel(Context context) {
		int searchPattern = Integer.valueOf(SharedPreferencesHelper.getString(context, SearchConfig.KEY_SEARCH_PATTERN,
				String.valueOf(context.getResources().getInteger(
						ResourceUtil.getIntegerId(context, "ssearch_config_search_pattern")))));
		return searchPattern;
	}

	public static SearchSource getSearchSource(Context context) {
		int searchEngine = SearchHelper.getSearchEngine(context);
		SearchSource mSearchSource;
		switch (searchEngine) {
		case SearchConfig.SEARCH_ENGINE_SOLO:
			mSearchSource = new SoloSource(context);
			break;
		case SearchConfig.SEARCH_ENGINE_GOOGLE:
			mSearchSource = new GoogleSource(context);
			break;
		case SearchConfig.SEARCH_ENGINE_BING:
			mSearchSource = new BingSource(context);
			break;
		case SearchConfig.SEARCH_ENGINE_YAHOO:
			mSearchSource = new YahooSource(context);
			break;
		case SearchConfig.SEARCH_ENGINE_DUCKDUCKGO:
			mSearchSource = new DuckDuckGoSource(context);
			break;
		case SearchConfig.SEARCH_ENGINE_BAIDU:
			mSearchSource = new BaiduSource(context);
			break;
		case SearchConfig.SEARCH_ENGINE_AOL:
			mSearchSource = new AolSource(context);
			break;
		default:
			mSearchSource = new SoloSource(context);
			break;
		}
		return mSearchSource;
	}
}
