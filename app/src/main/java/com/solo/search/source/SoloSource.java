package com.solo.search.source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchConfig;
import com.solo.search.util.SharedPreferencesHelper;

public class SoloSource extends SearchSource {

	private static final String REPLACED_CHARS = "%s";

	private Context mContext;

	public SoloSource(Context context) {
		super(context);
		mContext = context;
	}

	private String getDefaultSearchUrl(String keyWord) {
		String baseUrl = mContext.getResources().getString(
				ResourceUtil.getStringId(mContext, "ssearch_solo_search_default_base"), keyWord);
		return baseUrl;
	}

	@Override
	public String getSearchUrl(String keyword) {
		String baseUrl = SharedPreferencesHelper.getString(mContext, SearchConfig.KEY_SOLO_SEARCH_ENGINE_URL,
				getDefaultSearchUrl(keyword));
		return baseUrl.replace(REPLACED_CHARS, keyword);
	}

	private boolean isYandexSearchEngine() {
		String baseUrl = SharedPreferencesHelper.getString(mContext, SearchConfig.KEY_SOLO_SEARCH_ENGINE_URL, "");
		return baseUrl.startsWith(SearchConfig.URL_YANDEX_SEARCH_PREFIX);
	}

	@Override
	public String getSuggestionUrl(String keyWord) {
		String url = null;
		// http://suggest.yandex.ru/suggest-ya.cgi?part=%input%&lr=213&n=5&mob=1&srv=sololauncher
		if (isYandexSearchEngine()) {
			url = mContext.getResources().getString(ResourceUtil.getStringId(mContext, "ssearch_suggest_yandex_base"), keyWord);
		} else {
			String hl = Locale.getDefault().getLanguage();
			try {
				url = mContext.getResources().getString(ResourceUtil.getStringId(mContext, "ssearch_suggest_google_base"), hl,
						URLEncoder.encode(keyWord, "UTF-8"));
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return url;
	}

	@Override
	public String[] getSuggestions(String resultString) {
		// 解析Yandex的建议词
		// 搜索建议词的返回数据格式：suggest.apply(["solo",["solomon","solomon обувь официальный сайт"],[]],[])。
		if (isYandexSearchEngine()) {
			if (resultString.startsWith(SearchConfig.URL_YANDEX_SEARCH_SUGESS_PREFIX)
					&& resultString.endsWith(SearchConfig.URL_YANDEX_SEARCH_SUGESS_SUFFIX)) {
				// 截取有效数据
				String arrayStr = resultString.substring(SearchConfig.URL_YANDEX_SEARCH_SUGESS_PREFIX.length(), resultString
						.lastIndexOf(SearchConfig.URL_YANDEX_SEARCH_SUGESS_SUFFIX));
				try {
					JSONArray array = new JSONArray(arrayStr);
					if (array.length() > 1) {
						JSONArray suggessArray = array.getJSONArray(1);
						if (suggessArray != null) {
							String[] suggestions = new String[suggessArray.length()];
							for (int i = 0; i < suggestions.length; i++) {
								suggestions[i] = suggessArray.optString(i);
							}
							return suggestions;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} else {
			try {
				JSONArray array = new JSONArray(resultString);
				String keyWord = array.optString(0);
				if (keyWord != null) {
					JSONArray suggessArray = array.getJSONArray(1);
					if (suggessArray != null) {
						String[] suggestions = new String[suggessArray.length()];
						for (int i = 0; i < suggestions.length; i++) {
							suggestions[i] = suggessArray.optString(i);
						}
						return suggestions;
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String getSearchHomeUrl() {
		return null;
	}

}
