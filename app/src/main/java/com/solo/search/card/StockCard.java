package com.solo.search.card;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.solo.search.SearchActivity;
import com.solo.search.StockManageActivity;
import com.solo.search.card.entry.CardEntry;
import com.solo.search.card.entry.StockEntry;
import com.solo.search.card.model.CardItem;
import com.solo.search.card.model.StockItem;
import com.solo.search.util.AppLauncher;
import com.solo.search.util.DeviceUtils;
import com.solo.search.util.ResourceUtil;

public class StockCard extends BaseCard {

	public static final int MAX_STOCKS_SIZE = 6;

	private LinearLayout mMainView;
	private LinearLayout mContentContainer;
	private TextView mTitleTv;
	private RelativeLayout mMenuBtn;

	public StockCard(Context paramContext, CardEntry cardEntry) {
		super(paramContext, cardEntry);
	}

	@Override
	public String getCardId() {
		return CardConfig.CARD_ID_STOCK;
	}

	private View buildStockItem(final StockItem item) {
		View view = mInflater.inflate(ResourceUtil.getLayoutId(mContext, "ssearch_card_stock_item"), null);

		TextView nameTv = (TextView) view.findViewById(ResourceUtil.getId(mContext, "card_stock_name"));
		nameTv.setText(item.getTitle());
		TextView tickerTv = (TextView) view.findViewById(ResourceUtil.getId(mContext, "card_stock_ticker"));
		tickerTv.setText(item.getSymbol());

		TextView priceTv = (TextView) view.findViewById(ResourceUtil.getId(mContext, "card_stock_price"));
		priceTv.setText(item.getPrice());

		TextView priceChangeTv = (TextView) view.findViewById(ResourceUtil.getId(mContext, "card_stock_pencentage"));

		StringBuilder sb = new StringBuilder();
		if (TextUtils.isEmpty(item.getChangeValue()) || item.getChangeValue().equalsIgnoreCase(StockItem.UNKOWN)) {
			sb.append("0.00");
		} else {
			sb.append(item.getChangeValue());
		}
		sb.append("(");
		if (TextUtils.isEmpty(item.getChangePercent()) || item.getChangePercent().equalsIgnoreCase(StockItem.UNKOWN)) {
			sb.append("0.00%");
		} else {
			sb.append(item.getChangePercent());
		}
		sb.append(")");
		priceChangeTv.setText(sb.toString());

		if (item.isRising()) {
			priceChangeTv.setTextColor(mContext.getResources().getColor(ResourceUtil.getColorId(mContext, "ssearch_stock_up")));
		} else {
			priceChangeTv.setTextColor(mContext.getResources()
					.getColor(ResourceUtil.getColorId(mContext, "ssearch_stock_down")));
		}

		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (DeviceUtils.isNetConnected(mContext)) {
					AppLauncher.launchBrowser(mContext, item.getUrl());
				} else {
					Toast.makeText(mContext, ResourceUtil.getStringId(mContext, "ssearch_network_invalid"), Toast.LENGTH_SHORT)
							.show();
				}
			}
		});

		return view;
	}

	@Override
	public void buildCardView() {
		if (mMainView == null) {
			mMainView = (LinearLayout) mInflater.inflate(ResourceUtil.getLayoutId(mContext, "ssearch_card_stock"), null);
			mContentContainer = (LinearLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "stock_container"));
			mTitleTv = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "card_header_title"));
			mMenuBtn = (RelativeLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "card_header_menu"));
		}

		mContentContainer.removeAllViews();
		mTitleTv.setText(mContext.getResources().getString(ResourceUtil.getStringId(mContext, "ssearch_card_stock")));
		mMenuBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (DeviceUtils.isNetConnected(mContext)) {
					Intent intent = new Intent(mContext, StockManageActivity.class);
					intent.putExtra("title", mCardEntry.getCardTitle());
					((SearchActivity) mContext).startActivityForResult(intent, SearchActivity.REQ_MANAGE_STOCKS);
				} else {
					Toast.makeText(mContext, ResourceUtil.getStringId(mContext, "ssearch_network_invalid"), Toast.LENGTH_SHORT)
							.show();
				}
			}
		});

		if (mCardEntry != null && mCardEntry instanceof StockEntry) {
			StockEntry entry = (StockEntry) mCardEntry;
			ArrayList<CardItem> cardItems = entry.getCardItems();
			if (cardItems != null) {
				for (int i = 0; i < cardItems.size(); i++) {
					StockItem item = (StockItem) cardItems.get(i);
					mContentContainer.addView(buildStockItem(item));
				}
			} else {
				mMainView.setVisibility(View.GONE);
			}
		} else {
			mMainView.setVisibility(View.GONE);
		}
	}

	@Override
	public View getCardView() {
		return mMainView;
	}

}
