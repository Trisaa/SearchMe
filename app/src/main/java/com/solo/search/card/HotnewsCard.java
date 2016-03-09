package com.solo.search.card;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.solo.search.card.entry.CardEntry;
import com.solo.search.card.entry.HotnewsEntry;
import com.solo.search.card.model.CardItem;
import com.solo.search.card.model.HotnewsItem;
import com.solo.search.card.view.CardHeaderView;
import com.solo.search.util.AppLauncher;
import com.solo.search.util.LogUtils;
import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchConfig;

public class HotnewsCard extends BaseCard {

	private static final String TAG = LogUtils.makeLogTag(HotnewsCard.class);

	public static final int HOTNEWS_SIZE = 5;
	private static final int INVALID_ADS_POSITION = -1;

	private int mCurrentHotnewsIndex;
	private LinearLayout mMainView;
	private CardHeaderView mHeaderView;
	private LinearLayout mNewsContainer;
	private LinearLayout mMoreView;

	private View mAdView;

	private boolean isAdViewAdded;
	private ViewPager mAdsPager;
	private AdsAdapter mAdsAdapter;
	private ArrayList<NativeAd> mAds = new ArrayList<NativeAd>();
	private String mAdsId;

	@SuppressLint("InflateParams")
	public HotnewsCard(Context context, CardEntry cardEntry) {
		super(context, cardEntry);
		mAdsId = context.getResources().getString(ResourceUtil.getStringId(mContext, "ssearch_fb_native_ads_id"));
		mAdView = LayoutInflater.from(context).inflate(ResourceUtil.getLayoutId(mContext, "ssearch_card_native_ad_unit"), null);

		mAdsPager = (ViewPager) mAdView.findViewById(ResourceUtil.getId(mContext, "ad_pager"));
		mAdsAdapter = new AdsAdapter(mAds);
		mAdsPager.setAdapter(mAdsAdapter);
		mAdsPager.setOffscreenPageLimit(1);
		mAdsPager.setPageMargin(15);
		mAdsPager.setClipChildren(false);

		loadNativeAds();
	}

	private void clearAds() {
		isAdViewAdded = false;
		mAds.clear();
		mAdsAdapter.notifyDataSetChanged();
	}

	private void loadNativeAds() {
		if (mAdsId == null) {
			mAdsId = CardConfig.FACEBOOK_ADS_PLACEMENT_IDS;
		}
		final NativeAd mNativeAd = new NativeAd(mContext, mAdsId);
		mNativeAd.setAdListener(new AdListener() {

			@Override
			public void onAdClicked(Ad arg0) {
			}

			@Override
			public void onAdLoaded(Ad arg0) {
				LogUtils.d(TAG, TAG + " onAdLoaded");

				mAds.add(mNativeAd);
				mAdsAdapter.setAds(mAds);
				mAdsAdapter.notifyDataSetChanged();

				int adsPosition = mNewsContainer.getChildCount();
				if (mCardEntry != null && mCardEntry instanceof HotnewsEntry) {
					HotnewsEntry hotwordEntry = (HotnewsEntry) mCardEntry;
					adsPosition = hotwordEntry.getNativeAdsPosition();
				}

				if (adsPosition != INVALID_ADS_POSITION && !isAdViewAdded) {
					adsPosition = Math.max(0, Math.min(adsPosition, mNewsContainer.getChildCount()));
					mNewsContainer.addView(mAdView, adsPosition);
					isAdViewAdded = true;
				}

			}

			@Override
			public void onError(Ad arg0, AdError arg1) {
				LogUtils.d(TAG, TAG + " onError msg:" + arg1.getErrorMessage());
			}
		});
		mNativeAd.loadAd();
	}

	@Override
	public String getCardId() {
		return CardConfig.CARD_ID_HOTNEWS;
	}

	private void launchApp(String url) {
		AppLauncher.openAppStore(mContext, url);
	}

	private void clickNewsItem(HotnewsItem item) {
		String type = item.getType();
		if (!TextUtils.isEmpty(type)) {
			switch (type) {
			case SearchConfig.FEED_TYPE_APP:
				launchApp(item.getUrl());
				break;
			case SearchConfig.FEED_TYPE_HOTWORD:
				AppLauncher.launchSearch(mContext, item.getUrl());
				break;
			default:
				AppLauncher.launchBrowser(mContext, item.getUrl());
				break;
			}
		} else {
			AppLauncher.launchBrowser(mContext, item.getUrl());
		}

	}

	@SuppressLint("InflateParams")
	private View buildNewsItem(final int position, final HotnewsItem item) {
		View view = mInflater.inflate(ResourceUtil.getLayoutId(mContext, "ssearch_card_news_item"), null);

		TextView titleTv = (TextView) view.findViewById(ResourceUtil.getId(mContext, "news_title"));
		titleTv.setText(item.getTitle());

		TextView authorTv = (TextView) view.findViewById(ResourceUtil.getId(mContext, "news_author"));
		authorTv.setText(item.getAuthor());

		NetworkImageView image = (NetworkImageView) view.findViewById(ResourceUtil.getId(mContext, "news_image"));
		if (!TextUtils.isEmpty(item.getImg())) {
			image.setVisibility(View.VISIBLE);
			image.setImageUrl(item.getImg(), CardManager.getInstance(mContext).getImageLoader());
		} else {
			image.setVisibility(View.GONE);
		}

		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				clickNewsItem(item);
			}
		});
		startAppearAnimation(view);

		return view;
	}

	@SuppressLint("InflateParams")
	@Override
	public void buildCardView() {
		if (mMainView == null) {
			mMainView = (LinearLayout) mInflater.inflate(ResourceUtil.getLayoutId(mContext, "ssearch_card_news"), null);
			mNewsContainer = (LinearLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "news_container"));
			mHeaderView = (CardHeaderView) mMainView.findViewById(ResourceUtil.getId(mContext, "card_header"));
			mMoreView = (LinearLayout) mMainView.findViewById(ResourceUtil.getId(mContext, "card_footer_more"));
		}

		mHeaderView.setTitleText(mContext.getResources().getString(ResourceUtil.getStringId(mContext, "ssearch_card_news")));
		mHeaderView.setOnRefreshClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				buildCardView();
				clearAds();
				loadNativeAds();
			}
		});

		if (mCardEntry != null && mCardEntry instanceof HotnewsEntry) {
			int viewCount = mNewsContainer.getChildCount();
			for (int i = 0; i < viewCount; i++) {
				startDisappearAnimation(mNewsContainer.getChildAt(i));
			}

			new Handler().postDelayed(new Runnable() {

				public void run() {
					mNewsContainer.removeAllViews();

					final HotnewsEntry entry = (HotnewsEntry) mCardEntry;
					ArrayList<CardItem> cardItems = entry.getCardItems();
					if (cardItems != null && cardItems.size() > 0) {
						for (int i = 0; i < HOTNEWS_SIZE; i++) {
							if (mCurrentHotnewsIndex >= cardItems.size()) {
								mCurrentHotnewsIndex = 0;
							}
							HotnewsItem item = (HotnewsItem) cardItems.get(mCurrentHotnewsIndex++);
							mNewsContainer.addView(buildNewsItem(i, item));
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
			}, mContext.getResources().getInteger(android.R.integer.config_shortAnimTime));

		}

	}

	@Override
	public View getCardView() {
		return mMainView;
	}

	@SuppressLint("InflateParams")
	private View inflateAd(NativeAd nativeAd, Context context) {
		View adView = LayoutInflater.from(context).inflate(
				ResourceUtil.getLayoutId(mContext, "ssearch_card_native_ad_pager_item"), null);

		// Create native UI using the ad metadata.

		ImageView nativeAdIcon = (ImageView) adView.findViewById(ResourceUtil.getId(mContext, "nativeAdIcon"));
		TextView nativeAdTitle = (TextView) adView.findViewById(ResourceUtil.getId(mContext, "nativeAdTitle"));
		TextView nativeAdBody = (TextView) adView.findViewById(ResourceUtil.getId(mContext, "nativeAdBody"));
		ImageView nativeAdImage = (ImageView) adView.findViewById(ResourceUtil.getId(mContext, "nativeAdImage"));
		TextView nativeAdCallToAction = (TextView) adView.findViewById(ResourceUtil.getId(mContext, "nativeAdCallToAction"));

		// Setting the Text
		nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
		nativeAdCallToAction.setVisibility(View.VISIBLE);
		nativeAdTitle.setText(nativeAd.getAdTitle());
		nativeAdBody.setText(nativeAd.getAdBody());

		// Downloading and setting the ad icon.
		NativeAd.Image adIcon = nativeAd.getAdIcon();
		NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

		// Downloading and setting the cover image.
		NativeAd.Image adCoverImage = nativeAd.getAdCoverImage();
		NativeAd.downloadAndDisplayImage(adCoverImage, nativeAdImage);
		nativeAd.registerViewForInteraction(adView);

		return adView;

	}

	private class AdsAdapter extends PagerAdapter {

		private List<View> mViews = new ArrayList<View>();
		private List<NativeAd> mAds;

		public AdsAdapter(List<NativeAd> ads) {
			mAds = ads;
		}

		public void setAds(List<NativeAd> ads) {
			mAds = ads;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mViews.get(position));
		}

		@Override
		public int getCount() {
			return mAds.size();
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			final NativeAd ad = mAds.get(position);
			View adView = inflateAd(ad, mContext);

			container.addView(adView);
			mViews.add(adView);
			return adView;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

}
