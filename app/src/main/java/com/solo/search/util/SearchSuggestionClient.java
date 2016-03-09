package com.solo.search.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.solo.search.source.SearchSource;

public class SearchSuggestionClient {

	private static final boolean DEBUG = false;
	private static final String TAG = "SearchSuggestionClient";

	private static final String USER_AGENT = "Android/" + Build.VERSION.RELEASE;
	private static final String HTTP_TIMEOUT = "http.conn-manager.timeout";
	private static final int HTTP_CONNECT_TIMEOUT_MILLIS = 4000;

	private final HttpClient mHttpClient;
	private SearchSource mSearchSource;

	public SearchSuggestionClient(Context context, SearchSource suggestionSource) {
		mHttpClient = AndroidHttpClient.newInstance(USER_AGENT, context);
		mSearchSource = suggestionSource;
		HttpParams params = mHttpClient.getParams();
		params.setLongParameter(HTTP_TIMEOUT, HTTP_CONNECT_TIMEOUT_MILLIS);
	}

	private String getSearchSuggestionUri(String keyWord) {
		return mSearchSource.getSuggestionUrl(keyWord);
	}

	public String[] query(String keyWord) {
		if (TextUtils.isEmpty(keyWord)) {
			return null;
		}
		String suggestUri = getSearchSuggestionUri(keyWord);
		if (DEBUG)
			Log.d(TAG, "Sending request:" + suggestUri);
		try {
			HttpGet method = new HttpGet(suggestUri);
			HttpResponse response = mHttpClient.execute(method);
			if (DEBUG) {
				Log.d(TAG, "Receive statusCode:" + response.getStatusLine().getStatusCode());
			}
			if (response.getStatusLine().getStatusCode() == 200) {
				String resultString = EntityUtils.toString(response.getEntity());
				if (DEBUG) {
					Log.d(TAG, "Receive result:" + resultString);
				}
				return mSearchSource.getSuggestions(resultString);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void onDestroy() {
		// mHttpClient.close();
	}
}
