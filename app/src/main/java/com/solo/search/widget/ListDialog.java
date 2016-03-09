package com.solo.search.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.solo.search.util.ResourceUtil;
import com.solo.search.util.ViewHolder;

public class ListDialog extends AlertDialog {

	private static final int INVALID_ITEM = -1;

	private Context mContext;
	private String mTitle;
	private CharSequence[] mEntries;
	private CharSequence[] mEntryValues;
	private int[] mEntryIcons;
	private int mCheckedItem = INVALID_ITEM;
	private OnItemClickListener mItemClickListener;

	public ListDialog(Context context, String title, CharSequence[] entries, CharSequence[] entryValues, int[] entryIcons,
			int checkedItem) {
		super(context);
		mContext = context;
		mTitle = title;
		mEntries = entries;
		mEntryValues = entryValues;
		mEntryIcons = entryIcons;
		mCheckedItem = checkedItem;
	}

	public ListDialog(Context context, String title, CharSequence[] entries, CharSequence[] entryValues, int[] entryIcons) {
		super(context);
		mContext = context;
		mTitle = title;
		mEntries = entries;
		mEntryValues = entryValues;
		mEntryIcons = entryIcons;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ResourceUtil.getLayoutId(getContext(), "ssearch_preference_dialog_list"));

		setupView();
	}

	private void setupView() {
		TextView titleTV = (TextView) findViewById(ResourceUtil.getId(getContext(), "title"));
		if (TextUtils.isEmpty(mTitle)) {
			titleTV.setVisibility(View.GONE);
		} else {
			titleTV.setText(mTitle);
		}

		ListView list = (ListView) findViewById(ResourceUtil.getId(getContext(), "listview"));
		final ListAdapter adapter = new ListAdapter(mContext, mEntries, mEntryValues, mEntryIcons);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mItemClickListener != null) {
					mItemClickListener.onItemClick(parent, view, position, id);
				}
				dismiss();
			}
		});
	}

	public void setItemChecked(String itemValue) {
		if (!TextUtils.isEmpty(itemValue)) {
			for (int i = 0; i < mEntryValues.length; i++) {
				if (itemValue.equals(mEntryValues[i])) {
					mCheckedItem = i;
					break;
				}
			}
		} else {
			mCheckedItem = INVALID_ITEM;
		}
	}

	public void setItemChecked(int item) {
		mCheckedItem = item;
	}

	public void setOnItemClickListener(OnItemClickListener itemClickListener) {
		mItemClickListener = itemClickListener;
	}

	@Override
	public void show() {
		super.show();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}

	class ListAdapter extends BaseAdapter {

		private Context mContext;
		private CharSequence[] mEntries;
		private CharSequence[] mEntryValues;
		private int[] mEntryIcons = null;

		public ListAdapter(Context context, CharSequence[] entries, CharSequence[] entryValues, int[] entryIcons) {
			mContext = context;
			mEntries = entries;
			mEntryValues = entries;
			mEntryIcons = entryIcons;
		}

		@Override
		public int getCount() {
			return mEntries.length;
		}

		@Override
		public Object getItem(int position) {
			return mEntries[position];
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(
						ResourceUtil.getLayoutId(getContext(), "ssearch_list_item_radiobutton"), null);
				viewHolder.icon = (ImageView) convertView.findViewById(ResourceUtil.getId(getContext(), "icon"));
				viewHolder.title = (TextView) convertView.findViewById(ResourceUtil.getId(getContext(), "title"));
				viewHolder.summary = (TextView) convertView.findViewById(ResourceUtil.getId(getContext(), "summary"));
				viewHolder.radioButton = (RadioButton) convertView
						.findViewById(ResourceUtil.getId(getContext(), "radioButton"));
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.title.setText(mEntries[position]);
			viewHolder.summary.setText(mEntryValues[position]);

			viewHolder.radioButton.setFocusable(false);
			viewHolder.radioButton.setClickable(false);

			if (mCheckedItem == INVALID_ITEM) {
				viewHolder.radioButton.setVisibility(View.GONE);
			} else {
				viewHolder.radioButton.setVisibility(View.VISIBLE);
				if (mCheckedItem == position) {
					viewHolder.radioButton.setChecked(true);
				} else {
					viewHolder.radioButton.setChecked(false);
				}
			}

			if (mEntryIcons != null && position < mEntryIcons.length) {
				Drawable icon = ContextCompat.getDrawable(mContext, mEntryIcons[position]);

				if (icon != null) {
					viewHolder.icon.setVisibility(View.VISIBLE);
					viewHolder.icon.setImageDrawable(icon);
				} else {
					viewHolder.icon.setVisibility(View.GONE);
				}
			} else {
				viewHolder.icon.setVisibility(View.GONE);
			}

			return convertView;
		}

	}
}
