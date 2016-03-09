package com.solo.search.source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

import com.solo.search.util.ResourceUtil;

public class BaiduSource extends SearchSource {

	private static final String SEARCH_ID = "1006443d";
	private static final String SUGGESTION_ID = "1006443c";

	public BaiduSource(Context context) {
		super(context);
	}

	@Override
	public String getSearchHomeUrl() {
		return mContext.getResources().getString(ResourceUtil.getStringId(mContext, "ssearch_home_baidu_base"), SEARCH_ID);
	}

	@Override
	public String getSearchUrl(String keyWord) {
		String url = null;
		try {
			url = mContext.getResources().getString(ResourceUtil.getStringId(mContext, "ssearch_baidu_base"), SEARCH_ID,
					URLEncoder.encode(keyWord, "UTF-8"));
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;
	}

	@Override
	public String getSuggestionUrl(String keyWord) {
		String url = null;
		try {
			url = mContext.getResources().getString(ResourceUtil.getStringId(mContext, "ssearch_suggest_baidu_base"),
					SUGGESTION_ID, URLEncoder.encode(keyWord, "UTF-8"));
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;
	}

	@Override
	public String[] getSuggestions(String resultString) {
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
		return null;
	}

}
