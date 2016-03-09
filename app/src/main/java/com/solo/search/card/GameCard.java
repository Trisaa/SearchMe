package com.solo.search.card;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.solo.search.card.entry.CardEntry;
import com.solo.search.card.entry.GameEntry;
import com.solo.search.card.model.CardItem;
import com.solo.search.card.model.GameItem;
import com.solo.search.card.view.CardHeaderView;
import com.solo.search.util.AppLauncher;
import com.solo.search.util.ResourceUtil;

public class GameCard extends BaseCard {

	private static final int GAME_SIZE = 3;
	private int mCurrentIndex;
	private LinearLayout mMainView;
	private CardHeaderView mHeaderView;
	private LinearLayout mMoreView;

	private NetworkImageView[] mPreviewImgs;
	private TextView[] mTitleTvs;

	public GameCard(Context paramContext, CardEntry cardEntry) {
		super(paramContext, cardEntry);
	}

	@Override
	public String getCardId() {
		return CardConfig.CARD_ID_GAME;
	}

	@SuppressLint("InflateParams")
	@Override
	public void buildCardView() {
		if (mMainView == null) {
			mMainView = (LinearLayout) mInflater.inflate(ResourceUtil.getLayoutId(mContext, "ssearch_card_game"), null);
			mHeaderView = (CardHeaderView) mMainView.findViewById(ResourceUtil.getId(mContext, "card_header"));
			mMoreView = (LinearLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "card_footer_more"));
		}

		mHeaderView.setTitleText(mContext.getResources().getString(ResourceUtil.getStringId(mContext, "ssearch_card_game")));
		mHeaderView.setRefreshButtonVisibility(View.GONE);

		mPreviewImgs = new NetworkImageView[GAME_SIZE];
		mTitleTvs = new TextView[GAME_SIZE];

		mPreviewImgs[0] = (NetworkImageView) mMainView.findViewById(ResourceUtil.getId(mContext, "game_iv1"));
		mTitleTvs[0] = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "game_title_tv1"));

		mPreviewImgs[1] = (NetworkImageView) mMainView.findViewById(ResourceUtil.getId(mContext, "game_iv2"));
		mTitleTvs[1] = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "game_title_tv2"));

		mPreviewImgs[2] = (NetworkImageView) mMainView.findViewById(ResourceUtil.getId(mContext, "game_iv3"));
		mTitleTvs[2] = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "game_title_tv3"));

		if (mCardEntry != null && mCardEntry instanceof GameEntry) {
			final GameEntry gameEntry = (GameEntry) mCardEntry;
			ArrayList<CardItem> cardItems = gameEntry.getCardItems();
			if (cardItems != null && cardItems.size() > 0) {
				for (int i = 0; i < GAME_SIZE; i++) {
					if (mCurrentIndex >= cardItems.size()) {
						mCurrentIndex = 0;
					}
					final GameItem item = (GameItem) cardItems.get(mCurrentIndex++);
					mPreviewImgs[i].setImageUrl(item.getImg(), CardManager.getInstance(mContext).getImageLoader());
					mTitleTvs[i].setText(item.getTitle());
					mPreviewImgs[i].setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// 统计
							AppLauncher.launchBrowser(mContext, item.getUrl(), true);
						}
					});
				}
			} else {
				mMainView.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(gameEntry.getMoreUrl())) {
				mMoreView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						AppLauncher.launchBrowser(mContext, gameEntry.getMoreUrl(), true);
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
