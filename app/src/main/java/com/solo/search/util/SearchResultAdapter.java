package com.solo.search.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import com.solo.search.BaseSearchActivity;
import com.solo.search.suggestion.AppSuggestion;
import com.solo.search.suggestion.Suggestion;
import com.solo.search.suggestion.Suggestions;
import com.solo.search.widget.SuggestionPanelView;

import android.database.ContentObserver;
import android.os.Handler;
import android.view.View;

public class SearchResultAdapter extends BaseSearchAdapter {

	private final SuggestionPanelView mSuggesPanelView;
	private final BaseSearchActivity mSearchActivity;
	private final ArrayList<Searcher> mSearchers;
	private final ArrayList<Suggestions> mSuggestionsSource;
	private LinkedList<Suggestion> mSuggestions;
	private ArrayList<Suggestion> mViewableAppSuggestions;
	private ArrayList<Suggestion> mViewableSuggestions;
	private final HashMap<Integer, Integer> mSuggestionsIndexes;
	private final int mSuggestionsSourceNum;
	private final SuggestionsObserver mSuggestionsObserver;
	private int[] mSearchResultInsertPositions;
	private String mSearchText = null;
	private boolean mClearSuggestions = false;
	private int mDoneSearchersNum = 0;

	public SearchResultAdapter(BaseSearchActivity searchActivity, SuggestionPanelView container) {
		mSearchActivity = searchActivity;
		mSuggesPanelView = container;

		mSearchers = new ArrayList<Searcher>();
		mSuggestionsSource = mSearchActivity.getSuggestionsSource();
		mSuggestionsSourceNum = mSuggestionsSource.size();
		mSuggestionsIndexes = new HashMap<Integer, Integer>(mSuggestionsSourceNum);
		mSuggestionsObserver = new SuggestionsObserver(new Handler());

		for (int i = 0; i < mSuggestionsSourceNum; i++) {
			mSuggestionsIndexes.put(mSuggestionsSource.get(i).getId(), i);
			mSuggestionsSource.get(i).registerContentObserver(mSuggestionsObserver);
			mSearchers.add(new Searcher(mSuggestionsSource.get(i), this));
		}

		mSuggestions = new LinkedList<Suggestion>();
		mViewableAppSuggestions = new ArrayList<Suggestion>();
		mViewableSuggestions = new ArrayList<Suggestion>();
		mSearchResultInsertPositions = new int[mSuggestionsSourceNum * SearchPatternLevel.SEARCH_LEVEL_NUM];
	}

	public void onDestroy() {
		for (int i = 0; i < mSuggestionsSourceNum; i++) {
			mSuggestionsSource.get(i).unregisterContentObserver(mSuggestionsObserver);
			mSearchers.get(i).cancelSearcher();
			mSearchers.get(i).destroy();
		}
	}

	public int getCount() {
		return mViewableSuggestions.size();
	}

	public Object getItem(int position) {
		if (position < mViewableSuggestions.size()) {
			return mViewableSuggestions.get(position);
		} else {
			return null;
		}
	}

	public long getItemId(int position) {
		if (position < mViewableSuggestions.size()) {
			return mViewableSuggestions.get(position).getId();
		} else {
			return -1;
		}
	}

	public final void search(final String searchText) {
		resetSearchResultPositions();
		mSearchText = (searchText == null ? null : searchText.toLowerCase());
		if (mSearchText != null && mSearchText.trim().length() > 0) {
			mSearchText = mSearchText.trim();
			mSearchActivity.setProgressBarIndeterminateVisibility(true);
			mClearSuggestions = true;
			mDoneSearchersNum = 0;
			for (int i = 0; i < mSuggestionsSourceNum; i++) {
				mSearchers.get(i).search(mSearchText);
			}
		} else {
			mSuggestions.clear();
			mViewableAppSuggestions.clear();
			mViewableSuggestions.clear();
			mSuggesPanelView.clear();
			mSearchActivity.setProgressBarIndeterminateVisibility(false);
		}
	}

	public void addSuggestions(Suggestions suggestions, String searchText, int searchPatternLevel,
			ArrayList<Suggestion> suggestionsList) {
		if (searchText == mSearchText && suggestionsList != null
				&& searchPatternLevel >= SearchPatternLevel.SEARCH_CONTAINS_EACH_CHAR
				&& searchPatternLevel <= SearchPatternLevel.SEARCH_STARTS_WITH_TEXT) {
			if (mClearSuggestions) {
				mClearSuggestions = false;
				mSuggestions.clear();
			}

			Integer suggestionIndex = mSuggestionsIndexes.get(suggestions.getId());

			int insertPosition = 0;
			for (int i = 1; i <= (SearchPatternLevel.SEARCH_LEVEL_NUM - searchPatternLevel); i++) {
				insertPosition += mSearchResultInsertPositions[i * mSuggestionsSourceNum - 1];
			}
			insertPosition += mSearchResultInsertPositions[(SearchPatternLevel.SEARCH_LEVEL_NUM - searchPatternLevel)
					* mSuggestionsSourceNum + suggestionIndex];

			mSuggestions.addAll(insertPosition, suggestionsList);
			int pos = (SearchPatternLevel.SEARCH_LEVEL_NUM - searchPatternLevel) * mSuggestionsSourceNum + suggestionIndex;
			for (int i = pos; i < pos + mSuggestionsSourceNum - suggestionIndex; i++) {
				mSearchResultInsertPositions[i] += suggestionsList.size();
			}

			mViewableAppSuggestions.clear();
			mViewableSuggestions.clear();
			ListIterator<Suggestion> itr = mSuggestions.listIterator();
			while (itr.hasNext()) {
				Suggestion suggestion = itr.next();
				if (suggestion != null) {
					if (suggestion instanceof AppSuggestion) {
						mViewableAppSuggestions.add(suggestion);
					} else {
						mViewableSuggestions.add(suggestion);
					}
				}
			}
		}

		mSuggesPanelView.setVisibility(View.VISIBLE);
		mSuggesPanelView.addSuggestions(searchText, mViewableAppSuggestions, mViewableSuggestions);

	}

	public void clear() {
		mSuggesPanelView.setVisibility(View.GONE);
	}

	public void onDone() {
		mDoneSearchersNum++;
		if (mDoneSearchersNum >= mSearchers.size()) {
			if (mClearSuggestions) {
				mClearSuggestions = false;
				mSuggestions.clear();
				mViewableSuggestions.clear();
			}
			mSearchActivity.setProgressBarIndeterminateVisibility(false);
			mSuggesPanelView.onDone();
		}
	}

	public boolean hasSuggestions() {
		return (!mViewableSuggestions.isEmpty());
	}

	private final void resetSearchResultPositions() {
		Arrays.fill(mSearchResultInsertPositions, 0);
	}

	private class SuggestionsObserver extends ContentObserver {

		public SuggestionsObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			if (mSearchText != null && mSearchText.length() > 0) {
				search(mSearchText);
			}
		}
	}

}
