package com.solo.search.suggestion;


import java.util.ArrayList;

import com.solo.search.util.SearchPatternLevel;

import android.content.Intent;
import android.database.ContentObserver;

public abstract class Suggestions {

	private int mId;
	private int mMaxSuggestions;
	private int mSearchPatternLevel;

	public void setMaxSuggestions(int maxSuggesstions) {
		mId = getName().hashCode();
		mMaxSuggestions = maxSuggesstions;
		mSearchPatternLevel = SearchPatternLevel.SEARCH_STARTS_WITH_TEXT;
	}

	public int getId() {
		return mId;
	}

	public int getMaxSuggestions() {
		return mMaxSuggestions;
	}

	public int getSearchPatternLevel() {
		return mSearchPatternLevel;
	}

	public void setSearchPatternLevel(int searchPatternLevel) {
		mSearchPatternLevel = searchPatternLevel;
	}

	public abstract String getName();

	public abstract Intent getIntent(Suggestion suggestion);

	public abstract boolean launch(Suggestion suggestion);

	public abstract Suggestion getSuggestion(int id);

	public abstract ArrayList<Suggestion> getSuggestions(String searchText,
			int searchPatternLevel, int offset, int limit);

	public boolean registerContentObserver(ContentObserver observer) {
		return false;
	}

	public boolean unregisterContentObserver(ContentObserver observer) {
		return false;
	}

}
