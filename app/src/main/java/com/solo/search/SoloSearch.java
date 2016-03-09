package com.solo.search;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.solo.search.browser.SearchBrowserActivity;
import com.solo.search.card.CardConfig;
import com.solo.search.util.AppLauncher;
import com.solo.search.util.IntentUtils;
import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SharedPreferenceConstants;
import com.solo.search.util.SharedPreferencesHelper;
import com.yahoo.mobile.client.share.search.settings.SearchSDKSettings;
import com.yahoo.mobile.client.share.search.settings.SearchSDKSettings.Builder;
import com.yahoo.mobile.client.share.search.util.SafeSearchEnum;
import com.yahoo.mobile.client.share.yahoosearchlibraryexternalplugin.implementations.Browser;
import com.yahoo.mobile.client.share.yahoosearchlibraryexternalplugin.implementations.Factory;

public class SoloSearch {

	public static void initialize(Context context, String appId) {
		String ysdkId = context.getResources().getString(ResourceUtil.getStringId(context, "ssearch_ysdk_app_id"));
		if (TextUtils.isEmpty(ysdkId)) {
			ysdkId = "QyAi7f3e";
		}

		if (TextUtils.isEmpty(appId)) {
			appId = CardConfig.DEFAULT_APPID;
		}

		SharedPreferencesHelper.setString(context, SharedPreferenceConstants.KEY_APP_ID, appId);

		Builder builder = new SearchSDKSettings.Builder(ysdkId).setDeveloperMode(false).setVoiceSearchEnabled(false)
				.setSearchSuggestEnabled(true).setShortUrlEnabled(true).setSafeSearch(SafeSearchEnum.STRICT);
		SearchSDKSettings.initializeSearchSDKSettings(builder);
		Factory factory = (Factory) SearchSDKSettings.getFactory();
		factory.setBrowser(new Browser() {

			@Override
			public Intent getIntent(Context context, String url, String referer) {
				Intent previewIntent = new Intent(context, SearchBrowserActivity.class);
				previewIntent.putExtra(IntentUtils.EXTRA_SEARCH_BROWSER_URL, url);
				return previewIntent;
			}
		});
	}

	public static void launchNewsFeed(Context context) {
		Intent intent = new Intent(context, SearchActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public static void launchLocalSearch(Context context) {
		Intent intent = new Intent(context, SearchLocalActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	private static String getBasicParams(Context context) {
		return "?app_id="
				+ SharedPreferencesHelper.getString(context, SharedPreferenceConstants.KEY_APP_ID, CardConfig.DEFAULT_APPID);
	}

	public static void launchFunnyNews(Context context) {
		Intent intent = new Intent(context, SearchBrowserActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String url = CardConfig.URL_SOLO_FUNNY_PICTURES + getBasicParams(context);
		intent.putExtra(AppLauncher.EXTRA_SEARCH_BROWSER_URL, url);
		intent.putExtra(AppLauncher.EXTRA_BROWSER_FULL_SCREEN, false);
		context.startActivity(intent);
	}

	public static void launchHotnews(Context context) {
		Intent intent = new Intent(context, SearchBrowserActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String url = CardConfig.URL_SOLO_NEWS + getBasicParams(context);
		intent.putExtra(AppLauncher.EXTRA_SEARCH_BROWSER_URL, url);
		intent.putExtra(AppLauncher.EXTRA_BROWSER_FULL_SCREEN, false);
		context.startActivity(intent);
	}

	public static void launchGameCenter(Context context) {
		Intent intent = new Intent(context, SearchBrowserActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String url = CardConfig.URL_GAME_CENTER + getBasicParams(context);
		intent.putExtra(AppLauncher.EXTRA_SEARCH_BROWSER_URL, url);
		intent.putExtra(AppLauncher.EXTRA_BROWSER_FULL_SCREEN, true);
		context.startActivity(intent);
	}

	public static void launchBrowser(Context context, String keyword) {
		AppLauncher.launchSearch(context, keyword);
	}
}
