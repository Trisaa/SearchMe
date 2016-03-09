package com.solo.search.card;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.solo.search.card.entry.CardEntry;
import com.solo.search.card.entry.FunnyEntry;
import com.solo.search.card.model.CardItem;
import com.solo.search.card.model.FunnyItem;
import com.solo.search.card.view.CardHeaderView;
import com.solo.search.util.AppLauncher;
import com.solo.search.util.ResourceUtil;

public class FunnyCard extends BaseCard {

	public static final int ITEM_SIZE = 5;

	private int mCurrentIndex;
	private LinearLayout mMainView;
	private CardHeaderView mHeaderView;
	private LinearLayout mMoreView;

	private RelativeLayout[] mLayouts;
	private NetworkImageView[] mPreviewImgs;
	private TextView[] mTitleTvs;

	public FunnyCard(Context paramContext, CardEntry cardEntry) {
		super(paramContext, cardEntry);
	}

	@Override
	public String getCardId() {
		return CardConfig.CARD_ID_FUNNY;
	}

	@SuppressLint("InflateParams")
	@Override
	public void buildCardView() {
		if (mMainView == null) {
			mMainView = (LinearLayout) mInflater.inflate(ResourceUtil.getLayoutId(mContext, "ssearch_card_funny"), null);
			mHeaderView = (CardHeaderView) mMainView.findViewById(ResourceUtil.getId(mContext, "card_header"));
			mMoreView = (LinearLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "card_footer_more"));

			mLayouts = new RelativeLayout[ITEM_SIZE];
			mPreviewImgs = new NetworkImageView[ITEM_SIZE];
			mTitleTvs = new TextView[ITEM_SIZE];

			mLayouts[0] = (RelativeLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "card_funny_layout1"));
			mPreviewImgs[0] = (NetworkImageView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_iv1"));
			mTitleTvs[0] = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_title_tv1"));

			mLayouts[1] = (RelativeLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "card_funny_layout2"));
			mPreviewImgs[1] = (NetworkImageView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_iv2"));
			mTitleTvs[1] = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_title_tv2"));

			mLayouts[2] = (RelativeLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "card_funny_layout3"));
			mPreviewImgs[2] = (NetworkImageView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_iv3"));
			mTitleTvs[2] = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_title_tv3"));

			mLayouts[3] = (RelativeLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "card_funny_layout4"));
			mPreviewImgs[3] = (NetworkImageView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_iv4"));
			mTitleTvs[3] = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_title_tv4"));

			mLayouts[4] = (RelativeLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "card_funny_layout5"));
			mPreviewImgs[4] = (NetworkImageView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_iv5"));
			mTitleTvs[4] = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_title_tv5"));
		}

		mHeaderView.setTitleText(mContext.getResources().getString(ResourceUtil.getStringId(mContext, "ssearch_solo_funny")));
		mHeaderView.setOnRefreshClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				buildCardView();
			}
		});

		if (mCardEntry != null && mCardEntry instanceof FunnyEntry) {
			final FunnyEntry entry = (FunnyEntry) mCardEntry;
			ArrayList<CardItem> cardItems = entry.getCardItems();
			if (cardItems != null && cardItems.size() > 0) {
				for (int i = 0; i < ITEM_SIZE; i++) {
					if (mCurrentIndex >= cardItems.size()) {
						mCurrentIndex = 0;
					}
					final FunnyItem item = (FunnyItem) cardItems.get(mCurrentIndex++);

					mPreviewImgs[i].setImageUrl(item.getImg(), CardManager.getInstance(mContext).getImageLoader());

					mTitleTvs[i].setText(item.getTitle());

					mPreviewImgs[i].setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							AppLauncher.launchBrowser(mContext, item.getUrl());
						}
					});
				}
			} else {
				mMainView.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(entry.getMoreUrl())) {
				mMoreView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						AppLauncher.launchBrowser(mContext, entry.getMoreUrl());
					}
				});
			} else {
				mMoreView.setVisibility(View.GONE);
			}
		}

	}

	@Override
	public View getCardView() {
		return mMainView;
	}

}
