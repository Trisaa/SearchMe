package com.solo.search.card;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.solo.search.card.entry.CardEntry;
import com.solo.search.card.model.AdsItem;
import com.solo.search.card.model.CardItem;
import com.solo.search.util.AppLauncher;
import com.solo.search.util.PreferenceConstants;
import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SharedPreferencesHelper;
import com.solo.search.util.SoloLauncherWeb;

public class AdsCard extends BaseCard {

	private View mMainView;
	private ImageView mAdsBanner;
	private ImageView mCloseBtn;

	private AdsItem mAdsItem;

	public AdsCard(Context paramContext, CardEntry cardEntry) {
		super(paramContext, cardEntry);
	}

	@Override
	public String getCardId() {
		return CardConfig.CARD_ID_ADS;
	}

	private void launchApp(String url) {
		if (!TextUtils.isEmpty(url)) {
			AppLauncher.openAppStore(mContext, url);
		}
	}

	private void launchWebpage(String url) {
		if (!TextUtils.isEmpty(url)) {
			if (url.equals(SoloLauncherWeb.FACEBOOK_PAGE_URL)) {
				AppLauncher.startFacebook(mContext);
			} else {
				AppLauncher.launchBrowser(mContext, url);
			}
		}
	}

	@Override
	public void buildCardView() {
		mMainView = mInflater.inflate(ResourceUtil.getLayoutId(mContext, "ssearch_card_ads"), null);
		mAdsBanner = (ImageView) mMainView.findViewById(ResourceUtil.getId(mContext, "ads_banner"));
		mCloseBtn = (ImageView) mMainView.findViewById(ResourceUtil.getId(mContext, "close_ads"));
		if (mCardEntry != null) {
			ArrayList<CardItem> adsItems = (ArrayList<CardItem>) mCardEntry.getCardItems();
			if (adsItems != null && adsItems.size() > 0) {
				mAdsItem = (AdsItem) adsItems.get(0);
				if (!TextUtils.isEmpty(mAdsItem.getImg())) {
					CardManager.getInstance(mContext).getImageLoader().get(mAdsItem.getImg(), new ImageListener() {

						@Override
						public void onErrorResponse(VolleyError arg0) {
							mMainView.setVisibility(View.GONE);
						}

						@Override
						public void onResponse(ImageContainer arg0, boolean arg1) {
							Bitmap banner = arg0.getBitmap();
							if (banner != null) {
								mMainView.setVisibility(View.VISIBLE);
								mCloseBtn.setVisibility(View.VISIBLE);
								mAdsBanner.setImageBitmap(banner);
							} else {
								mMainView.setVisibility(View.GONE);
							}
						}
					});
				} else {
					mMainView.setVisibility(View.GONE);
				}
			}
		}

		mAdsBanner.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mAdsItem != null) {
					if (mAdsItem.getType().equals(CardConfig.CARD_TYPE_APP)) {
						launchApp(mAdsItem.getUrl());
					} else {
						launchWebpage(mAdsItem.getUrl());
					}
				}
			}
		});

		mCloseBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mMainView.getParent() != null) {
					removeSelfView();
					SharedPreferencesHelper.setLong(mContext, PreferenceConstants.KEY_SEARCH_ADS_CLOSE_TIME, System
							.currentTimeMillis());
				}
			}
		});
	}

	private void removeSelfView() {
		ViewGroup parent = (ViewGroup) mMainView.getParent();
		if (parent != null) {
			parent.removeView(mMainView);
		}
	}

	@Override
	public View getCardView() {
		return mMainView;
	}

}
