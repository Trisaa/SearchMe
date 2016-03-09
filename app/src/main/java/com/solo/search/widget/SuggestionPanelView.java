package com.solo.search.widget;

import java.util.ArrayList;

import android.content.Context;
import android.provider.MediaStore.Audio.ArtistColumns;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.solo.search.BaseSearchActivity;
import com.solo.search.base.OnAppLaunchListener;
import com.solo.search.suggestion.AlbumSuggestion;
import com.solo.search.suggestion.BookmarkSuggestion;
import com.solo.search.suggestion.ContactSuggestion;
import com.solo.search.suggestion.MessageSuggestion;
import com.solo.search.suggestion.MusicSuggestion;
import com.solo.search.suggestion.Suggestion;
import com.solo.search.suggestion.WebSuggestion;
import com.solo.search.util.ResourceUtil;
import com.solo.search.util.ViewHolder;

public class SuggestionPanelView extends LinearLayout implements OnClickListener {

	private BaseSearchActivity mSearchActivity;

	private LinearLayout mAppsResultLayout;
	private InnerScrollGridView mAppsGridView;
	private AppsAdapter mAppsAdapter;
	private LinearLayout mMoreAppsBtn;
	private LinearLayout mSearchResultContainer;

	private SuggestionsView mContactSuggestionsView;
	private SuggestionsView mSMSSuggestionsView;
	private SuggestionsView mMusicSuggestionsView;
	private SuggestionsView mBookmarkSuggestionView;
	private SuggestionsView mWebSuggestionsView;

	private OnAppLaunchListener mAppLaunchListener;

	public SuggestionPanelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SuggestionPanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SuggestionPanelView(Context context) {
		super(context);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initialize(getContext());
	}

	private void initialize(Context context) {
		View.inflate(context, ResourceUtil.getLayoutId(getContext(), "ssearch_result_layout"), this);

		mAppsResultLayout = (LinearLayout) findViewById(ResourceUtil.getId(getContext(), "app_result_layout"));
		mAppsGridView = (InnerScrollGridView) findViewById(ResourceUtil.getId(getContext(), "gridview"));
		mMoreAppsBtn = (LinearLayout) findViewById(ResourceUtil.getId(getContext(), "app_search_more_item"));
		mSearchResultContainer = (LinearLayout) findViewById(ResourceUtil.getId(getContext(), "search_result_container"));
		mContactSuggestionsView = new SuggestionsView(context, context.getResources().getString(
				ResourceUtil.getStringId(getContext(), "ssearch_contact")));
		mSMSSuggestionsView = new SuggestionsView(context, context.getResources().getString(
				ResourceUtil.getStringId(getContext(), "ssearch_message")));
		mMusicSuggestionsView = new SuggestionsView(context, context.getResources().getString(
				ResourceUtil.getStringId(getContext(), "ssearch_music")));
		mBookmarkSuggestionView = new SuggestionsView(context, context.getResources().getString(
				ResourceUtil.getStringId(getContext(), "ssearch_bookmark")));
		mWebSuggestionsView = new SuggestionsView(context, context.getResources().getString(
				ResourceUtil.getStringId(getContext(), "ssearch_in_webpage")));

		mSearchResultContainer.addView(mContactSuggestionsView);
		mSearchResultContainer.addView(mSMSSuggestionsView);
		mSearchResultContainer.addView(mMusicSuggestionsView);
		mSearchResultContainer.addView(mBookmarkSuggestionView);
		mSearchResultContainer.addView(mWebSuggestionsView);

		hideView();
		setListener();

	}

	private void setListener() {
		mMoreAppsBtn.setOnClickListener(this);
		mAppsGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mAppsAdapter != null) {
					Suggestion suggestion = mAppsAdapter.getItem(position);
					if (suggestion != null) {
						mAppsAdapter.getItem(position).launch();
						mSearchActivity.onSuggestionLaunch(suggestion);
						if (mAppLaunchListener != null) {
							mAppLaunchListener.onAppLaunch();
						}
					}
				}
			}
		});
	}

	public void setOnAppLaunchListener(OnAppLaunchListener listener) {
		mAppLaunchListener = listener;
	}

	public void setSearchActivity(BaseSearchActivity searchActivity) {
		mSearchActivity = searchActivity;
		mContactSuggestionsView.setSearchActivity(searchActivity);
		mSMSSuggestionsView.setSearchActivity(searchActivity);
		mMusicSuggestionsView.setSearchActivity(searchActivity);
		mBookmarkSuggestionView.setSearchActivity(searchActivity);
		mWebSuggestionsView.setSearchActivity(searchActivity);
	}

	public void addSuggestion(Suggestion suggestion) {
		if (suggestion instanceof ContactSuggestion) {
			if (mContactSuggestionsView.getVisibility() == View.GONE) {
				mContactSuggestionsView.setVisibility(View.VISIBLE);
			}
			mContactSuggestionsView.addSuggestion(suggestion);
		} else if (suggestion instanceof MessageSuggestion) {
			if (mSMSSuggestionsView.getVisibility() == View.GONE) {
				mSMSSuggestionsView.setVisibility(View.VISIBLE);
			}
			mSMSSuggestionsView.addSuggestion(suggestion);
		} else if (suggestion instanceof MusicSuggestion || suggestion instanceof AlbumSuggestion
				|| suggestion instanceof ArtistColumns) {
			if (mMusicSuggestionsView.getVisibility() == View.GONE) {
				mMusicSuggestionsView.setVisibility(View.VISIBLE);
			}
			mMusicSuggestionsView.addSuggestion(suggestion);
		} else if (suggestion instanceof BookmarkSuggestion) {
			if (mBookmarkSuggestionView.getVisibility() == View.GONE) {
				mBookmarkSuggestionView.setVisibility(View.VISIBLE);
			}
			mBookmarkSuggestionView.addSuggestion(suggestion);
		} else if (suggestion instanceof WebSuggestion) {
			if (mWebSuggestionsView.getVisibility() == View.GONE) {
				mWebSuggestionsView.setVisibility(View.VISIBLE);
			}
			mWebSuggestionsView.addSuggestion(suggestion);
		}
	}

	public void addSuggestions(String searchText, ArrayList<Suggestion> appSuggestions, ArrayList<Suggestion> suggestions) {
		clear();
		addAppSuggestions(searchText, appSuggestions);
		for (Suggestion suggestion : suggestions) {
			addSuggestion(suggestion);
		}
	}

	private void addAppSuggestions(String searchText, ArrayList<Suggestion> suggestions) {
		if (mAppsAdapter == null) {
			mAppsAdapter = new AppsAdapter(getContext(), suggestions);
			mAppsGridView.setAdapter(mAppsAdapter);
			mAppsAdapter.notifyDataSetChanged();
		} else {
			mAppsAdapter.setAppSuggestions(suggestions);
		}

		if (suggestions == null || suggestions.size() == 0) {
			mAppsResultLayout.setVisibility(View.GONE);
		} else {
			mAppsResultLayout.setVisibility(View.VISIBLE);
		}
		setupShowMoreAppsLayout();
	}

	public void hideView() {
		mAppsResultLayout.setVisibility(View.GONE);
		mSMSSuggestionsView.setVisibility(View.GONE);
		mContactSuggestionsView.setVisibility(View.GONE);
		mMusicSuggestionsView.setVisibility(View.GONE);
		mBookmarkSuggestionView.setVisibility(View.GONE);
		mWebSuggestionsView.setVisibility(View.GONE);
	}

	public void clear() {
		mContactSuggestionsView.clear();
		mSMSSuggestionsView.clear();
		mMusicSuggestionsView.clear();
		mBookmarkSuggestionView.clear();
		mWebSuggestionsView.clear();
		if (mAppsAdapter != null) {
			mAppsAdapter = null;
		}
		mAppsGridView.setAdapter(null);
		hideView();
	}

	private void checkVisibility(SuggestionsView view) {
		if (view.getSuggestionsCount() == 0) {
			view.setVisibility(View.GONE);
		} else {
			view.setVisibility(View.VISIBLE);
		}
	}

	public void onDone() {
		checkVisibility(mContactSuggestionsView);
		checkVisibility(mSMSSuggestionsView);
		checkVisibility(mMusicSuggestionsView);
		checkVisibility(mBookmarkSuggestionView);
		checkVisibility(mWebSuggestionsView);
	}

	private void setupShowMoreAppsLayout() {
		boolean canIncrease = mAppsAdapter.canIncreaseRows();
		if (canIncrease) {
			mMoreAppsBtn.setVisibility(View.VISIBLE);
		} else {
			mMoreAppsBtn.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == ResourceUtil.getId(getContext(), "app_search_more_item"))
			if (mAppsAdapter != null) {
				mAppsAdapter.increaseRows();
				setupShowMoreAppsLayout();
				if (mAppLaunchListener != null) {
					mAppLaunchListener.onAppLaunch();
				}
			}
	}
}

class AppsAdapter extends BaseAdapter {

	public static final int COLUMNS = 5;
	private static final int INITIALIZED_ROWS = 2;
	private static final int INCREASED_ROWS = 4;
	private int mCurrentViewableCount;
	private Context mContext;
	private ArrayList<Suggestion> mAppSuggestions;

	public AppsAdapter(Context context, ArrayList<Suggestion> appSuggestions) {
		mContext = context;
		mAppSuggestions = appSuggestions;
		mCurrentViewableCount = Math.min(mAppSuggestions.size(), COLUMNS * INITIALIZED_ROWS);
	}

	public int getCurrentViewableCount() {
		return mCurrentViewableCount;
	}

	public void setAppSuggestions(ArrayList<Suggestion> appSuggestions) {
		setAppSuggestions(appSuggestions, COLUMNS * INITIALIZED_ROWS);
	}

	public void setAppSuggestions(ArrayList<Suggestion> appSuggestions, int viewableCount) {
		mAppSuggestions = appSuggestions;
		mCurrentViewableCount = Math.min(mAppSuggestions.size(), viewableCount);
		notifyDataSetChanged();
	}

	public boolean canIncreaseRows() {
		return mCurrentViewableCount < mAppSuggestions.size();
	}

	public void increaseRows() {
		if (canIncreaseRows()) {
			int increasedCount = Math.min(mAppSuggestions.size() - mCurrentViewableCount, COLUMNS * INCREASED_ROWS);
			mCurrentViewableCount += increasedCount;
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		return mCurrentViewableCount;
	}

	@Override
	public Suggestion getItem(int position) {
		if (position < mAppSuggestions.size()) {
			return mAppSuggestions.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(ResourceUtil.getLayoutId(mContext, "ssearch_app_list_item"),
					null);
			viewHolder = new ViewHolder();
			viewHolder.icon = (ImageView) convertView.findViewById(ResourceUtil.getId(mContext, "app_icon"));
			viewHolder.title = (TextView) convertView.findViewById(ResourceUtil.getId(mContext, "app_title"));
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.icon.setImageDrawable(mAppSuggestions.get(position).getIcon());
		viewHolder.title.setText(mAppSuggestions.get(position).getTitle());

		return convertView;
	}

}
