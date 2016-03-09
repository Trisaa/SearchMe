package com.solo.search.util;

import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.solo.search.browser.SearchBrowserActivity;
import com.yahoo.mobile.client.share.search.ui.activity.SearchActivity;
import com.yahoo.mobile.client.share.search.ui.activity.TrendingSearchEnum;

/**
 * 应用启动器。启动应用，包括启动内置浏览器打开url等。
 * 
 * 
 */
public class AppLauncher {

	public static final int VOICE_RECOGNITION_REQUEST_CODE = 1000;
	public static final String PACKAGE_FACEBOOK = "com.facebook.katana";
	public static final String PACKAGE_GOOGLEPLUS = "com.google.android.apps.plus";

	// Browser
	public static final String ACTION_BROWSER = "com.solo.search.ACTION.BROWSER";
	public static final String EXTRA_SEARCH_BROWSER_URL = "search_browser_url";
	public static final String EXTRA_BROWSER_FULL_SCREEN = "browser_full_screen";

	private static void launchYahooSearch(Context context, String keyword) {
		SearchActivity.IntentBuilder builder = new SearchActivity.IntentBuilder();

		builder.setTrendingCategory(TrendingSearchEnum.CELEBRITY);
		builder.addWebVertical();
		builder.addImageVertical();
		builder.addVideoVertical();
		Intent intent = builder.buildIntent(context);
		intent.putExtra(SearchActivity.QUERY_STRING, keyword);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(SearchActivity.HEADER_RESOURCE_KEY, ResourceUtil.getLayoutId(context, "ssearch_yssdk_custom_header"));
		context.startActivity(intent);
		((Activity) context).overridePendingTransition(0, 0);
	}

	private static void launchNormalSearch(Context context, String keyword) {
		if (!TextUtils.isEmpty(keyword)) {
			String url = SearchHelper.getSearchSource(context).getSearchUrl(keyword);
			AppLauncher.launchBrowser(context, url);
		} else {
			String homePageUrl = SearchHelper.getSearchSource(context).getSearchHomeUrl();
			if (!TextUtils.isEmpty(homePageUrl)) {
				AppLauncher.launchBrowser(context, homePageUrl);
			} else {
				Toast.makeText(context, ResourceUtil.getStringId(context, "ssearch_query_cannot_empty"), Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	/**
	 * 搜索关键字
	 * 
	 * @param context
	 * @param keyword
	 */
	public static void launchSearch(Context context, String keyword) {
		int searchEngine = SearchHelper.getSearchEngine(context);
		String defaultEngineUrl = SharedPreferencesHelper.getString(context, SearchConfig.KEY_SOLO_SEARCH_ENGINE_URL,
				SearchConfig.URL_YAHOO_SEARCH);
		// 如果默认是雅虎搜索,则执行相关逻辑。
		if (searchEngine == SearchConfig.SEARCH_ENGINE_SOLO && defaultEngineUrl.equals(SearchConfig.URL_YAHOO_SEARCH)) {
			launchYahooSearch(context, keyword);
		} else {
			launchNormalSearch(context, keyword);
		}
	}

	public static void launchBrowser(Context context, String url) {
		launchBrowser(context, url, false);
	}

	/**
	 * 使用内置浏览器打开Url
	 * 
	 * @param context
	 * @param url
	 * @param fullScreen
	 *            浏览器页面是否进入全屏模式
	 */
	public static void launchBrowser(Context context, String url, boolean fullScreen) {
		Intent intent = new Intent(context, SearchBrowserActivity.class);
		intent.putExtra(EXTRA_SEARCH_BROWSER_URL, url);
		intent.putExtra(EXTRA_BROWSER_FULL_SCREEN, fullScreen);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public static void startFacebook(Context context) {
		try {
			if (DeviceUtils.isApkInstalled(context, PACKAGE_FACEBOOK)) {
				Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(SoloLauncherWeb.FACEBOOK_APP_URL));
				appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(appIntent,
						PackageManager.MATCH_DEFAULT_ONLY);
				if (list != null && list.size() > 0) {
					context.startActivity(appIntent);
				}
			} else {
				launchBrowser(context, SoloLauncherWeb.FACEBOOK_PAGE_URL);
			}
		} catch (Exception e) {

		}
	}

	/**
	 * 直接跳转GP还是弹出商店的选择列表
	 */

	public static void openAppStore(Context context, String marketUrl) {
		if (DeviceUtils.isApkInstalled(context, "com.android.vending")) {
			try {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(marketUrl));
				browserIntent.setClassName("com.android.vending", "com.android.vending.AssetBrowserActivity");
				browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(browserIntent);
			} catch (Exception e) {

			}
		} else {
			try {
				Uri u = Uri.parse(marketUrl);
				Intent market = new Intent(Intent.ACTION_VIEW, u);
				market.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(market);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(context, ResourceUtil.getStringId(context, "ssearch_market_not_found"), Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

}
