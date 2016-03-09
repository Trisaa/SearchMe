package com.solo.search.source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

import com.solo.search.util.ResourceUtil;

public class DuckDuckGoSource extends SearchSource {

	public DuckDuckGoSource(Context context) {
		super(context);
	}

	@Override
	public String getSearchHomeUrl() {
		return null;
	}

	@Override
	public String getSearchUrl(String keyWord) {
		String url = mContext.getResources().getString(ResourceUtil.getStringId(mContext, "search_duckduckgo_base"), keyWord);
		return url;
	}

	@Override
	public String getSuggestionUrl(String keyWord) {
		String url = null;
		try {
			url = mContext.getResources().getString(ResourceUtil.getStringId(mContext, "search_suggest_duckduckgo_base"),
					URLEncoder.encode(keyWord, "UTF-8"));
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;

	}

	/**
	 * 解析建议词数据： https://duckduckgo.com/ac/?q=solo
	 * [{"phrase":"solomon northup"},{
	 * "phrase":"solomons words for the wise"},{"phrase"
	 * :"solon city schools"},{"phrase"
	 * :"solomon kane"},{"phrase":"solomon grundy"
	 * },{"phrase":"solomon islands"},
	 * {"phrase":"solomon"},{"phrase":"solomon's word"
	 * },{"phrase":"solomon's temple"},{"phrase":"solodyn"}]
	 */
	@Override
	public String[] getSuggestions(String resultString) {

		try {
			JSONArray array = new JSONArray(resultString);
			if (array != null) {
				String[] suggestions = new String[array.length()];
				for (int i = 0; i < suggestions.length; i++) {
					JSONObject phraseObject = array.getJSONObject(i);
					suggestions[i] = phraseObject.getString("phrase");
				}
				return suggestions;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;

	}
}
