package com.solo.search;


import java.util.ArrayList;

import com.solo.search.suggestion.Suggestion;
import com.solo.search.suggestion.Suggestions;

import android.app.Activity;

public abstract class BaseSearchActivity extends Activity {

	public abstract void onSuggestionLaunch(Suggestion suggestion);

	public abstract void setProgressBarIndeterminateVisibility();

	public abstract ArrayList<Suggestions> getSuggestionsSource();

	public abstract void setSearchHistoryVisible(boolean visible);

}
