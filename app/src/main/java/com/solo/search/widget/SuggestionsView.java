package com.solo.search.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.solo.search.BaseSearchActivity;
import com.solo.search.suggestion.AlbumSuggestion;
import com.solo.search.suggestion.AppSuggestion;
import com.solo.search.suggestion.ArtistSuggestion;
import com.solo.search.suggestion.BookmarkSuggestion;
import com.solo.search.suggestion.ContactSuggestion;
import com.solo.search.suggestion.MessageSuggestion;
import com.solo.search.suggestion.MusicSuggestion;
import com.solo.search.suggestion.Suggestion;
import com.solo.search.suggestion.WebSuggestion;
import com.solo.search.util.ResourceUtil;

public class SuggestionsView extends LinearLayout {

	private enum Style {
		White, Transparent
	}

	private static final int TYPE_APP = 0;
	private static final int TYPE_CONTACT = 1;
	private static final int TYPE_SMS = 2;
	private static final int TYPE_MUSIC = 3;
	private static final int TYPE_BOOKMARK = 4;
	private static final int TYPE_WEB = 5;

	private BaseSearchActivity mSearchActivity;
	private LayoutInflater mInflater;
	private LinearLayout mContainer;

	public SuggestionsView(Context context, String title) {
		super(context);
		init(context, title);
	}

	public SuggestionsView(Context context, Style style, String title) {
		super(context);
		init(context, title);
	}

	private void init(Context context, String title) {
		mInflater = LayoutInflater.from(context);
		setOrientation(VERTICAL);
		mInflater.inflate(ResourceUtil.getLayoutId(getContext(), "ssearch_suggestion_panel"), this);
		TextView titleText = (TextView) findViewById(ResourceUtil.getId(getContext(), "search_panel_title"));
		mContainer = (LinearLayout) findViewById(ResourceUtil.getId(getContext(), "search_panel_container"));

		titleText.setText(title);
	}

	public void setSearchActivity(BaseSearchActivity searchActivity) {
		mSearchActivity = searchActivity;
	}

	public int getSuggestionsCount() {
		return mContainer.getChildCount();
	}

	public void addSuggestion(Suggestion suggestion) {
		if (suggestion != null) {
			View view = getView(suggestion);
			if (mContainer.getChildCount() > 0) {
				View divider = mInflater.inflate(ResourceUtil.getLayoutId(getContext(), "ssearch_divider_horizontal"), null);
				mContainer.addView(divider, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1));
			}
			mContainer.addView(view, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}
	}

	private int getItemType(Suggestion suggestion) {
		int type = TYPE_APP;
		if (suggestion instanceof AppSuggestion) {
			type = TYPE_APP;
		} else if (suggestion instanceof ContactSuggestion) {
			type = TYPE_CONTACT;
		} else if (suggestion instanceof MessageSuggestion) {
			type = TYPE_SMS;
		} else if (suggestion instanceof MusicSuggestion || suggestion instanceof AlbumSuggestion
				|| suggestion instanceof ArtistSuggestion) {
			type = TYPE_MUSIC;
		} else if (suggestion instanceof BookmarkSuggestion) {
			type = TYPE_BOOKMARK;
		} else if (suggestion instanceof WebSuggestion) {
			type = TYPE_WEB;
		}
		return type;
	}

	public View getView(final Suggestion suggestion) {
		int itemType = getItemType(suggestion);
		View convertView = null;
		switch (itemType) {
		case TYPE_CONTACT:
			convertView = mInflater.inflate(ResourceUtil.getLayoutId(getContext(), "ssearch_list_item_contact"), null);

			ImageView sms = (ImageView) convertView.findViewById(ResourceUtil.getId(getContext(),
					"search_list_item_contact_message"));
			sms.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mSearchActivity.onSuggestionLaunch(suggestion);
					((ContactSuggestion) suggestion).smsTo();
				}
			});

			// 直接拨号
			ImageView dial = (ImageView) convertView.findViewById(ResourceUtil.getId(getContext(),
					"search_list_item_contact_dial"));
			dial.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mSearchActivity.onSuggestionLaunch(suggestion);
					((ContactSuggestion) suggestion).dial();
				}
			});

			break;
		case TYPE_SMS:
			convertView = mInflater.inflate(ResourceUtil.getLayoutId(getContext(), "ssearch_list_item_message"), null);
			break;
		case TYPE_MUSIC:
			convertView = mInflater.inflate(ResourceUtil.getLayoutId(getContext(), "ssearch_list_item_music"), null);
			break;
		case TYPE_BOOKMARK:
			convertView = mInflater.inflate(ResourceUtil.getLayoutId(getContext(), "ssearch_list_item_bookmark"), null);
			break;
		case TYPE_WEB:
			convertView = mInflater.inflate(ResourceUtil.getLayoutId(getContext(), "ssearch_list_item_web"), null);
			break;
		}
		ImageView icon = (ImageView) convertView.findViewById(ResourceUtil.getId(getContext(), "search_list_item_icon"));
		TextView title = (TextView) convertView.findViewById(ResourceUtil.getId(getContext(), "search_list_item_title"));
		TextView info = (TextView) convertView.findViewById(ResourceUtil.getId(getContext(), "search_list_item_info"));

		if (suggestion.getIcon() != null) {
			icon.setImageDrawable(suggestion.getIcon().getCurrent());
			icon.setVisibility(View.VISIBLE);
		} else {
			icon.setTag(null);
			icon.setOnClickListener(null);
			icon.setVisibility(View.GONE);
		}
		title.setText(suggestion.getTitle());

		if (info != null) {
			if (suggestion.getInfoText() != null) {
				info.setText(suggestion.getInfoText());
				info.setVisibility(View.VISIBLE);
			} else {
				info.setVisibility(View.GONE);
			}
		}

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				suggestion.launch();
				if (mSearchActivity != null) {
					mSearchActivity.onSuggestionLaunch(suggestion);
				}
			}
		});

		return convertView;
	}

	public void clear() {
		mContainer.removeAllViews();
	}

}
