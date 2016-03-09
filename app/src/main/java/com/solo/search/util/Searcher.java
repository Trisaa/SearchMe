package com.solo.search.util;


import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.solo.search.suggestion.Suggestion;
import com.solo.search.suggestion.Suggestions;

import android.os.Handler;
import android.os.Message;

public class Searcher extends Handler {

	private static final String TAG = LogUtils.makeLogTag(Searcher.class);

	private static final int MSG_FINISH_SUGGESTIONS = 0;
	private static final int MAX_SUGGESTIONS_PER_QUERY = 20;

	private static final int MIN_CORE_POOL_SIZE = 1;
	private static final int MAX_POOL_SIZE = 6;
	private static final int MAX_QUEUE_SIZE = 14;
	private static final int KEEP_ALIVE = 10;

	private String mSearchText;
	private int mSearchPatternLevel;
	private int mOffset;
	private int mSuggestionsSourceNum;

	private final Suggestions mSuggestionsSource;
	private BaseSearchAdapter mSearchAdapter;
	private AsyncSearcher mAsyncSearcher;
	private static final AtomicInteger sSearcherNums = new AtomicInteger(0);// 搜索操作个数
	private static final BlockingQueue<Runnable> sWorkQueue = new ArrayBlockingQueue<Runnable>(MAX_QUEUE_SIZE);

	private static final ThreadFactory sThreadFactory = new ThreadFactory() {

		private final AtomicInteger mCount = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable runnable) {
			String threadName = "SoloSearcher-" + mCount.getAndIncrement();
			Thread thread = new Thread(runnable, threadName);

			LogUtils.d(TAG, TAG + "	newThread threadName:" + threadName);
			return thread;
		}
	};
	private static final ThreadPoolExecutor sExecutor = new ThreadPoolExecutor(MIN_CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE,
			TimeUnit.SECONDS, sWorkQueue, sThreadFactory, new ThreadPoolExecutor.DiscardOldestPolicy());

	private ArrayList<Suggestion> mSuggestions = new ArrayList<Suggestion>();

	@Override
	public void handleMessage(Message msg) {
		SearchResult searchResult = (SearchResult) msg.obj;
		LogUtils.d(TAG, TAG + " handleMessage what:" + msg.what);
		if (msg.what == MSG_FINISH_SUGGESTIONS) {
			if (searchResult.searchText != null && searchResult.searchText.equals(mSearchText)) {
				mSuggestions.addAll(searchResult.suggestions);
				int numSuggestions = (searchResult.suggestions != null) ? searchResult.suggestions.size() : 0;
				mSuggestionsSourceNum += numSuggestions;
				if (numSuggestions < MAX_SUGGESTIONS_PER_QUERY) {
					mSearchPatternLevel = SearchPatternLevel.next(mSearchPatternLevel);
					mOffset = 0;
				} else {
					mOffset += numSuggestions;
				}

				// 若没有搜索完毕，则继续搜索
				boolean done = (mSearchPatternLevel < mSuggestionsSource.getSearchPatternLevel() || mSuggestionsSourceNum >= mSuggestionsSource
						.getMaxSuggestions());
				LogUtils.d(TAG, TAG + " handleMessage done:" + done + " mSearchPatternLevel:" + mSearchPatternLevel
						+ " getSearchPatternLevel:" + mSuggestionsSource.getSearchPatternLevel() + " searchResult.searchText:"
						+ searchResult.searchText + " name:" + mSuggestionsSource.getName() + " mSuggestions:"
						+ mSuggestions.size() + " searchResult.suggestions:" + searchResult.suggestions.size());
				if (!done) {
					doSearch();
				} else {
					if (mSearchAdapter != null) {
						mSearchAdapter.addSuggestions(mSuggestionsSource, searchResult.searchText,
								searchResult.searchPatternLevel, mSuggestions);
						mSuggestions.clear();
						mSearchAdapter.onDone();
					}
				}
			}
		}
	}

	public Searcher(Suggestions suggestions, BaseSearchAdapter searchAdapter) {
		mSuggestionsSource = suggestions;
		mSearchAdapter = searchAdapter;
		LogUtils.d(TAG, TAG + "	name:" + suggestions.getName() + " maxSuggestions:" + suggestions.getMaxSuggestions()
				+ " searchPatternLevel:" + suggestions.getSearchPatternLevel());
		int searchNums = sSearcherNums.incrementAndGet();
		setThreadPoolSize(searchNums);
	}

	private void setThreadPoolSize(int size) {
		int corePoolSize = Math.max(MIN_CORE_POOL_SIZE, Math.min(MAX_POOL_SIZE, size));
		LogUtils.d(TAG, TAG + "	setThreadPoolSize:" + corePoolSize);
		sExecutor.setCorePoolSize(corePoolSize);
	}

	public void cancelSearcher() {
		mSearchText = null;
		if (mSuggestions != null) {
			mSuggestions.clear();
		}
		if (mAsyncSearcher != null) {
			sExecutor.remove(mAsyncSearcher);
			mAsyncSearcher.cancel();
			mAsyncSearcher = null;
		}
	}

	private void doSearch() {
		LogUtils.d(TAG, TAG + "	doSearch activeCount:" + sExecutor.getActiveCount() + " corePoolSize:"
				+ sExecutor.getCorePoolSize());
		mAsyncSearcher = new AsyncSearcher(mSearchText, mSearchPatternLevel, mOffset, mSuggestionsSource.getMaxSuggestions()
				- mSuggestionsSourceNum);
		sExecutor.execute(mAsyncSearcher);
	}

	public void search(final String searchText) {
		LogUtils.d(TAG, TAG + "	search:" + searchText);
		cancelSearcher();
		mSearchText = searchText;
		mSuggestionsSourceNum = 0;
		mSearchPatternLevel = SearchPatternLevel.SEARCH_STARTS_WITH_TEXT;
		mOffset = 0;
		doSearch();
	}

	public void destroy() {
		int searcherNums = sSearcherNums.decrementAndGet();
		setThreadPoolSize(searcherNums);
		cancelSearcher();
		// sExecutor.execute(new Runnable() {
		//
		// @Override
		// public void run() {
		// }
		// });
	}

	private class AsyncSearcher implements Runnable {

		private String mSearchText;
		private int mSearchPatternLevel;
		private int mOffset;
		private int mLimit;
		private SearchResult mSearchResult;

		public AsyncSearcher(String searchText, int searchPatternLevel, int offset, int limit) {
			mSearchResult = new SearchResult();
			mSearchText = searchText;
			mSearchPatternLevel = searchPatternLevel;
			mOffset = offset;
			mLimit = limit;
			mSearchResult.searchText = searchText;
			mSearchResult.searchPatternLevel = searchPatternLevel;
			LogUtils.d(TAG, "Construct AsyncSearcher...searchText:" + mSearchText + " mSearchPatternLevel:"
					+ mSearchPatternLevel + " mOffset:" + mOffset + " mLimit:" + mLimit);
		}

		public void cancel() {
			mSearchResult.searchText = null;
		}

		@Override
		public void run() {
			LogUtils.d(TAG, " searching...." + mSearchText);
			mSearchResult.suggestions = mSuggestionsSource.getSuggestions(mSearchText, mSearchPatternLevel, mOffset, mLimit);
			Message msg = obtainMessage();
			msg.what = MSG_FINISH_SUGGESTIONS;
			msg.obj = mSearchResult;
			msg.sendToTarget();
		}

	}

	private class SearchResult {
		public String searchText;
		public int searchPatternLevel;
		public ArrayList<Suggestion> suggestions;

	}

}
