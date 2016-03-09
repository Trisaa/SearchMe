package com.solo.search.util;

public class SearchConfig {

	public static final String KEY_APP = "key_search_app";
	public static final String KEY_CONTACT = "key_search_contact";
	public static final String KEY_MESSAGE = "key_search_message";
	public static final String KEY_MUSIC = "key_search_music";
	public static final String KEY_BOOKMARK = "key_search_bookmark";
	public static final String KEY_WEB = "key_search_web";
	public static final String KEY_SEARCH_PATTERN = "key_search_pattern";
	public static final String KEY_SEARCH_ENGINE = "key_search_engine";
	public static final String KEY_SHOW_SEARCH_BAR_HOTWORD = "key_show_search_bar_hotword";

	public static final String KEY_SOLO_SEARCH_ENGINE_NAME = "key_solo_search_engine_key";
	public static final String KEY_SOLO_SEARCH_ENGINE_KEY = "key_solo_search_engine_name";
	public static final String KEY_SOLO_SEARCH_ENGINE_ICON = "key_solo_search_engine_icon";
	public static final String KEY_SOLO_SEARCH_ENGINE_URL = "key_solo_search_engine_url";
	public static final String KEY_GET_SEARCG_ENGINE_TIME = "key_get_solo_search_engine_time";
	public static final String KEY_GET_HOTWORD_SUCC_TIME = "key_get_hotword_succ_time";
	public static final String KEY_INITILIZED_CARDS = "initilized_cards";
	public static final String KEY_GET_IME_HOTWORD_SUCC_TIME = "key_get_ime_hotword_succ_time";
	public static final String KEY_IME_HOTWORD_DATA = "key_ime_hotword_data";

	public static final String KEY_STOCKS = "stocks";

	public static final String KEY_SOURCE_CURRENCY = "key_source_currency";
	public static final String KEY_TARGET_CURRENCY = "key_target_currency";

	public static final String DEFAULT_SOURCE_CURRENCY = "USD";
	public static final String DEFAULT_TARGET_CURRENCY = "EUR";

	public static final int MAX_SUGGESTIONS_SIZE = 20;
	public static final int MAX_SUGGESTIONS_SIZE_WEB = 10;

	public static final int SEARCH_ENGINE_SOLO = 0;
	public static final int SEARCH_ENGINE_GOOGLE = 1;
	public static final int SEARCH_ENGINE_BING = 2;
	public static final int SEARCH_ENGINE_YAHOO = 3;
	public static final int SEARCH_ENGINE_DUCKDUCKGO = 4;
	public static final int SEARCH_ENGINE_BAIDU = 5;
	public static final int SEARCH_ENGINE_AOL = 6;

	public static final String FEED_TYPE_HOTWORD = "hotword";
	public static final String FEED_TYPE_APP = "app";
	public static final String FEED_TYPE_WEBPAGE = "webpage";

	public static final String URL_YAHOO_SEARCH = "http://search.yahoo.com/search?p=%s";
	public static final String URL_YANDEX_SEARCH_PREFIX = "http://yandex.ru/touchsearch?text=";
	public static final String URL_YANDEX_SEARCH_SUGESS_PREFIX = "suggest.apply(";// Yandex
																					// 搜索建议词的返回数据格式：suggest.apply(["solo",["solomon","solomon обувь официальный сайт"],[]],[])
	public static final String URL_YANDEX_SEARCH_SUGESS_SUFFIX = ")";

	public static final String EXTRA_SEARCH_LOCAL_KEYWORD = "search_local_keyword";
}
