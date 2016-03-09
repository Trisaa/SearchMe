package com.solo.search.card;

import android.content.Context;

import com.solo.search.util.ApiConstants;
import com.solo.search.util.DeviceUtils;
import com.solo.search.util.SearchConfig;
import com.solo.search.util.SharedPreferenceConstants;
import com.solo.search.util.SharedPreferencesHelper;

public class CardConfig {

	public static final long INTERVAL_UDPATE_CARD = 60 * 1000;// 更新卡片的时间间隔，单位：毫秒。

	public static final String FACEBOOK_ADS_PLACEMENT_IDS = "1560511240859053_1598184497091727";
	public static final String FACEBOOK_BANNER_PLACEMENT_ID = "1560511240859053_1609309782645865";

	private static final String URL_HOST = ApiConstants.API_BASE_URL + "/v1";
	// private static final String URL_HOST_TEST =
	// "http://114.113.223.246:8001/v1";
	// private static final String URL_HOST_TEST_AWS =
	// "http://api.ytgxjr.com/v1";

	private static final String URL_HOST_CARD = URL_HOST + "/card";
	private static final String URL_DEFAULT_PARAMETERS = "page={0}&size={1}&campaign={2}&lang={3}&version_code={4}&device_id={5}&app_id={6}";
	private static final String URL_BASIC_PARAMETERS = "campaign={0}&lang={1}&version_code={2}&device_id={3}&app_id={4}";

	private static final String URL_ADS = URL_HOST_CARD + "/ads?" + URL_DEFAULT_PARAMETERS;
	private static final String URL_HOTWORD = URL_HOST_CARD + "/hotwords?" + URL_DEFAULT_PARAMETERS;
	public static final String URL_HOTNEWS = URL_HOST_CARD + "/news?" + URL_DEFAULT_PARAMETERS;
	private static final String URL_VIDEO = URL_HOST_CARD + "/videos?" + URL_DEFAULT_PARAMETERS;
	private static final String URL_STOCKS = URL_HOST_CARD
			+ "/stocks?symbol={0}&campaign={1}&lang={2}&version_code={3}&device_id={4}";
	public static final String URL_STOCK = URL_HOST_CARD
			+ "/stock?symbol={0}&campaign={1}&lang={2}&version_code={3}&device_id={4}";
	public static final String URL_HOT_STOCKS = URL_HOST_CARD
			+ "/hot_stocks?campaign={0}&lang={1}&version_code={2}&device_id={3}";
	private static final String URL_GAMES = URL_HOST_CARD + "/games?" + URL_DEFAULT_PARAMETERS;
	private static final String URL_FUNNY = URL_HOST_CARD + "/news/funny?" + URL_DEFAULT_PARAMETERS;

	private static final String URL_CURRENCY = URL_HOST_CARD + "/currency?" + URL_BASIC_PARAMETERS;
	public static final String URL_DEFAULT_SEARCH_ENGINE = URL_HOST_CARD + "/search_engine?" + URL_BASIC_PARAMETERS;

	public static final String URL_SOLO_NEWS = "http://news.solo-launcher.com/hot";
	public static final String URL_GAME_CENTER = "http://game.solo-launcher.com/";
	public static final String URL_SOLO_FUNNY_PICTURES = "http://news.solo-launcher.com/funny";

	// 从桌面上Icon点击进入的链接
	private static final String URL_ICON_REF = "?ref=icon";
	public static final String URL_SOLO_NEWS_ICON = URL_SOLO_NEWS + URL_ICON_REF;
	public static final String URL_GAME_CENTER_ICON = URL_GAME_CENTER + URL_ICON_REF;
	public static final String URL_SOLO_FUNNY_PICTURES_ICON = URL_SOLO_FUNNY_PICTURES + URL_ICON_REF;

	public static final String CARD_ID_ADS = "0";
	public static final String CARD_ID_HOTWORD = "1";
	public static final String CARD_ID_HOTNEWS = "2";
	public static final String CARD_ID_VIDEO = "3";
	public static final String CARD_ID_STOCK = "4";
	public static final String CARD_ID_CURRENCY = "5";
	public static final String CARD_ID_GAME = "6";
	public static final String CARD_ID_FUNNY = "7";

	public static final int CARD_ORDER_ADS = 0;
	public static final int CARD_ORDER_HOTWORD = 100;
	public static final int CARD_ORDER_HOTNEWS = 200;
	public static final int CARD_ORDER_FUNNY = 225;
	public static final int CARD_ORDER_GAME = 250;
	public static final int CARD_ORDER_VIDEO = 300;
	public static final int CARD_ORDER_STOCK = 400;
	public static final int CARD_ORDER_CURRENCY = 500;

	// 卡片数据的类型
	public static final String CARD_TYPE_APP = "app";
	public static final String CARD_TYPE_WEBPAGE = "webpage";

	private static final int REQUEST_CARD_PAGE = 1;
	private static final int REQUEST_CARD_PAGE_SIZE = 50;

	public static final long TIME_RESHOW_ADS = 6 * 60 * 60 * 1000;// 关闭广告卡片后，6个小时后会重新显示。
	public static final long UPDATE_INTERVAL_CURRENCY = 60 * 60 * 1000;// 汇率刷新间隔为60分钟。

	public static final int CARD_STATE_DISABLE = 0;
	public static final int CARD_STATE_ENABLE = 1;

	public static final String CARD_ID = "card_id";
	public static final String CARD_TITLE = "card_title";
	public static final String CARD_ENABLE = "card_enable";
	public static final String CARD_ORDER = "card_order";
	public static final String CARD_UPDATE_INTERVAL = "update_interval";
	public static final String CARD_UPDATE_TIME = "update_time";
	public static final String CARD_DATA = "card_data";
	public static final String CARD_ITEMS_DATA = "items_data";
	public static final String CARD_MENU_DATA = "menu_data";

	public static final String[] CARD_DB_PROJECTION = new String[] { CARD_ID, CARD_TITLE, CARD_ENABLE, CARD_ORDER,
			CARD_UPDATE_INTERVAL, CARD_UPDATE_TIME, CARD_DATA };

	// 第一次安装需要初始化存入数据库的卡片数据
	public static final String[] DEFAULT_CARDS_ID = new String[] { CARD_ID_ADS, CARD_ID_HOTWORD, CARD_ID_HOTNEWS,
			CARD_ID_FUNNY, CARD_ID_GAME, CARD_ID_VIDEO, CARD_ID_STOCK, CARD_ID_CURRENCY };

	/*
	 * public static final int[] DEFAULT_CARDS_TITLE = new int[] {
	 * R.string.ssearch_card_ads, R.string.ssearch_card_hotwords,
	 * R.string.ssearch_card_news, R.string.ssearch_solo_funny,
	 * R.string.ssearch_card_game, R.string.ssearch_card_video,
	 * R.string.ssearch_card_stock, R.string.ssearch_card_currency };
	 */

	// 初始化卡片状态，是否显示。
	public static final int[] INITILIZED_CARDS_STATE = new int[] { CARD_STATE_ENABLE, CARD_STATE_ENABLE, CARD_STATE_ENABLE,
			CARD_STATE_ENABLE, CARD_STATE_ENABLE, CARD_STATE_ENABLE, CARD_STATE_DISABLE, CARD_STATE_DISABLE };

	// 初始化卡片顺序
	public static final int[] INITILIZED_CARDS_ORDER = new int[] { CARD_ORDER_ADS, CARD_ORDER_HOTWORD, CARD_ORDER_HOTNEWS,
			CARD_ORDER_FUNNY, CARD_ORDER_GAME, CARD_ORDER_VIDEO, CARD_ORDER_STOCK, CARD_ORDER_CURRENCY };

	// 默认股票
	public static final String DEFAULT_STOCKS_SYMBOLS = "AAPL,GOOG";

	// 卡片编辑界面不可编辑的卡片。
	public static final String[] CARDS_NOT_EDITABLE = new String[] { CARD_ID_ADS, CARD_ID_HOTWORD };

	public static final String DEFAULT_APPID = "1004";

	public static String appendBasicParameters(Context context, String basicUrl) {
		int versionCode = DeviceUtils.getVersionCode(context, context.getPackageName());
		String isoCode = DeviceUtils.getCountryISOCode(context);
		isoCode="us";
		String appId = SharedPreferencesHelper.getString(context, SharedPreferenceConstants.KEY_APP_ID, DEFAULT_APPID);
		String url = basicUrl.replace("{0}", isoCode).replace("{1}", DeviceUtils.getLocaleLanguage(context))
				.replace("{2}", String.valueOf(versionCode)).replace("{3}", DeviceUtils.getDeviceUUID(context))
				.replace("{4}", appId);
		url=url+"&flag=0";
		return url;
	}

	private static String appendDefaultParameters(Context context, String baseUrl, int page, int pageSize) {
		int version_code = DeviceUtils.getVersionCode(context, context.getPackageName());
		String isoCode = DeviceUtils.getCountryISOCode(context);
		isoCode="us";
		String appId = SharedPreferencesHelper.getString(context, SharedPreferenceConstants.KEY_APP_ID, DEFAULT_APPID);
		String url = baseUrl.replace("{0}", String.valueOf(page)).replace("{1}", String.valueOf(pageSize))
				.replace("{2}", isoCode).replace("{3}", DeviceUtils.getLocaleLanguage(context))
				.replace("{4}", String.valueOf(version_code)).replace("{5}", DeviceUtils.getDeviceUUID(context))
				.replace("{6}", appId);
		url=url+"&flag=0";
		return url;
	}

	private static String buildUrl(Context context, String baseUrl) {
		return appendDefaultParameters(context, baseUrl, REQUEST_CARD_PAGE, REQUEST_CARD_PAGE_SIZE);
	}

	private static String buildStocksUrl(Context context) {
		String symbols = SharedPreferencesHelper.getString(context, SearchConfig.KEY_STOCKS, DEFAULT_STOCKS_SYMBOLS);
		int version_code = DeviceUtils.getVersionCode(context, context.getPackageName());
		String isoCode = DeviceUtils.getCountryISOCode(context);
		String url = URL_STOCKS.replace("{0}", symbols).replace("{1}", isoCode)
				.replace("{2}", DeviceUtils.getLocaleLanguage(context)).replace("{3}", String.valueOf(version_code))
				.replace("{4}", DeviceUtils.getDeviceUUID(context));
		return url;
	}

	private static String buildCurrencyUrl(Context context) {
		return appendBasicParameters(context, URL_CURRENCY);
	}

	public static String buildHotStocksUrl(Context context) {
		return appendBasicParameters(context, URL_HOT_STOCKS);
	}

	public static String getCardUrl(Context context, String cardId) {
		String url = null;
		switch (cardId) {
		case CARD_ID_ADS:
			url = buildUrl(context, URL_ADS);
			break;
		case CARD_ID_HOTWORD:
			url = buildUrl(context, URL_HOTWORD);
			break;
		case CARD_ID_HOTNEWS:
			url = buildUrl(context, URL_HOTNEWS);
			break;
		case CARD_ID_VIDEO:
			url = buildUrl(context, URL_VIDEO);
			break;
		case CARD_ID_STOCK:
			url = buildStocksUrl(context);
			break;
		case CARD_ID_CURRENCY:
			url = buildCurrencyUrl(context);
			break;
		case CARD_ID_GAME:
			url = buildUrl(context, URL_GAMES);
			break;
		case CARD_ID_FUNNY:
			url = buildUrl(context, URL_FUNNY);
			break;
		}
		return url;
	}

	public static boolean isEditableCard(String cardId) {
		boolean editable = true;
		for (String id : CARDS_NOT_EDITABLE) {
			if (id.equals(cardId)) {
				editable = false;
				break;
			}

		}
		return editable;
	}
}
