package com.solo.search;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.solo.search.card.CardConfig;
import com.solo.search.card.CardFactory;
import com.solo.search.card.CardManager;
import com.solo.search.card.entry.CardEntry;
import com.solo.search.db.CardDBHelper;
import com.solo.search.util.ResourceUtil;
import com.solo.search.widget.Titlebar;
import com.solo.search.widget.Titlebar.OnTitlebarClickListener;
import com.solo.search.widget.dragsortlistview.DragSortController;
import com.solo.search.widget.dragsortlistview.DragSortListView;
import com.solo.search.widget.dragsortlistview.DragSortListView.DragListener;
import com.solo.search.widget.dragsortlistview.DragSortListView.DropListener;

public class CardManageActivity extends Activity implements OnItemClickListener, OnTitlebarClickListener {

	private static final int MSG_LOAD_CARD_SUCC = 0;
	private static final int MSG_LOAD_CARD_FAILED = 1;

	private Titlebar mTitlebar;
	private LinearLayout mCardContainer;
	private DragSortListView mListView;
	private CardAdapter mCardAdapter;

	private ArrayList<CardEntry> mCardEntries;
	private boolean isCardDataChanged = false;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_LOAD_CARD_SUCC:
				mCardAdapter = new CardAdapter(CardManageActivity.this);
				mListView.setAdapter(mCardAdapter);
				mCardContainer.setVisibility(View.VISIBLE);
				break;
			case MSG_LOAD_CARD_FAILED:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(ResourceUtil.getLayoutId(this, "ssearch_activity_card_manage"));
		mTitlebar = (Titlebar) findViewById(ResourceUtil.getId(this, "titlebar"));
		mCardContainer = (LinearLayout) findViewById(ResourceUtil.getId(this, "card_container"));
		mListView = (DragSortListView) findViewById(ResourceUtil.getId(this, "listview"));

		mCardEntries = new ArrayList<CardEntry>();

		DragSortController controller = new DragSortController(mListView);
		controller.setDragInitMode(DragSortController.ON_LONG_PRESS);
		controller.setSortEnabled(true);

		mTitlebar.setTitle(getString(ResourceUtil.getStringId(this, "ssearch_card_edit")));
		mTitlebar.setOnTitlebarClickListener(this);

		mListView.setFloatViewManager(controller);
		mListView.setOnItemClickListener(this);
		mListView.setDragListener(new DragListener() {

			@Override
			public void drag(int from, int to) {
				Collections.swap(mCardEntries, from, to);
			}
		});

		mListView.setDropListener(new DropListener() {

			@Override
			public void drop(int from, int to) {
				for (int i = 0; i < mCardEntries.size(); i++) {
					// Ads的order为0，确保在第一个位置, 热词order为100，确保在100。
					int order = (i + CardConfig.CARDS_NOT_EDITABLE.length) * 100;
					mCardEntries.get(i).setCardOrder(order);
					CardManager.getInstance(CardManageActivity.this).saveCardEntryToDB(mCardEntries.get(i));
				}
				isCardDataChanged = true;
				mCardAdapter.notifyDataSetChanged();
			}
		});

		loadCardsData();

	}

	private void loadCardsData() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Message msg = new Message();
				// ContentResolver resolver =
				// CardManageActivity.this.getContentResolver();
				// Cursor cursor = resolver.query(CardDBHelper.CARD_URI,
				// CardConfig.CARD_DB_PROJECTION, null, null,
				// CardConfig.CARD_ORDER + " ASC");
				CardDBHelper dbHelper = new CardDBHelper(CardManageActivity.this);
				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = db.query(CardDBHelper.TABLE_NAME, CardConfig.CARD_DB_PROJECTION, null, null, null, null,
						CardConfig.CARD_ORDER + " ASC");

				try {
					while (cursor.moveToNext()) {
						String cardId = cursor.getString(cursor.getColumnIndexOrThrow(CardConfig.CARD_ID));

						// 广告和热词卡片不可编辑。
						if (CardConfig.isEditableCard(cardId)) {
							CardEntry cardEntry = CardFactory.makeCardEntry(CardManageActivity.this, cardId, cursor);
							mCardEntries.add(cardEntry);
						}
					}
					msg.what = MSG_LOAD_CARD_SUCC;
				} catch (Exception ex) {
					msg.what = MSG_LOAD_CARD_FAILED;
				} finally {
					cursor.close();
				}
				db.close();
				mHandler.sendMessage(msg);
			}
		}).start();
	}

	@Override
	public void onBackPressed() {
		if (isCardDataChanged) {
			setResult(RESULT_OK);
		}
		finish();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	}

	class CardAdapter extends BaseAdapter {

		private Context mContext;

		public CardAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			return mCardEntries.size();
		}

		@Override
		public Object getItem(int position) {
			return mCardEntries.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(mContext).inflate(
					ResourceUtil.getLayoutId(CardManageActivity.this, "ssearch_card_manage_item"), null);

			TextView titleTV = (TextView) convertView.findViewById(ResourceUtil.getId(CardManageActivity.this, "title"));
			final Switch switchButton = (Switch) convertView.findViewById(ResourceUtil.getId(CardManageActivity.this,
					"switch_button"));
			ImageView dragHandle = (ImageView) convertView.findViewById(ResourceUtil.getId(CardManageActivity.this,
					"drag_handle"));

			CardEntry cardEntry = mCardEntries.get(position);
			titleTV.setText(cardEntry.getCardTitle());

			switchButton.setVisibility(View.VISIBLE);
			dragHandle.setVisibility(View.VISIBLE);

			boolean isChecked = (cardEntry.getCardEnable() == CardConfig.CARD_STATE_ENABLE);
			if (switchButton.isChecked() != isChecked) {
				switchButton.setChecked(isChecked);
			}

			switchButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					boolean isChecked = switchButton.isChecked();
					isCardDataChanged = true;
					mCardEntries.get(position).setCardEnable(
							isChecked ? CardConfig.CARD_STATE_ENABLE : CardConfig.CARD_STATE_DISABLE);
					CardManager.getInstance(mContext).saveCardEntryToDB(mCardEntries.get(position));

				}
			});

			return convertView;
		}
	}

	@Override
	public void onBackButtonClick() {
		onBackPressed();
	}

	@Override
	public void onRightButtonClick() {
	}

}
