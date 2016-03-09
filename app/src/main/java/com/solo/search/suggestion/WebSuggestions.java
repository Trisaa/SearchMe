package com.solo.search.suggestion;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;

import com.solo.search.source.SearchSource;
import com.solo.search.util.AppLauncher;
import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchSuggestionClient;

public class WebSuggestions extends Suggestions {

	private static final String NAME = "WebSuggestions";

	private Context mContext;
	private SearchSuggestionClient mSearchClient;
	private SearchSource mSuggestionSource;
	private Drawable mIcon;

	public WebSuggestions(Context context, SearchSource suggestionSource) {
		mContext = context;
		mSearchClient = new SearchSuggestionClient(context, suggestionSource);
		mSuggestionSource = suggestionSource;
		mIcon = ContextCompat.getDrawable(mContext, ResourceUtil.getDrawableId(context, "ssearch_web"));
	}

	@Override
	public String getName() {
		return NAME;
	}

	@SuppressWarnings("deprecation")
	@Override
	public Intent getIntent(Suggestion suggestion) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(((WebSuggestion) suggestion).getUrl()));
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		return intent;
	}

	@Override
	public boolean launch(Suggestion suggestion) {
		if (suggestion instanceof WebSuggestion) {
			AppLauncher.launchSearch(mContext, suggestion.getTitle());
		}
		return true;
	}

	@Override
	public Suggestion getSuggestion(int id) {
		return null;
	}

	@Override
	public ArrayList<Suggestion> getSuggestions(String searchText, int searchPatternLevel, int offset, int limit) {
		ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
		String[] suggestionArray = mSearchClient.query(searchText);
		if (suggestionArray != null) {
			for (String suggest : suggestionArray) {
				WebSuggestion webSuggestion = new WebSuggestion(this, 11, suggest, mSuggestionSource.getSearchUrl(suggest));
				suggestions.add(webSuggestion);
			}
		}
		return suggestions;
	}

	public Drawable getIcon() {
		return mIcon;
	}

	public boolean unregisterContentObserver(ContentObserver observer) {
		mSearchClient.onDestroy();
		return false;
	}
}
