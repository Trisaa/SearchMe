package com.solo.search.util;


import java.util.ArrayList;

import com.solo.search.suggestion.Suggestion;
import com.solo.search.suggestion.Suggestions;

public abstract class BaseSearchAdapter {

	public abstract void addSuggestions(Suggestions suggestions,
			String searchText, int searchPatternLevel,
			ArrayList<Suggestion> suggestionsList);

	public abstract void onDone();

	public abstract void onDestroy();
}
