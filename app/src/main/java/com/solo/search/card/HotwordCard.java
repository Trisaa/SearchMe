package com.solo.search.card;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.solo.search.card.entry.CardEntry;
import com.solo.search.card.entry.HotwordEntry;
import com.solo.search.card.model.CardItem;
import com.solo.search.card.model.HotwordItem;
import com.solo.search.card.view.CardHeaderView;
import com.solo.search.util.AppLauncher;
import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchConfig;
import com.solo.search.util.SoloLauncherWeb;

public class HotwordCard extends BaseCard {

	public static final int HOTWORD_SIZE = 9;
	private static final int TOP_HOTWORD_SIZE = 3;
	private static final int[] TOP_HOTWORD_WEIGHTS = { 1, 1, 1 };// 顶部三个Item的布局权重
	private static final float BOTTOM_HOTWORD_WEIGHT_MAX_RATIO = 1.5f;

	private View mMainView;
	private CardHeaderView mHeaderView;
	private LinearLayout mTopContainer;
	private LinearLayout mBottomContainer;

	private int mCurrentHotwordIndex;

	public HotwordCard(Context paramContext, CardEntry cardEntry) {
		super(paramContext, cardEntry);
	}

	@Override
	public String getCardId() {
		return CardConfig.CARD_ID_HOTWORD;
	}

	private void launchAppHotword(HotwordItem hotword) {
		AppLauncher.openAppStore(mContext, hotword.getUrl());
	}

	private void launchWebpageHotword(final HotwordItem hotword) {
		if (!TextUtils.isEmpty(hotword.getUrl())) {
			if (hotword.getUrl().equals(SoloLauncherWeb.FACEBOOK_URL)) {
				AppLauncher.startFacebook(mContext);
			} else {
				AppLauncher.launchBrowser(mContext, hotword.getUrl());
			}
		}
	}

	private void searchHotword(HotwordItem hotword) {
		String type = hotword.getType();
		switch (type) {
		case SearchConfig.FEED_TYPE_APP:
			launchAppHotword(hotword);
			break;
		case SearchConfig.FEED_TYPE_HOTWORD:
			AppLauncher.launchSearch(mContext, hotword.getTitle());
			break;
		case SearchConfig.FEED_TYPE_WEBPAGE:
			launchWebpageHotword(hotword);
			break;
		}

	}

	@Override
	public void buildCardView() {
		mMainView = mInflater.inflate(ResourceUtil.getLayoutId(mContext, "ssearch_card_hotword"), null);
		if (mCardEntry != null) {
			mHeaderView = (CardHeaderView) mMainView.findViewById(ResourceUtil.getId(mContext, "card_header"));

			mTopContainer = (LinearLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "top_container"));
			mBottomContainer = (LinearLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "bottom_container"));

			mHeaderView.setTitleText(mContext.getResources().getString(
					ResourceUtil.getStringId(mContext, "ssearch_card_hotwords")));
			mHeaderView.setOnRefreshClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					refreshCardView();
				}
			});

			refreshCardView();
		}
	}

	public void setHeaderVisivility(int vidibility) {
		if (mHeaderView != null) {
			mHeaderView.setVisibility(vidibility);
		}
	}

	@Override
	public View getCardView() {
		return mMainView;
	}

	private View getLargeItemView(final HotwordItem item, int weight) {
		RelativeLayout view = (RelativeLayout) LayoutInflater.from(mContext).inflate(
				ResourceUtil.getLayoutId(mContext, "ssearch_card_hotword_large_item"), null);

		if (!TextUtils.isEmpty(item.getTitle())) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.MATCH_PARENT, weight);
			int margin = mContext.getResources().getDimensionPixelSize(
					ResourceUtil.getDimenId(mContext, "search_hotword_small_item_margin"));
			params.setMargins(margin, margin, margin, margin);
			view.setLayoutParams(params);
		}

		TextView titleTv = (TextView) view.findViewById(ResourceUtil.getId(mContext, "hotword_tv"));
		final ImageView iconIv = (ImageView) view.findViewById(ResourceUtil.getId(mContext, "hotword_iv"));

		titleTv.setText(item.getTitle());

		if (!TextUtils.isEmpty(item.getImg())) {
			CardManager.getInstance(mContext).getImageLoader().get(item.getImg(), new ImageListener() {

				@Override
				public void onErrorResponse(VolleyError arg0) {
				}

				@Override
				public void onResponse(ImageContainer arg0, boolean arg1) {
					Bitmap icon = arg0.getBitmap();
					if (icon != null) {
						iconIv.setVisibility(View.VISIBLE);
						iconIv.setImageBitmap(icon);
					}
				}
			});
		}

		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchHotword(item);
			}
		});

		return view;
	}

	private View getSmallItemView(final HotwordItem item, float weight) {
		View itemView = LayoutInflater.from(mContext).inflate(
				ResourceUtil.getLayoutId(mContext, "ssearch_card_hotword_small_item"), null);
		TextView titleTv = (TextView) itemView.findViewById(ResourceUtil.getId(mContext, "hotword_tv"));

		if (!TextUtils.isEmpty(item.getTitle())) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.MATCH_PARENT, weight);
			int margin = mContext.getResources().getDimensionPixelSize(
					ResourceUtil.getDimenId(mContext, "search_hotword_small_item_margin"));
			params.setMargins(margin, margin, margin, margin);
			itemView.setLayoutParams(params);
		}

		titleTv.setText(item.getTitle());

		if (item.isHot()) {
			titleTv.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(mContext, ResourceUtil
					.getDrawableId(mContext, "ssearch_ic_hot")), null);
		} else if (item.isNew()) {
			titleTv.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(mContext, ResourceUtil
					.getDrawableId(mContext, "ssearch_ic_new")), null);
		}

		String colorStr = item.getColor();
		if (TextUtils.isEmpty(colorStr)) {
			colorStr = HotwordItem.DEFAULT_COLOR;
		}

		int color;
		try {
			color = Color.parseColor(colorStr);
		} catch (Exception e) {
			color = Color.parseColor(HotwordItem.DEFAULT_COLOR);
		}
		itemView.setBackgroundColor(color);

		itemView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchHotword(item);
			}
		});

		return itemView;
	}

	private View getHotwordPairViews(HotwordItem leftHotword, HotwordItem rightHotword) {
		LinearLayout layout = new LinearLayout(mContext);
		leftHotword.isHot(true);
		rightHotword.isNew(true);

		float leftWeight = 1;
		float rightWeight = 1;

		if (leftHotword.getTitle().length() > 0) {
			leftWeight = 1.0f / leftHotword.getTitle().length();
		}

		if (rightHotword.getTitle().length() > 0) {
			rightWeight = 1.0f / rightHotword.getTitle().length();
		}

		// 确保左右两个宽度最大的比例控制在一定的范围内 BOTTOM_HOTWORD_WEIGHT_MAX_RATIO
		if (leftWeight > rightWeight) {
			leftWeight = Math.min(leftWeight, rightWeight * BOTTOM_HOTWORD_WEIGHT_MAX_RATIO);
		} else {
			rightWeight = Math.min(rightWeight, leftWeight * BOTTOM_HOTWORD_WEIGHT_MAX_RATIO);
		}

		View view1 = getSmallItemView(leftHotword, leftWeight);
		View view2 = getSmallItemView(rightHotword, rightWeight);
		layout.addView(view1);
		layout.addView(view2);
		return layout;
	}

	private void checkCurrentHotwordIndex() {
		if (mCurrentHotwordIndex >= mCardEntry.getCardItems().size()) {
			mCurrentHotwordIndex = 0;
		}
	}

	/**
	 * 是否显示顶部的热词，保证前TOP_HOTWORD_SIZE个热词的图片链接不为空，才显示
	 * 
	 * @param cardItems
	 * @return
	 */
	private boolean isShowTopHotwordLayout(ArrayList<CardItem> cardItems) {
		boolean isShow = true;
		int index = mCurrentHotwordIndex;
		for (int i = 0; i < TOP_HOTWORD_SIZE; i++) {
			if (index >= mCardEntry.getCardItems().size()) {
				index = 0;
			}
			HotwordItem item = (HotwordItem) cardItems.get(index);
			index += 1;
			if (TextUtils.isEmpty(item.getImg())) {
				isShow = false;
				break;
			}
		}
		return isShow;
	}

	public void refreshCardView() {
		if (mCardEntry != null && mCardEntry instanceof HotwordEntry) {
			final ArrayList<CardItem> cardItems = mCardEntry.getCardItems();
			if (cardItems != null && cardItems.size() >= HOTWORD_SIZE) {
				mTopContainer.removeAllViews();
				mBottomContainer.removeAllViews();

				boolean isShowTopHotwordLayout = isShowTopHotwordLayout(cardItems);
				int bottomItemCount;

				if (isShowTopHotwordLayout) {
					bottomItemCount = (HOTWORD_SIZE - TOP_HOTWORD_SIZE);
					mTopContainer.setVisibility(View.VISIBLE);
					for (int i = 0; i < TOP_HOTWORD_SIZE; i++) {
						checkCurrentHotwordIndex();
						HotwordItem item = (HotwordItem) cardItems.get(mCurrentHotwordIndex++);
						View itemView = getLargeItemView(item, TOP_HOTWORD_WEIGHTS[i]);
						mTopContainer.addView(itemView);
					}
				} else {
					mTopContainer.setVisibility(View.GONE);
					bottomItemCount = HOTWORD_SIZE;
				}

				// 一次添加两个Item
				for (int i = 0; (i + 2) <= bottomItemCount; i += 2) {
					checkCurrentHotwordIndex();
					HotwordItem item1 = (HotwordItem) cardItems.get(mCurrentHotwordIndex++);

					checkCurrentHotwordIndex();
					HotwordItem item2 = (HotwordItem) cardItems.get(mCurrentHotwordIndex++);

					View itemView = getHotwordPairViews(item1, item2);
					mBottomContainer.addView(itemView);
				}

			}

		} else {
			mMainView.setVisibility(View.GONE);
		}
	}
}
