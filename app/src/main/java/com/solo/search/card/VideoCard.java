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
import com.solo.search.card.entry.VideoEntry;
import com.solo.search.card.model.CardItem;
import com.solo.search.card.model.VideoItem;
import com.solo.search.card.view.CardHeaderView;
import com.solo.search.util.AppLauncher;
import com.solo.search.util.ResourceUtil;

public class VideoCard extends BaseCard {

	private static final int VIDEO_SIZE = 3;
	private int mCurrentIndex;
	private LinearLayout mMainView;
	private CardHeaderView mHeaderView;
	private LinearLayout mMoreView;

	private RelativeLayout[] mLayouts;
	private NetworkImageView[] mPreviewImgs;
	private TextView[] mTitleTvs;
	private TextView[] mTimeTvs;

	public VideoCard(Context paramContext, CardEntry cardEntry) {
		super(paramContext, cardEntry);
	}

	@Override
	public String getCardId() {
		return CardConfig.CARD_ID_VIDEO;
	}

	@SuppressLint("InflateParams")
	@Override
	public void buildCardView() {
		if (mMainView == null) {
			mMainView = (LinearLayout) mInflater.inflate(ResourceUtil.getLayoutId(mContext, "ssearch_card_video"), null);
			mHeaderView = (CardHeaderView) mMainView.findViewById(ResourceUtil.getId(mContext, "card_header"));
			mMoreView = (LinearLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "card_footer_more"));

			mLayouts = new RelativeLayout[VIDEO_SIZE];
			mPreviewImgs = new NetworkImageView[VIDEO_SIZE];
			mTitleTvs = new TextView[VIDEO_SIZE];
			mTimeTvs = new TextView[VIDEO_SIZE];

			mLayouts[0] = (RelativeLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "card_video_layout1"));
			mPreviewImgs[0] = (NetworkImageView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_iv1"));
			mTitleTvs[0] = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_title_tv1"));
			mTimeTvs[0] = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_time_tv1"));

			mLayouts[1] = (RelativeLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "card_video_layout2"));
			mPreviewImgs[1] = (NetworkImageView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_iv2"));
			mTitleTvs[1] = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_title_tv2"));
			mTimeTvs[1] = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_time_tv2"));

			mLayouts[2] = (RelativeLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "card_video_layout3"));
			mPreviewImgs[2] = (NetworkImageView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_iv3"));
			mTitleTvs[2] = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_title_tv3"));
			mTimeTvs[2] = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "funny_time_tv3"));
		}

		mHeaderView.setTitleText(mContext.getResources().getString(ResourceUtil.getStringId(mContext, "ssearch_card_video")));
		mHeaderView.setOnRefreshClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				buildCardView();
			}
		});

		if (mCardEntry != null && mCardEntry instanceof VideoEntry) {
			for (int i = 0; i < VIDEO_SIZE; i++) {
				startDisappearAnimation(mLayouts[i]);
			}

			final VideoEntry videoEntry = (VideoEntry) mCardEntry;
			ArrayList<CardItem> cardItems = videoEntry.getCardItems();
			if (cardItems != null && cardItems.size() > 0) {
				for (int i = 0; i < VIDEO_SIZE; i++) {
					if (mCurrentIndex >= cardItems.size()) {
						mCurrentIndex = 0;
					}
					final VideoItem item = (VideoItem) cardItems.get(mCurrentIndex++);
					mPreviewImgs[i].setImageUrl(item.getImg(), CardManager.getInstance(mContext).getImageLoader());
					mTitleTvs[i].setText(item.getTitle());
					mTimeTvs[i].setText(item.getTime());

					mPreviewImgs[i].setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							AppLauncher.launchBrowser(mContext, item.getUrl());
						}
					});
					startAppearAnimation(mLayouts[i]);
				}
			} else {
				mMainView.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(videoEntry.getMoreUrl())) {
				mMoreView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						AppLauncher.launchBrowser(mContext, videoEntry.getMoreUrl());
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
