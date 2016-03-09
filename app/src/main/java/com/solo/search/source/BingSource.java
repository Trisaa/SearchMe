package com.solo.search.source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

import com.solo.search.util.ResourceUtil;

public class BingSource extends SearchSource {

	public BingSource(Context context) {
		super(context);
	}

	@Override
	public String getSearchUrl(String keyWord) {
		String url = null;
		try {
			url = mContext.getResources().getString(ResourceUtil.getStringId(mContext, "ssearch_bing_base"),
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
		String hl = Locale.getDefault().getLanguage();
		String url = null;
		try {
			url = mContext.getResources().getString(ResourceUtil.getStringId(mContext, "ssearch_suggest_google_base"), hl,
					URLEncoder.encode(keyWord, "UTF-8"));
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

	@Override
	public String getSearchHomeUrl() {
		return null;
	}

}
